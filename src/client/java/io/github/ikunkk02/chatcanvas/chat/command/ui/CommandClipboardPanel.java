package io.github.ikunkk02.chatcanvas.chat.command.ui;

import io.github.ikunkk02.chatcanvas.chat.command.CommandClipboardManager;
import io.github.ikunkk02.chatcanvas.chat.command.CommandPresetRegistry;
import io.github.ikunkk02.chatcanvas.chat.command.SavedCommand;
import io.github.ikunkk02.chatcanvas.chat.command.SensitiveCommandDetector;
import io.github.ikunkk02.chatcanvas.chat.interaction.ChatFieldActions;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.CommandClipboardConfig;
import io.github.ikunkk02.chatcanvas.config.CommandInsertMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.WeakHashMap;

public final class CommandClipboardPanel {
	private static final int WIDTH = 276;
	private static final int HEIGHT = 246;
	private static final int ROW_HEIGHT = 27;
	private static boolean openNextScreen;
	private static final WeakHashMap<ChatScreen, CommandClipboardPanel> ACTIVE =
			new WeakHashMap<>();
	private final CommandClipboardManager manager = CommandClipboardManager.instance();
	private ChatScreen owner;
	private TextFieldWidget searchField;
	private TextFieldWidget titleField;
	private TextFieldWidget categoryField;
	private TextFieldWidget commandField;
	private boolean open;
	private boolean presets;
	private boolean favoritesOnly;
	private boolean recent;
	private int categoryIndex;
	private int x;
	private int y;
	private int scroll;
	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;
	private long cachedRevision = Long.MIN_VALUE;
	private String cachedQuery = "";
	private String cachedCategory = "";
	private boolean cachedFavorites;
	private boolean cachedRecent;
	private List<SavedCommand> cachedCommands = List.of();
	private Dialog dialog = Dialog.NONE;
	private SavedCommand editing;
	private String pendingCommand;
	private String statusKey;

	public void init(ChatScreen screen, TextFieldWidget chatField) {
		owner = screen;
		ACTIVE.put(screen, this);
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		searchField = new TextFieldWidget(renderer, 0, 0, WIDTH - 16, 18,
				Text.translatable("chat_canvas.command.search"));
		searchField.setPlaceholder(Text.translatable("chat_canvas.command.search"));
		titleField = new TextFieldWidget(renderer, 0, 0, WIDTH - 32, 18,
				Text.translatable("chat_canvas.command.name"));
		titleField.setPlaceholder(Text.translatable("chat_canvas.command.name"));
		categoryField = new TextFieldWidget(renderer, 0, 0, WIDTH - 32, 18,
				Text.translatable("chat_canvas.command.category"));
		categoryField.setPlaceholder(Text.translatable("chat_canvas.command.category"));
		commandField = new TextFieldWidget(renderer, 0, 0, WIDTH - 32, 18,
				Text.translatable("chat_canvas.command.command"));
		commandField.setPlaceholder(Text.translatable("chat_canvas.command.command"));
		commandField.setMaxLength(1024);
		int rightSpace = screen.width - chatField.getX() - chatField.getWidth();
		x = rightSpace >= WIDTH + 8
				? chatField.getX() + chatField.getWidth() + 4
				: Math.max(4, chatField.getX() - WIDTH - 4);
		y = Math.max(4, chatField.getY() - HEIGHT - 4);
		clamp(screen);
		if (openNextScreen) {
			open = true;
			openNextScreen = false;
		}
	}

	public static void requestOpenNextChatScreen() {
		openNextScreen = true;
	}

	public static boolean dispatchCharTyped(ChatScreen screen, char chr, int modifiers) {
		CommandClipboardPanel panel = ACTIVE.get(screen);
		return panel != null && panel.charTyped(chr, modifiers);
	}

	public static boolean dispatchMouseDragged(
			ChatScreen screen, double mouseX, double mouseY, int button) {
		CommandClipboardPanel panel = ACTIVE.get(screen);
		return panel != null && panel.mouseDragged(screen, mouseX, mouseY, button);
	}

