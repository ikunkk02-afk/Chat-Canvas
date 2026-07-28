package io.github.ikunkk02.chatcanvas.chat.command;

import io.github.ikunkk02.chatcanvas.config.CommandClipboardConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Owns command-tool data independently from chat output and input history.
 */
public final class CommandToolManager {
	public enum MutationResult {
		CHANGED, UNCHANGED, INVALID, LIMIT_REACHED, SAVE_PENDING
	}

	@FunctionalInterface
	public interface ErrorSink {
		void report(String summary, Throwable throwable);
	}

	private static final long SAVE_DELAY_MS = 750L;

	private final CommandToolStorage storage;
	private final CommandClipboardStorage legacyStorage;
	private final Path legacyPath;
	private final Supplier<CommandClipboardConfig> config;
	private final ErrorSink errors;
	private final List<CommandHistoryEntry> recent = new ArrayList<>();
	private final List<FavoriteCommandEntry> favorites = new ArrayList<>();
	private long revision;
	private long saveAt;

	public CommandToolManager(
			CommandToolStorage storage,
			CommandClipboardStorage legacyStorage,
			Path legacyPath,
			Supplier<CommandClipboardConfig> config,
			ErrorSink errors
	) {
		this.storage = storage;
		this.legacyStorage = legacyStorage;
		this.legacyPath = legacyPath;
		this.config = config;
		this.errors = errors == null ? (summary, failure) -> { } : errors;
		load();
	}

	public synchronized List<CommandHistoryEntry> recent() {
		return List.copyOf(recent);
	}

	public synchronized List<FavoriteCommandEntry> favorites() {
		return List.copyOf(favorites);
	}

	public synchronized long revision() {
		return revision;
	}

	public synchronized void recordExecuted(
			String rawCommand, String serverIdentifier, long now) {
		CommandClipboardConfig options = config.get().sanitized();
		String command = CommandTextSanitizer.normalizeCommand(rawCommand);
		if (!options.recordRecentCommands() || command.length() <= 1
				|| isExcluded(command, options)) {
			return;
		}
		recent.removeIf(entry -> entry.command().equals(command));
		recent.add(0, CommandHistoryEntry.create(command, now, serverIdentifier));
		trimRecent(options.maxRecentCommands());
		changed(now);
	}

	public synchronized boolean deleteRecent(UUID id, long now) {
		if (!recent.removeIf(entry -> entry.entryId().equals(id))) return false;
		changed(now);
		return true;
	}

	public synchronized boolean clearRecent(long now) {
		if (recent.isEmpty()) return false;
		recent.clear();
		changed(now);
		return true;
	}

	public synchronized boolean clearRecentForServer(String serverIdentifier, long now) {
		if (serverIdentifier == null || serverIdentifier.isBlank()) return false;
		if (!recent.removeIf(entry ->
				serverIdentifier.equals(entry.serverIdentifier()))) return false;
		changed(now);
		return true;
	}

	public synchronized MutationResult addFavorite(
			String name, String rawCommand, long now) {
		String command = CommandTextSanitizer.normalizeCommand(rawCommand);
		if (command.length() <= 1 || name == null || name.isBlank()) {
			return MutationResult.INVALID;
		}
		Optional<FavoriteCommandEntry> existing = favorites.stream()
				.filter(entry -> entry.command().equals(command))
				.findFirst();
		if (existing.isPresent()) {
			FavoriteCommandEntry old = existing.get();
			favorites.remove(old);
			favorites.add(0, old.edited(name, command, now));
			normalizeFavoriteOrder();
			changed(now);
			return MutationResult.CHANGED;
		}
		if (favorites.size() >= config.get().maxCommands()) {
			return MutationResult.LIMIT_REACHED;
		}
		favorites.add(0, FavoriteCommandEntry.create(
				name, command, 0, now));
		normalizeFavoriteOrder();
		changed(now);
		return MutationResult.CHANGED;
	}

	public synchronized MutationResult editFavorite(
			UUID id, String name, String rawCommand, long now) {
		int index = favoriteIndex(id);
		String command = CommandTextSanitizer.normalizeCommand(rawCommand);
		if (index < 0 || command.length() <= 1
				|| name == null || name.isBlank()) {
			return MutationResult.INVALID;
		}
		for (int i = 0; i < favorites.size(); i++) {
			if (i != index && favorites.get(i).command().equals(command)) {
				return MutationResult.UNCHANGED;
			}
		}
		favorites.set(index, favorites.get(index).edited(name, command, now));
		changed(now);
		return MutationResult.CHANGED;
	}

	public synchronized boolean deleteFavorite(UUID id, long now) {
		if (!favorites.removeIf(entry -> entry.entryId().equals(id))) return false;
		normalizeFavoriteOrder();
		changed(now);
		return true;
	}

