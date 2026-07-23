package io.github.ikunkk02.chatcanvas.chat.command;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.CommandClipboardConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

public final class CommandClipboardManager {
	public enum AddResult {
		ADDED, UPDATED_EXISTING, DUPLICATE, LIMIT_REACHED, INVALID, SAVE_FAILED
	}

	private static final long USE_FLUSH_DELAY_MS = 750L;
	private static final CommandClipboardManager INSTANCE = new CommandClipboardManager(
			new CommandClipboardStorage(FabricLoader.getInstance().getConfigDir()
					.resolve("chat_canvas").resolve("command_clipboard.json")));

	private final CommandClipboardStorage storage;
	private final List<SavedCommand> commands = new ArrayList<>();
	private long revision;
	private long pendingUseFlushAt;

	CommandClipboardManager(CommandClipboardStorage storage) {
		this.storage = storage;
		commands.addAll(storage.load().commands());
		normalizeSortOrder();
	}

	public static CommandClipboardManager instance() {
		return INSTANCE;
	}

	public synchronized List<SavedCommand> commands() {
		return List.copyOf(commands);
	}

	public synchronized long revision() {
		return revision;
	}

	public synchronized AddResult add(String title, String command, String category,
									  boolean updateExisting) {
		String normalized = command == null ? "" : command.strip();
		if (!normalized.startsWith("/") || normalized.length() < 2) return AddResult.INVALID;
		Optional<SavedCommand> duplicate = findByCommand(normalized);
		CommandClipboardConfig config = ChatCanvasConfig.instance().commandClipboard();
		if (duplicate.isPresent() && !config.allowDuplicates()) {
			if (!updateExisting) return AddResult.DUPLICATE;
			SavedCommand existing = duplicate.get();
			return replace(existing.id(), existing.edited(title, normalized, category,
					System.currentTimeMillis())) ? AddResult.UPDATED_EXISTING : AddResult.SAVE_FAILED;
		}
		if (commands.size() >= config.maxCommands()) return AddResult.LIMIT_REACHED;
		long now = System.currentTimeMillis();
		commands.add(SavedCommand.create(defaultTitle(title, normalized), normalized,
				category, commands.size(), now));
		return persistMutation() ? AddResult.ADDED : AddResult.SAVE_FAILED;
	}

	public synchronized boolean edit(UUID id, String title, String command, String category) {
		int index = indexOf(id);
		if (index < 0 || command == null || !command.strip().startsWith("/")) return false;
		SavedCommand old = commands.get(index);
		commands.set(index, old.edited(defaultTitle(title, command), command, category,
				System.currentTimeMillis()));
		return persistMutation();
	}

	public synchronized boolean toggleFavorite(UUID id) {
		return update(id, command -> command.withFavorite(!command.favorite(),
				System.currentTimeMillis()), true);
	}

	public synchronized boolean delete(UUID id) {
		if (!commands.removeIf(command -> command.id().equals(id))) return false;
		return persistMutation();
	}

	public synchronized boolean deleteCategory(String category) {
		String target = category == null ? "" : category;
		if (!commands.removeIf(command -> command.category().equalsIgnoreCase(target))) return false;
		return persistMutation();
	}

	public synchronized boolean clearNonFavorites() {
		if (!commands.removeIf(command -> !command.favorite())) return false;
		return persistMutation();
	}

	public synchronized boolean clearAll() {
		if (commands.isEmpty()) return false;
		commands.clear();
		return persistMutation();
	}

	public synchronized boolean move(UUID id, int delta) {
		int index = indexOf(id);
		int target = Math.max(0, Math.min(commands.size() - 1, index + delta));
		if (index < 0 || target == index) return false;
		SavedCommand command = commands.remove(index);
		commands.add(target, command);
		return persistMutation();
	}

	public synchronized void markUsed(UUID id, long nowMs) {
		if (update(id, command -> command.used(nowMs), false)) {
			pendingUseFlushAt = Math.max(pendingUseFlushAt, nowMs + USE_FLUSH_DELAY_MS);
		}
	}

	public synchronized void tick(long nowMs) {
		if (pendingUseFlushAt != 0L && nowMs >= pendingUseFlushAt) flush();
	}

	public synchronized void flush() {
		if (pendingUseFlushAt == 0L) return;
		storage.save(new CommandClipboardData(CommandClipboardData.CURRENT_VERSION, commands));
		pendingUseFlushAt = 0L;
	}

	public synchronized List<SavedCommand> search(
			String query, String category, boolean favoritesOnly, boolean recent) {
		String needle = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
		String categoryNeedle = category == null ? "" : category.strip().toLowerCase(Locale.ROOT);
		Comparator<SavedCommand> order = recent
				? Comparator.comparingLong(SavedCommand::lastUsedAt).reversed()
				: Comparator.comparingInt(SavedCommand::sortOrder);
		return commands.stream()
				.filter(command -> !favoritesOnly || command.favorite())
				.filter(command -> categoryNeedle.isEmpty()
						|| command.category().toLowerCase(Locale.ROOT).equals(categoryNeedle))
				.filter(command -> needle.isEmpty()
						|| command.title().toLowerCase(Locale.ROOT).contains(needle)
						|| command.command().toLowerCase(Locale.ROOT).contains(needle)
						|| command.category().toLowerCase(Locale.ROOT).contains(needle))
				.sorted(order)
				.toList();
	}

	public synchronized Set<String> categories() {
		LinkedHashSet<String> result = new LinkedHashSet<>();
		commands.stream().map(SavedCommand::category).filter(value -> !value.isBlank())
				.sorted(String.CASE_INSENSITIVE_ORDER).forEach(result::add);
		return result;
	}

	public synchronized Optional<SavedCommand> findByCommand(String command) {
		if (command == null) return Optional.empty();
		String normalized = command.strip();
		return commands.stream().filter(value -> value.command().equals(normalized)).findFirst();
	}

	private boolean update(UUID id, UnaryOperator<SavedCommand> operation, boolean persist) {
		int index = indexOf(id);
		if (index < 0) return false;
		commands.set(index, operation.apply(commands.get(index)));
		revision++;
		return !persist || persist();
	}

	private boolean replace(UUID id, SavedCommand value) {
		int index = indexOf(id);
		if (index < 0) return false;
		commands.set(index, value.withSortOrder(commands.get(index).sortOrder()));
		return persistMutation();
	}

	private boolean persistMutation() {
		normalizeSortOrder();
		revision++;
		return persist();
	}

	private boolean persist() {
		return storage.save(new CommandClipboardData(CommandClipboardData.CURRENT_VERSION, commands));
	}

	private int indexOf(UUID id) {
		for (int i = 0; i < commands.size(); i++) {
			if (commands.get(i).id().equals(id)) return i;
		}
		return -1;
	}

	private void normalizeSortOrder() {
		for (int i = 0; i < commands.size(); i++) {
			commands.set(i, commands.get(i).withSortOrder(i));
		}
	}

	private static String defaultTitle(String title, String command) {
		if (title != null && !title.isBlank()) return title.strip();
		String value = command.strip().substring(1);
		int space = value.indexOf(' ');
		return space < 0 ? value : value.substring(0, space);
	}
}