	public static boolean dispatchMouseReleased(ChatScreen screen, int button) {
		CommandClipboardPanel panel = ACTIVE.get(screen);
		return panel != null && panel.mouseReleased(button);
	}

	public boolean mouseClicked(ChatScreen screen, TextFieldWidget chatField,
								ChatInputSuggestor suggestor,
								double mouseX, double mouseY, int button) {
		CommandClipboardConfig config = ChatCanvasConfig.instance().commandClipboard();
		if (!config.enabled()) return false;
		if (dialog != Dialog.NONE) return dialogClick(mouseX, mouseY, button);
		if (config.showPanelButton() && !open
				&& buttonX(screen, chatField) <= mouseX
				&& mouseX < buttonX(screen, chatField) + 43
				&& chatField.getY() <= mouseY && mouseY < chatField.getY() + 14) {
			open = true;
			statusKey = null;
			return true;
		}
		if (!open) return false;
		if (!contains(mouseX, mouseY)) {
			open = false;
			return false;
		}
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && mouseY < y + 16) {
			dragging = true;
			dragOffsetX = (int) mouseX - x;
			dragOffsetY = (int) mouseY - y;
			return true;
		}
		if (hit(mouseX, mouseY, x + 8, y + 19, 125, 18)) {
			presets = false;
			scroll = 0;
			return true;
		}
		if (hit(mouseX, mouseY, x + 143, y + 19, 125, 18)) {
			presets = true;
			scroll = 0;
			return true;
		}
		if (searchField.mouseClicked(mouseX, mouseY, button)) return true;
		if (!presets && hit(mouseX, mouseY, x + 8, y + 68, 64, 18)) {
			openSaveDialog(chatField.getText());
			return true;
		}
		if (!presets && hit(mouseX, mouseY, x + 76, y + 68, 60, 18)) {
			favoritesOnly = !favoritesOnly;
			return true;
		}
		if (!presets && hit(mouseX, mouseY, x + 140, y + 68, 60, 18)) {
			recent = !recent;
			return true;
		}
		if (!presets && hit(mouseX, mouseY, x + 204, y + 68, 64, 18)) {
			cycleCategory();
			return true;
		}
		if (!presets && hit(mouseX, mouseY, x + 8, y + HEIGHT - 23, 90, 16)) {
			dialog = currentCategory().isBlank()
					? Dialog.CONFIRM_CLEAR_NON_FAVORITES : Dialog.CONFIRM_DELETE_CATEGORY;
			return true;
		}
		if (!presets && hit(mouseX, mouseY, x + WIDTH - 98, y + HEIGHT - 23, 90, 16)) {
			dialog = Dialog.CONFIRM_CLEAR_ALL;
			return true;
		}
		int rowIndex = ((int) mouseY - (y + 91)) / ROW_HEIGHT + scroll;
		if (rowIndex < 0) return true;
		if (presets) {
			List<CommandPresetRegistry.Preset> values = visiblePresets();
			if (rowIndex >= values.size()) return true;
			CommandPresetRegistry.Preset preset = values.get(rowIndex);
			if (mouseX >= x + WIDTH - 24) {
				openSaveDialog(preset.command());
			} else {
				insert(chatField, suggestor, preset.command(), Screen.hasShiftDown());
			}
			return true;
		}
		List<SavedCommand> values = filteredCommands();
		if (rowIndex >= values.size()) return true;
		SavedCommand command = values.get(rowIndex);
		int right = x + WIDTH - 8;
		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			MinecraftClient.getInstance().keyboard.setClipboard(command.command());
		} else if (mouseX >= right - 16) {
			dialog = Dialog.CONFIRM_DELETE;
			editing = command;
		} else if (mouseX >= right - 32) {
			openEditDialog(command);
		} else if (mouseX >= right - 48) {
			manager.toggleFavorite(command.id());
		} else if (mouseX >= right - 64) {
			manager.move(command.id(), 1);
		} else if (mouseX >= right - 80) {
			manager.move(command.id(), -1);
		} else {
			insert(chatField, suggestor, command.command(), Screen.hasShiftDown());
			manager.markUsed(command.id(), System.currentTimeMillis());
		}
		return true;
	}

	public boolean mouseDragged(ChatScreen screen, double mouseX, double mouseY, int button) {
		if (!dragging || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
		x = (int) mouseX - dragOffsetX;
		y = (int) mouseY - dragOffsetY;
		clamp(screen);
		return true;
	}

	public boolean mouseReleased(int button) {
		if (!dragging) return false;
		dragging = false;
		return button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
	}

	public boolean mouseScrolled(double amount) {
		if (!open || dialog != Dialog.NONE) return false;
		int size = presets ? visiblePresets().size() : filteredCommands().size();
		int max = Math.max(0, size - 5);
		scroll = Math.max(0, Math.min(max, scroll + (amount < 0 ? 1 : -1)));
		return true;
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers,
							  TextFieldWidget chatField, ChatInputSuggestor suggestor) {
		if (dialog != Dialog.NONE) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				closeDialog();
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				confirmDialog();
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_TAB
					&& (dialog == Dialog.SAVE || dialog == Dialog.EDIT)) {
				if (titleField.isFocused()) focusDialogField(categoryField);
				else if (categoryField.isFocused()) focusDialogField(commandField);
				else focusDialogField(titleField);
				return true;
			}
			return focusedDialogField().keyPressed(keyCode, scanCode, modifiers);
		}
		if (!open) return false;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			open = false;
			return true;
		}
		return searchField.keyPressed(keyCode, scanCode, modifiers);
	}

	public boolean charTyped(char chr, int modifiers) {
		if (dialog != Dialog.NONE) return focusedDialogField().charTyped(chr, modifiers);
		return open && searchField.charTyped(chr, modifiers);
	}

	public void tick() {
		manager.tick(System.currentTimeMillis());
	}

	public void removed() {
		manager.flush();
		open = false;
		dialog = Dialog.NONE;
		dragging = false;
		if (owner != null) ACTIVE.remove(owner);
		owner = null;
	}

	public void render(ChatScreen screen, TextFieldWidget chatField, DrawContext context,
					   int mouseX, int mouseY, float delta) {
		CommandClipboardConfig config = ChatCanvasConfig.instance().commandClipboard();
		if (!config.enabled()) return;
		if (config.showPanelButton() && !open) {
			int bx = buttonX(screen, chatField);
			fillButton(context, bx, chatField.getY(), 43, 14, open);
			context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
					Text.translatable("chat_canvas.command.button"), bx + 21,
					chatField.getY() + 3, 0xFFFFFF);
		}
		if (!open) return;
		clamp(screen);
		context.fill(x, y, x + WIDTH, y + HEIGHT, 0xF0181B25);
		context.drawBorder(x, y, WIDTH, HEIGHT, 0xFF59647A);
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		context.drawTextWithShadow(renderer, Text.translatable("chat_canvas.command.clipboard"),
				x + 8, y + 5, 0xFFFFFF);
		if (dialog != Dialog.NONE) {
			renderDialog(context, renderer, mouseX, mouseY, delta);
			return;
		}
		fillButton(context, x + 8, y + 19, 125, 18, !presets);
		fillButton(context, x + 143, y + 19, 125, 18, presets);
		context.drawCenteredTextWithShadow(renderer,
				Text.translatable("chat_canvas.command.mine"), x + 70, y + 24, 0xFFFFFF);
		context.drawCenteredTextWithShadow(renderer,
				Text.translatable("chat_canvas.command.presets"), x + 205, y + 24, 0xFFFFFF);
		searchField.setX(x + 8);
		searchField.setY(y + 43);
		searchField.setWidth(WIDTH - 16);
		searchField.render(context, mouseX, mouseY, delta);
		if (!presets) renderUserToolbar(context, renderer);
		List<?> rows = presets ? visiblePresets() : filteredCommands();
		for (int visible = 0; visible < 5; visible++) {
			int index = scroll + visible;
			if (index >= rows.size()) break;
			int rowY = y + 91 + visible * ROW_HEIGHT;
			context.fill(x + 8, rowY, x + WIDTH - 8, rowY + ROW_HEIGHT - 2,
					(mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 2)
							? 0xCC354157 : 0xAA252B39);
			if (presets) renderPreset(context, renderer,
					(CommandPresetRegistry.Preset) rows.get(index), rowY);
			else renderCommand(context, renderer, (SavedCommand) rows.get(index), rowY);
		}
		if (!presets) {
			fillButton(context, x + 8, y + HEIGHT - 23, 90, 16, false);
			fillButton(context, x + WIDTH - 98, y + HEIGHT - 23, 90, 16, false);
			context.drawCenteredTextWithShadow(renderer,
					Text.translatable(currentCategory().isBlank()
							? "chat_canvas.command.clear_non_favorites"
							: "chat_canvas.command.clear_category"),
					x + 53, y + HEIGHT - 19, 0xFFDDDDDD);
			context.drawCenteredTextWithShadow(renderer,
					Text.translatable("chat_canvas.command.clear_all"),
					x + WIDTH - 53, y + HEIGHT - 19, 0xFFFFB0B0);
		}
		if (statusKey != null) {
			context.drawTextWithShadow(renderer, Text.translatable(statusKey),
					x + 103, y + HEIGHT - 19, 0xFFFFCC66);
		}
	}

	private void renderUserToolbar(DrawContext context, TextRenderer renderer) {
		int toolbarY = y + 68;
		fillButton(context, x + 8, toolbarY, 64, 18, false);
		fillButton(context, x + 76, toolbarY, 60, 18, favoritesOnly);
		fillButton(context, x + 140, toolbarY, 60, 18, recent);
		fillButton(context, x + 204, toolbarY, 64, 18, categoryIndex != 0);
		context.drawCenteredTextWithShadow(renderer,
				Text.translatable("chat_canvas.command.save_current"), x + 40, toolbarY + 5, 0xFFFFFF);
		context.drawCenteredTextWithShadow(renderer,
				Text.translatable("chat_canvas.command.favorite"), x + 106, toolbarY + 5, 0xFFFFFF);
		context.drawCenteredTextWithShadow(renderer,
				Text.translatable("chat_canvas.command.recent"), x + 170, toolbarY + 5, 0xFFFFFF);
		String category = currentCategory();
		Text categoryText = category.isBlank()
				? Text.translatable("chat_canvas.command.all")
				: Text.literal(category);
		context.drawCenteredTextWithShadow(renderer, categoryText, x + 236, toolbarY + 5, 0xFFFFFF);
	}

	private void renderCommand(DrawContext context, TextRenderer renderer,
							   SavedCommand command, int rowY) {
		context.drawTextWithShadow(renderer, Text.literal(command.title()),
				x + 12, rowY + 3, 0xFFFFFFFF);
		context.drawTextWithShadow(renderer, Text.literal(shorten(command.command(), 27)),
				x + 12, rowY + 14, 0xFFB7BFCE);
		String actions = "↑ ↓ " + (command.favorite() ? "★" : "☆") + " E ×";
		context.drawTextWithShadow(renderer, Text.literal(actions),
				x + WIDTH - 87, rowY + 8, 0xFFFFD36A);
	}

	private void renderPreset(DrawContext context, TextRenderer renderer,
							  CommandPresetRegistry.Preset preset, int rowY) {
		context.drawTextWithShadow(renderer, Text.translatable(preset.titleKey()),
				x + 12, rowY + 3, 0xFFFFFFFF);
		context.drawTextWithShadow(renderer, Text.literal(shorten(preset.command(), 31)),
				x + 12, rowY + 14, 0xFFB7BFCE);
		context.drawTextWithShadow(renderer, Text.literal("+"),
				x + WIDTH - 22, rowY + 8, 0xFF7FE59A);
	}

	private void renderDialog(DrawContext context, TextRenderer renderer,
							  int mouseX, int mouseY, float delta) {
		int dx = x + 8;
		int dy = y + 20;
		int dialogWidth = WIDTH - 16;
		int dialogHeight = HEIGHT - 28;
		context.fill(x + 1, y + 18, x + WIDTH - 1, y + HEIGHT - 1, 0xFF10131B);
		context.fill(dx, dy, dx + dialogWidth, dy + dialogHeight, 0xFF171B25);
		context.drawBorder(dx, dy, dialogWidth, dialogHeight, 0xFF73809A);
		Text title = switch (dialog) {
			case SAVE -> Text.translatable("chat_canvas.command.save_current");
			case EDIT -> Text.translatable("chat_canvas.command.edit");
			case CONFIRM_SENSITIVE -> Text.translatable("chat_canvas.command.sensitive");
			case CONFIRM_DUPLICATE -> Text.translatable("chat_canvas.command.duplicate");
			case CONFIRM_DELETE -> Text.translatable("chat_canvas.command.delete");
			case CONFIRM_CLEAR_NON_FAVORITES -> Text.translatable(
					"chat_canvas.command.clear_non_favorites");
			case CONFIRM_DELETE_CATEGORY -> Text.translatable(
					"chat_canvas.command.clear_category");
			case CONFIRM_CLEAR_ALL -> Text.translatable("chat_canvas.command.clear_all");
			default -> Text.empty();
		};
		context.drawTextWithShadow(renderer, title, dx + 8, dy + 8, 0xFFFFFFFF);
		if (dialog == Dialog.SAVE || dialog == Dialog.EDIT) {
			titleField.setX(dx + 8);
			titleField.setY(dy + 29);
			titleField.setWidth(dialogWidth - 16);
			categoryField.setX(dx + 8);
			categoryField.setY(dy + 57);
			categoryField.setWidth(dialogWidth - 16);
			commandField.setX(dx + 8);
			commandField.setY(dy + 85);
			commandField.setWidth(dialogWidth - 16);
			context.drawTextWithShadow(renderer, Text.translatable("chat_canvas.command.name"),
					dx + 8, dy + 20, 0xFF9FAABD);
			context.drawTextWithShadow(renderer, Text.translatable("chat_canvas.command.category"),
					dx + 8, dy + 48, 0xFF9FAABD);
			context.drawTextWithShadow(renderer, Text.translatable("chat_canvas.command.command"),
					dx + 8, dy + 76, 0xFF9FAABD);
			titleField.render(context, mouseX, mouseY, delta);
			categoryField.render(context, mouseX, mouseY, delta);
			commandField.render(context, mouseX, mouseY, delta);
		} else {
			String warning = dialog == Dialog.CONFIRM_SENSITIVE
					? "chat_canvas.command.plaintext_warning"
					: dialog == Dialog.CONFIRM_DUPLICATE
					? "chat_canvas.command.update_existing"
					: dialog == Dialog.CONFIRM_CLEAR_ALL
					? "chat_canvas.command.cannot_restore"
					: "chat_canvas.command.confirm";
			context.drawTextWrapped(renderer, Text.translatable(warning),
					dx + 8, dy + 29, dialogWidth - 16, 0xFFFFB0B0);
		}
		fillButton(context, dx + 8, y + HEIGHT - 31, 94, 18, false);
		fillButton(context, x + WIDTH - 110, y + HEIGHT - 31, 94, 18, true);
		context.drawCenteredTextWithShadow(renderer, Text.translatable("gui.cancel"),
				dx + 55, y + HEIGHT - 26, 0xFFFFFF);
		context.drawCenteredTextWithShadow(renderer, Text.translatable("gui.ok"),
				x + WIDTH - 63, y + HEIGHT - 26, 0xFFFFFF);
	}

	private boolean dialogClick(double mouseX, double mouseY, int button) {
		int dx = x + 8;
		if (dialog == Dialog.SAVE || dialog == Dialog.EDIT) {
			if (titleField.mouseClicked(mouseX, mouseY, button)) {
				focusDialogField(titleField);
				return true;
			}
			if (categoryField.mouseClicked(mouseX, mouseY, button)) {
				focusDialogField(categoryField);
				return true;
			}
			if (commandField.mouseClicked(mouseX, mouseY, button)) {
				focusDialogField(commandField);
				return true;
			}
		}
		if (hit(mouseX, mouseY, dx + 8, y + HEIGHT - 31, 94, 18)) {
			closeDialog();
			return true;
		}
		if (hit(mouseX, mouseY, x + WIDTH - 110, y + HEIGHT - 31, 94, 18)) {
			confirmDialog();
			return true;
		}
		return true;
	}

	private void confirmDialog() {
		switch (dialog) {
			case SAVE -> saveFromDialog(false);
			case EDIT -> {
				if (editing != null && manager.edit(editing.id(), titleField.getText(),
						commandField.getText(), categoryField.getText())) closeDialog();
			}
			case CONFIRM_SENSITIVE -> saveFromDialog(true);
			case CONFIRM_DUPLICATE -> saveFromDialog(true, true);
			case CONFIRM_DELETE -> {
				if (editing != null) manager.delete(editing.id());
				closeDialog();
			}
			case CONFIRM_CLEAR_NON_FAVORITES -> {
				manager.clearNonFavorites();
				closeDialog();
			}
			case CONFIRM_DELETE_CATEGORY -> {
				manager.deleteCategory(currentCategory());
				categoryIndex = 0;
				closeDialog();
			}
			case CONFIRM_CLEAR_ALL -> {
				manager.clearAll();
				closeDialog();
			}
			default -> closeDialog();
		}
	}

	private void saveFromDialog(boolean sensitiveConfirmed) {
		saveFromDialog(sensitiveConfirmed, false);
	}

	private void saveFromDialog(boolean sensitiveConfirmed, boolean updateExisting) {
		if (dialog == Dialog.SAVE || dialog == Dialog.EDIT) {
			pendingCommand = commandField.getText().strip();
		}
		if (pendingCommand == null || !pendingCommand.strip().startsWith("/")) {
			statusKey = "chat_canvas.command.not_command";
			closeDialog();
			return;
		}
		CommandClipboardConfig config = ChatCanvasConfig.instance().commandClipboard();
		if (!sensitiveConfirmed && config.sensitiveWarning()
				&& SensitiveCommandDetector.isSensitive(pendingCommand)) {
			dialog = Dialog.CONFIRM_SENSITIVE;
			return;
		}
		CommandClipboardManager.AddResult result = manager.add(
				titleField.getText(), pendingCommand, categoryField.getText(), updateExisting);
		statusKey = switch (result) {
			case ADDED, UPDATED_EXISTING -> null;
			case DUPLICATE -> "chat_canvas.command.duplicate";
			case LIMIT_REACHED -> "chat_canvas.command.limit";
			case INVALID -> "chat_canvas.command.not_command";
			case SAVE_FAILED -> "chat_canvas.command.save_failed";
		};
		if (result == CommandClipboardManager.AddResult.ADDED
				|| result == CommandClipboardManager.AddResult.UPDATED_EXISTING) closeDialog();
		else if (result == CommandClipboardManager.AddResult.DUPLICATE) {
			dialog = Dialog.CONFIRM_DUPLICATE;
		}
	}

	private void openSaveDialog(String command) {
		String trimmed = command == null ? "" : command.strip();
		if (!trimmed.startsWith("/") || trimmed.length() < 2) {
			statusKey = "chat_canvas.command.not_command";
			return;
		}
		pendingCommand = trimmed;
		titleField.setText(defaultTitle(trimmed));
		categoryField.setText("");
		commandField.setText(trimmed);
		titleField.setFocused(true);
		categoryField.setFocused(false);
		commandField.setFocused(false);
		dialog = Dialog.SAVE;
	}

	private void openEditDialog(SavedCommand command) {
		editing = command;
		pendingCommand = command.command();
		titleField.setText(command.title());
		categoryField.setText(command.category());
		commandField.setText(command.command());
		titleField.setFocused(true);
		categoryField.setFocused(false);
		commandField.setFocused(false);
		dialog = Dialog.EDIT;
	}

	private void closeDialog() {
		dialog = Dialog.NONE;
		editing = null;
		pendingCommand = null;
		titleField.setFocused(false);
		categoryField.setFocused(false);
		commandField.setFocused(false);
	}

	private TextFieldWidget focusedDialogField() {
		if (commandField.isFocused()) return commandField;
		return categoryField.isFocused() ? categoryField : titleField;
	}

	private void focusDialogField(TextFieldWidget selected) {
		titleField.setFocused(selected == titleField);
		categoryField.setFocused(selected == categoryField);
		commandField.setFocused(selected == commandField);
	}

	private void insert(TextFieldWidget field, ChatInputSuggestor suggestor,
						String command, boolean opposite) {
		CommandInsertMode mode = ChatCanvasConfig.instance().commandClipboard().insertMode();
		if (opposite) mode = mode.opposite();
		ChatFieldActions.applyCommand(field, suggestor, command, mode);
	}

	private List<SavedCommand> filteredCommands() {
		String query = searchField == null ? "" : searchField.getText();
		String category = currentCategory();
		long revision = manager.revision();
		if (revision != cachedRevision || !query.equals(cachedQuery)
				|| !category.equals(cachedCategory) || favoritesOnly != cachedFavorites
				|| recent != cachedRecent) {
			cachedCommands = manager.search(query, category, favoritesOnly, recent);
			cachedRevision = revision;
			cachedQuery = query;
			cachedCategory = category;
			cachedFavorites = favoritesOnly;
			cachedRecent = recent;
			scroll = Math.min(scroll, Math.max(0, cachedCommands.size() - 5));
		}
		return cachedCommands;
	}

	private List<CommandPresetRegistry.Preset> visiblePresets() {
		String query = searchField == null ? "" : searchField.getText().strip()
				.toLowerCase(Locale.ROOT);
		var hidden = ChatCanvasConfig.instance().commandClipboard().hiddenPresetIds();
		return CommandPresetRegistry.all().stream()
				.filter(preset -> !hidden.contains(preset.id()))
				.filter(preset -> query.isEmpty()
						|| preset.command().toLowerCase(Locale.ROOT).contains(query)
						|| Text.translatable(preset.titleKey()).getString()
								.toLowerCase(Locale.ROOT).contains(query))
				.toList();
	}

	private void cycleCategory() {
		List<String> values = new ArrayList<>();
		values.add("");
		values.addAll(manager.categories());
		categoryIndex = (categoryIndex + 1) % Math.max(1, values.size());
		scroll = 0;
	}

	private String currentCategory() {
		List<String> values = new ArrayList<>();
		values.add("");
		values.addAll(manager.categories());
		if (categoryIndex >= values.size()) categoryIndex = 0;
		return values.get(categoryIndex);
	}

	private int buttonX(ChatScreen screen, TextFieldWidget field) {
		int right = field.getX() + field.getWidth() + 4;
		if (right + 43 <= screen.width - 2) return right;
		return Math.max(2, field.getX() - 47);
	}

	private void clamp(ChatScreen screen) {
		x = Math.max(4, Math.min(screen.width - WIDTH - 4, x));
		y = Math.max(4, Math.min(screen.height - HEIGHT - 20, y));
	}

	private boolean contains(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + WIDTH && mouseY >= y && mouseY < y + HEIGHT;
	}

	private static boolean hit(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private static void fillButton(DrawContext context, int x, int y, int width,
								   int height, boolean active) {
		context.fill(x, y, x + width, y + height, active ? 0xFF405C82 : 0xCC30394B);
		context.drawBorder(x, y, width, height, 0xFF5D6A82);
	}

	private static String shorten(String value, int limit) {
		if (value.length() <= limit) return value;
		return value.substring(0, Math.max(1, limit - 1)) + "…";
	}

	private static String defaultTitle(String command) {
		String token = command.substring(1);
		int space = token.indexOf(' ');
		return space < 0 ? token : token.substring(0, space);
	}

	private enum Dialog {
		NONE, SAVE, EDIT, CONFIRM_SENSITIVE, CONFIRM_DUPLICATE, CONFIRM_DELETE,
		CONFIRM_DELETE_CATEGORY, CONFIRM_CLEAR_NON_FAVORITES, CONFIRM_CLEAR_ALL
	}
}