	public synchronized boolean moveFavorite(UUID id, int direction, long now) {
		int index = favoriteIndex(id);
		int target = Math.max(0, Math.min(favorites.size() - 1, index + direction));
		if (index < 0 || index == target) return false;
		FavoriteCommandEntry entry = favorites.remove(index);
		favorites.add(target, entry);
		normalizeFavoriteOrder();
		changed(now);
		return true;
	}

	public synchronized List<CommandHistoryEntry> searchRecent(String query) {
		String needle = normalizedQuery(query);
		return recent.stream()
				.filter(entry -> needle.isEmpty()
						|| entry.command().toLowerCase(Locale.ROOT).contains(needle)
						|| CommandTextSanitizer.commandName(entry.command())
						.toLowerCase(Locale.ROOT).contains(needle))
				.toList();
	}

	public synchronized List<FavoriteCommandEntry> searchFavorites(String query) {
		String needle = normalizedQuery(query);
		return favorites.stream()
				.filter(entry -> needle.isEmpty()
						|| entry.name().toLowerCase(Locale.ROOT).contains(needle)
						|| entry.command().toLowerCase(Locale.ROOT).contains(needle))
				.toList();
	}

	public synchronized void tick(long now) {
		if (saveAt != 0L && now >= saveAt) flush();
	}

	public synchronized boolean flush() {
		if (saveAt == 0L) return true;
		boolean saved = storage.save(snapshot(true));
		if (saved) {
			saveAt = 0L;
		} else {
			errors.report("命令工具数据保存失败，修改已保留在内存中", null);
			saveAt = System.currentTimeMillis() + SAVE_DELAY_MS;
		}
		return saved;
	}

	private void load() {
		CommandToolStorage.LoadResult result = storage.load();
		recent.addAll(result.data().recent());
		favorites.addAll(result.data().favorites());
		if (result.status() == CommandToolStorage.LoadStatus.RECOVERED_CORRUPT) {
			errors.report("命令工具数据损坏，已备份并恢复为空数据", null);
		} else if (result.status() == CommandToolStorage.LoadStatus.PARTIAL_RECOVERY) {
			errors.report("命令工具数据包含无效记录，已跳过损坏项目", null);
		}
		if (!result.data().migrationCompleted()) migrateLegacy();
		trimRecent(config.get().maxRecentCommands());
		normalizeFavoriteOrder();
	}

	private void migrateLegacy() {
		long now = System.currentTimeMillis();
		try {
			if (legacyStorage != null && legacyPath != null && Files.exists(legacyPath)) {
				Set<String> commands = new HashSet<>();
				favorites.forEach(entry -> commands.add(entry.command()));
				for (SavedCommand saved : legacyStorage.load().commands()) {
					String command = CommandTextSanitizer.normalizeCommand(saved.command());
					if (command.length() <= 1 || !commands.add(command)) continue;
					long created = saved.createdAt() > 0L ? saved.createdAt() : now;
					long updated = Math.max(created, saved.updatedAt());
					favorites.add(new FavoriteCommandEntry(
							saved.id(), saved.title(), command, created, updated,
							favorites.size(), ""));
				}
			}
			normalizeFavoriteOrder();
			if (!storage.save(snapshot(true))) {
				errors.report("旧指令数据迁移完成，但新数据暂时无法保存", null);
				saveAt = now + SAVE_DELAY_MS;
			}
		} catch (RuntimeException failure) {
			errors.report("旧指令数据迁移失败，已保留旧文件", failure);
			if (!storage.save(snapshot(true))) saveAt = now + SAVE_DELAY_MS;
		}
	}

	private CommandToolData snapshot(boolean migrated) {
		return new CommandToolData(
				CommandToolData.CURRENT_VERSION, migrated, recent, favorites);
	}

	private void changed(long now) {
		revision++;
		saveAt = Math.max(saveAt, now + SAVE_DELAY_MS);
	}

	private void trimRecent(int capacity) {
		while (recent.size() > capacity) recent.remove(recent.size() - 1);
		recent.sort(Comparator.comparingLong(
				CommandHistoryEntry::executedAt).reversed());
	}

	private void normalizeFavoriteOrder() {
		for (int i = 0; i < favorites.size(); i++) {
			favorites.set(i, favorites.get(i).withSortOrder(i));
		}
	}

	private int favoriteIndex(UUID id) {
		for (int i = 0; i < favorites.size(); i++) {
			if (favorites.get(i).entryId().equals(id)) return i;
		}
		return -1;
	}

	private static boolean isExcluded(
			String command, CommandClipboardConfig options) {
		if (SensitiveCommandDetector.isSensitive(command)) return true;
		String root = CommandTextSanitizer.commandName(command)
				.toLowerCase(Locale.ROOT);
		int namespace = root.lastIndexOf(':');
		if (namespace >= 0) root = root.substring(namespace + 1);
		return options.excludedCommandNames().contains(root);
	}

	private static String normalizedQuery(String query) {
		return query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
	}
}
