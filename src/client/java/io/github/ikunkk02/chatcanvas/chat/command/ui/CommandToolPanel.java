package io.github.ikunkk02.chatcanvas.chat.command.ui;

import io.github.ikunkk02.chatcanvas.chat.command.ClipboardCommandCandidate;
import io.github.ikunkk02.chatcanvas.chat.command.ClipboardCommandParseResult;
import io.github.ikunkk02.chatcanvas.chat.command.ClipboardCommandParser;
import io.github.ikunkk02.chatcanvas.chat.command.CommandHistoryEntry;
import io.github.ikunkk02.chatcanvas.chat.command.CommandTextSanitizer;
import io.github.ikunkk02.chatcanvas.chat.command.CommandToolManager;
import io.github.ikunkk02.chatcanvas.chat.command.CommandToolRuntime;
import io.github.ikunkk02.chatcanvas.chat.command.CommandToolTab;
import io.github.ikunkk02.chatcanvas.chat.command.DangerousCommandDetector;
import io.github.ikunkk02.chatcanvas.chat.command.FavoriteCommandEntry;
import io.github.ikunkk02.chatcanvas.chat.command.SensitiveCommandDetector;
import io.github.ikunkk02.chatcanvas.chat.interaction.ChatFieldActions;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.CommandClipboardConfig;
import io.github.ikunkk02.chatcanvas.config.CommandInsertMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.WeakHashMap;

public final class CommandToolPanel {
	private static final int WIDTH = 292;
	private static final int HEIGHT = 260;
	private static final int ROW_HEIGHT = 31;
	private static final int VISIBLE_ROWS = 5;
	private static final WeakHashMap<ChatScreen, CommandToolPanel> ACTIVE =
			new WeakHashMap<>();
	private static boolean openNextScreen;

	private final CommandToolManager manager = CommandToolRuntime.manager();
	private ChatScreen owner;
	private EditBox searchField;
	private EditBox nameField;
	private EditBox favoriteCommandField;
	private CommandToolTab tab = CommandToolTab.RECENT;
	private ClipboardCommandParseResult clipboard =
			new ClipboardCommandParseResult(List.of(), false, false);
	private boolean clipboardLoaded;
	private boolean open;
	private float openProgress;
	private int x;
	private int y;
	private int visualX;
	private int scroll;
	private int selected;
	private boolean listFocused;
	private boolean dragging;
	private UUID draggingFavorite;
	private int dragOffsetX;
	private int dragOffsetY;
	private Dialog dialog = Dialog.NONE;
	private Dialog pendingFavoriteDialog = Dialog.NONE;
	private UUID pendingId;
	private String statusKey;
	private long cachedRevision = Long.MIN_VALUE;
	private String cachedQuery = "";
	private CommandToolTab cachedTab;
	private List<Row> cachedRows = List.of();

	public void init(ChatScreen screen, EditBox commandField) {
		owner = screen;
		ACTIVE.put(screen, this);
		Font renderer = Minecraft.getInstance().font;
		searchField = new EditBox(renderer, 0, 0, WIDTH - 46, 18,
				Component.translatable("chat_canvas.command.search"));
		searchField.setHint(Component.translatable("chat_canvas.command.search"));
		searchField.setMaxLength(128);
		nameField = new EditBox(renderer, 0, 0, WIDTH - 32, 18,
				Component.translatable("chat_canvas.command.name"));
		nameField.setHint(Component.translatable("chat_canvas.command.name"));
		favoriteCommandField = new EditBox(renderer, 0, 0, WIDTH - 32, 18,
				Component.translatable("chat_canvas.command.command"));
		favoriteCommandField.setHint(Component.translatable(
				"chat_canvas.command.command"));
		favoriteCommandField.setMaxLength(CommandTextSanitizer.MAX_COMMAND_LENGTH);
		int rightSpace = screen.width - commandField.getX() - commandField.getWidth();
		x = rightSpace >= WIDTH + 8
				? commandField.getX() + commandField.getWidth() + 4
				: Math.max(4, commandField.getX() - WIDTH - 4);
		y = Math.max(4, commandField.getY() - HEIGHT - 4);
		clamp(screen);
		visualX = x;
		if (openNextScreen) {
			open = true;
			openProgress = 1.0f;
			openNextScreen = false;
		}
	}

	public static void requestOpenNextChatScreen() {
		openNextScreen = true;
	}

	public static boolean dispatchCharTyped(
			ChatScreen screen, char character, int modifiers) {
		CommandToolPanel panel = ACTIVE.get(screen);
		return panel != null && panel.charTyped(character, modifiers);
	}

	public static boolean dispatchMouseDragged(
			ChatScreen screen, double mouseX, double mouseY, int button) {
		CommandToolPanel panel = ACTIVE.get(screen);
		return panel != null
				&& panel.mouseDragged(screen, mouseX, mouseY, button);
	}

	public static boolean dispatchMouseReleased(ChatScreen screen, int button) {
		CommandToolPanel panel = ACTIVE.get(screen);
		return panel != null && panel.mouseReleased(button);
	}

	public void close() {
		open = false;
		dialog = Dialog.NONE;
		listFocused = false;
		setSearchFocused(false);
	}

	public boolean mouseClicked(
			ChatScreen screen,
			EditBox commandField,
			CommandSuggestions suggestor,
			double mouseX,
			double mouseY,
			int button
	) {
		CommandClipboardConfig config = ChatCanvasConfig.instance().commandClipboard();
		if (!config.enabled()) return false;
		int buttonX = buttonX(screen, commandField);
		if (config.showPanelButton() && !open
				&& hit(mouseX, mouseY, buttonX, commandField.getY(), 54, 14)) {
			open = true;
			statusKey = null;
			return true;
		}
		if (!open) return false;
		if (dialog != Dialog.NONE) return dialogClick(mouseX, mouseY, button);
		if (!contains(mouseX, mouseY)) {
			close();
			return false;
		}
		int px = visualX;
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && mouseY < y + 17) {
			dragging = true;
			dragOffsetX = (int) mouseX - x;
			dragOffsetY = (int) mouseY - y;
			return true;
		}
		for (int i = 0; i < CommandToolTab.values().length; i++) {
			if (hit(mouseX, mouseY, px + 8 + i * 92, y + 20, 88, 18)) {
				selectTab(CommandToolTab.values()[i]);
				return true;
			}
		}
		if (searchField.mouseClicked(mouseX, mouseY, button)) {
			setSearchFocused(true);
			listFocused = false;
			return true;
		}
		if (hit(mouseX, mouseY, px + WIDTH - 32, y + 43, 24, 18)) {
			if (tab == CommandToolTab.CLIPBOARD) refreshClipboard();
			else {
				searchField.setValue("");
				invalidateRows();
			}
			return true;
		}
		if (hit(mouseX, mouseY, px + 8, y + 66, 116, 18)) {
			if (tab == CommandToolTab.FAVORITES) openFavoriteDialog(
					null, commandField.getValue());
			else if (tab == CommandToolTab.RECENT) {
				openFavoriteDialog(null, commandField.getValue());
			} else refreshClipboard();
			return true;
		}
		if (tab == CommandToolTab.RECENT
				&& hit(mouseX, mouseY, px + WIDTH - 124, y + 66, 116, 18)) {
			dialog = Dialog.CONFIRM_CLEAR_RECENT;
			return true;
		}
		if (mouseY < y + 88
				|| mouseY >= y + 88 + VISIBLE_ROWS * ROW_HEIGHT) return true;
		int rowIndex = ((int) mouseY - (y + 88)) / ROW_HEIGHT + scroll;
		List<Row> rows = rows();
		if (rowIndex < 0 || rowIndex >= rows.size()) return true;
		selected = rowIndex;
		listFocused = true;
		setSearchFocused(false);
		Row row = rows.get(rowIndex);
		int right = px + WIDTH - 8;
		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			Minecraft.getInstance().keyboardHandler.setClipboard(row.command());
		} else if (tab == CommandToolTab.FAVORITES && mouseX >= right - 18) {
			pendingId = row.id();
			dialog = Dialog.CONFIRM_DELETE_FAVORITE;
		} else if (tab == CommandToolTab.RECENT && mouseX >= right - 18) {
			pendingId = row.id();
			dialog = Dialog.CONFIRM_DELETE_RECENT;
		} else if (tab == CommandToolTab.FAVORITES && mouseX >= right - 38) {
			favorite(row.id()).ifPresent(entry -> openFavoriteDialog(
					entry, entry.command()));
		} else if (tab == CommandToolTab.FAVORITES && mouseX >= right - 58) {
			if (searchField.getValue().isEmpty()) draggingFavorite = row.id();
		} else if (tab == CommandToolTab.FAVORITES && mouseX >= right - 78) {
			Minecraft.getInstance().keyboardHandler.setClipboard(row.command());
		} else if (tab == CommandToolTab.RECENT && mouseX >= right - 38) {
			openFavoriteDialog(null, row.command());
		} else if (tab == CommandToolTab.RECENT && mouseX >= right - 58) {
			Minecraft.getInstance().keyboardHandler.setClipboard(row.command());
		} else if (tab == CommandToolTab.CLIPBOARD && mouseX >= right - 20) {
			Minecraft.getInstance().keyboardHandler.setClipboard(row.command());
		} else {
			insert(commandField, suggestor, row.command(), Screen.hasShiftDown());
		}
		return true;
	}

	public boolean mouseDragged(
			ChatScreen screen, double mouseX, double mouseY, int button) {
		if (draggingFavorite != null
				&& button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			List<Row> values = rows();
			int current = -1;
			for (int i = 0; i < values.size(); i++) {
				if (draggingFavorite.equals(values.get(i).id())) {
					current = i;
					break;
				}
			}
			int target = Math.max(0, Math.min(values.size() - 1,
					((int) mouseY - (y + 88)) / ROW_HEIGHT + scroll));
			if (current >= 0 && target != current) {
				manager.moveFavorite(draggingFavorite,
						target > current ? 1 : -1, System.currentTimeMillis());
				selected = target;
				invalidateRows();
			}
			return true;
		}
		if (!dragging || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
		x = (int) mouseX - dragOffsetX;
		y = (int) mouseY - dragOffsetY;
		clamp(screen);
		return true;
	}

	public boolean mouseReleased(int button) {
		if (draggingFavorite != null) {
			draggingFavorite = null;
			return button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
		}
		if (!dragging) return false;
		dragging = false;
		return button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
	}

	public boolean mouseScrolled(double amount) {
		if (!open || dialog != Dialog.NONE) return false;
		int max = Math.max(0, rows().size() - VISIBLE_ROWS);
		scroll = Math.max(0, Math.min(max, scroll + (amount < 0 ? 1 : -1)));
		selected = Math.max(scroll,
				Math.min(selected, scroll + VISIBLE_ROWS - 1));
		return true;
	}

	public boolean keyPressed(
			int keyCode, int scanCode, int modifiers,
			EditBox commandField, CommandSuggestions suggestor) {
		if (!open) {
			if (keyCode == GLFW.GLFW_KEY_F && Screen.hasControlDown()) {
				open = true;
				setSearchFocused(true);
				return true;
			}
			return false;
		}
		if (dialog != Dialog.NONE) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				closeDialog();
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ENTER
					|| keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				confirmDialog();
				return true;
			}
			if (dialog == Dialog.EDIT_FAVORITE
					|| dialog == Dialog.ADD_FAVORITE) {
				if (keyCode == GLFW.GLFW_KEY_TAB) {
					focusDialogField(nameField.isFocused()
							? favoriteCommandField : nameField);
					return true;
				}
				return focusedDialogField().keyPressed(
						keyCode, scanCode, modifiers);
			}
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (!searchField.getValue().isEmpty()) {
				searchField.setValue("");
				invalidateRows();
			} else close();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_F && Screen.hasControlDown()) {
			setSearchFocused(true);
			listFocused = false;
			return true;
		}
		if (searchField.isFocused()) {
			if (keyCode == GLFW.GLFW_KEY_ENTER
					|| keyCode == GLFW.GLFW_KEY_KP_ENTER) {
				setSearchFocused(false);
				listFocused = true;
				return true;
			}
			return searchField.keyPressed(keyCode, scanCode, modifiers);
		}
		if (!listFocused) return false;
		if (keyCode == GLFW.GLFW_KEY_TAB
				|| keyCode == GLFW.GLFW_KEY_RIGHT
				|| keyCode == GLFW.GLFW_KEY_LEFT) {
			int direction = keyCode == GLFW.GLFW_KEY_LEFT ? -1 : 1;
			selectTab(tab.next(direction));
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
			List<Row> rows = rows();
			if (!rows.isEmpty()) {
				selected = Math.max(0, Math.min(rows.size() - 1,
						selected + (keyCode == GLFW.GLFW_KEY_UP ? -1 : 1)));
				if (selected < scroll) scroll = selected;
				if (selected >= scroll + VISIBLE_ROWS) {
					scroll = selected - VISIBLE_ROWS + 1;
				}
			}
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER
				|| keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			List<Row> rows = rows();
			if (!rows.isEmpty()) {
				insert(commandField, suggestor,
						rows.get(Math.min(selected, rows.size() - 1)).command(),
						Screen.hasShiftDown());
			}
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_DELETE) {
			List<Row> rows = rows();
			if (!rows.isEmpty() && tab != CommandToolTab.CLIPBOARD) {
				pendingId = rows.get(Math.min(selected, rows.size() - 1)).id();
				dialog = tab == CommandToolTab.RECENT
						? Dialog.CONFIRM_DELETE_RECENT
						: Dialog.CONFIRM_DELETE_FAVORITE;
			}
			return true;
		}
		return false;
	}

	public boolean charTyped(char character, int modifiers) {
		if (dialog == Dialog.ADD_FAVORITE
				|| dialog == Dialog.EDIT_FAVORITE) {
			return focusedDialogField().charTyped(character, modifiers);
		}
		return open && searchField.isFocused()
				&& searchField.charTyped(character, modifiers);
	}

	public void removed() {
		manager.flush();
		close();
		dragging = false;
		draggingFavorite = null;
		if (owner != null) ACTIVE.remove(owner);
		owner = null;
	}

	public void render(
			ChatScreen screen, EditBox commandField, GuiGraphicsExtractor context,
			int mouseX, int mouseY, float delta) {
		CommandClipboardConfig config = ChatCanvasConfig.instance().commandClipboard();
		if (!config.enabled()) return;
		float target = open ? 1.0f : 0.0f;
		openProgress += (target - openProgress)
				* Math.min(1.0f, Math.max(0.12f, delta * 0.35f));
		if (Math.abs(target - openProgress) < 0.01f) openProgress = target;
		if (config.showPanelButton() && !open && openProgress <= 0.01f) {
			int bx = buttonX(screen, commandField);
			fillButton(context, bx, commandField.getY(), 54, 14, false);
			context.drawCenteredString(
					Minecraft.getInstance().font,
					Component.translatable("chat_canvas.command.tools"),
					bx + 27, commandField.getY() + 3, 0xFFFFFF);
		}
		if (openProgress <= 0.01f) return;
		clamp(screen);
		visualX = x + Math.round((1.0f - openProgress)
				* (x < screen.width / 2 ? -24 : 24));
		int px = visualX;
		Font renderer = Minecraft.getInstance().font;
		context.fill(px, y, px + WIDTH, y + HEIGHT, 0xF0181B25);
		context.renderOutline(px, y, WIDTH, HEIGHT, 0xFF59647A);
		context.drawString(renderer,
				Component.translatable("chat_canvas.command.tools"),
				px + 8, y + 5, 0xFFFFFF);
		if (dialog != Dialog.NONE) {
			renderDialog(context, renderer, mouseX, mouseY, delta);
			return;
		}
		renderTabs(context, renderer, px);
		searchField.setX(px + 8);
		searchField.setY(y + 43);
		searchField.setWidth(WIDTH - 46);
		searchField.render(context, mouseX, mouseY, delta);
		fillButton(context, px + WIDTH - 32, y + 43, 24, 18, false);
		context.drawCenteredString(renderer,
				Component.literal(tab == CommandToolTab.CLIPBOARD ? "↻" : "×"),
				px + WIDTH - 20, y + 48, 0xFFFFFF);
		renderToolbar(context, renderer, px);
		List<Row> rows = rows();
		if (rows.isEmpty()) {
			context.drawCenteredString(renderer,
					Component.translatable(emptyStateKey()), px + WIDTH / 2,
					y + 139, 0xFFADB6C7);
		} else {
			for (int visible = 0; visible < VISIBLE_ROWS; visible++) {
				int index = scroll + visible;
				if (index >= rows.size()) break;
				renderRow(context, renderer, rows.get(index), index,
						y + 88 + visible * ROW_HEIGHT, mouseX, mouseY);
			}
		}
		if (statusKey != null) {
			context.drawString(renderer, Component.translatable(statusKey),
					px + 8, y + HEIGHT - 14, 0xFFFFCC66);
		} else if (tab == CommandToolTab.CLIPBOARD && clipboard.multipleLines()) {
			context.drawString(renderer,
					Component.translatable("chat_canvas.command.clipboard.multiple"),
					px + 8, y + HEIGHT - 14, 0xFFFFCC66);
		}
	}

	private void renderTabs(GuiGraphicsExtractor context, Font renderer, int px) {
		String[] keys = {
				"chat_canvas.command.tab.recent",
				"chat_canvas.command.tab.favorites",
				"chat_canvas.command.tab.clipboard"
		};
		for (int i = 0; i < keys.length; i++) {
			boolean active = tab.ordinal() == i;
			int tabX = px + 8 + i * 92;
			fillButton(context, tabX, y + 20, 88, 18, active);
			context.drawCenteredString(renderer,
					Component.translatable(keys[i]), tabX + 44, y + 25, 0xFFFFFF);
			if (active) context.fill(tabX + 6, y + 36, tabX + 82, y + 38,
					0xFF9CC8FF);
		}
	}

	private void renderToolbar(GuiGraphicsExtractor context, Font renderer, int px) {
		fillButton(context, px + 8, y + 66, 116, 18, false);
		String primary = switch (tab) {
			case RECENT, FAVORITES -> "chat_canvas.command.favorite_current";
			case CLIPBOARD -> "chat_canvas.command.clipboard.refresh";
		};
		context.drawCenteredString(renderer, Component.translatable(primary),
				px + 66, y + 71, 0xFFFFFF);
		if (tab == CommandToolTab.RECENT) {
			fillButton(context, px + WIDTH - 124, y + 66, 116, 18, false);
			context.drawCenteredString(renderer,
					Component.translatable("chat_canvas.command.clear_all"),
					px + WIDTH - 66, y + 71, 0xFFFFB0B0);
		}
	}

	private void renderRow(
			GuiGraphicsExtractor context, Font renderer, Row row, int index,
			int rowY, int mouseX, int mouseY) {
		int px = visualX;
		boolean hover = hit(mouseX, mouseY, px + 8, rowY,
				WIDTH - 16, ROW_HEIGHT - 2);
		boolean active = listFocused && index == selected;
		context.fill(px + 8, rowY, px + WIDTH - 8, rowY + ROW_HEIGHT - 2,
				active ? 0xDD40516B : hover ? 0xCC354157 : 0xAA252B39);
		context.drawString(renderer,
				Component.literal(shorten(row.title(), 30)), px + 12, rowY + 3,
				0xFFFFFFFF);
		int commandColor = row.dangerous() ? 0xFFFFB36A : 0xFFB7BFCE;
		context.drawString(renderer,
				Component.literal((row.dangerous() ? "⚠ " : "")
						+ shorten(row.command(), 34)),
				px + 12, rowY + 16, commandColor);
		String actions = switch (tab) {
			case RECENT -> "C  ☆  ×";
			case FAVORITES -> "C  ↕  E  ×";
			case CLIPBOARD -> "C";
		};
		if (!actions.isEmpty()) {
			context.drawString(renderer, Component.literal(actions),
					px + WIDTH - renderer.getWidth(actions) - 12,
					rowY + 7, 0xFFFFD36A);
		}
		if (hover && renderer.getWidth(row.command()) > WIDTH - 34) {
			context.drawTooltip(renderer, Component.literal(row.command()), mouseX, mouseY);
		} else if (hover && row.dangerous()) {
			context.drawTooltip(renderer,
					Component.translatable("chat_canvas.command.dangerous"),
					mouseX, mouseY);
		}
	}

	private void renderDialog(
			GuiGraphicsExtractor context, Font renderer,
			int mouseX, int mouseY, float delta) {
		int px = visualX;
		int dx = px + 8;
		int dy = y + 22;
		int dialogWidth = WIDTH - 16;
		context.fill(px + 1, y + 18, px + WIDTH - 1, y + HEIGHT - 1,
				0xFF10131B);
		context.fill(dx, dy, dx + dialogWidth, y + HEIGHT - 8, 0xFF171B25);
		context.renderOutline(dx, dy, dialogWidth, HEIGHT - 30, 0xFF73809A);
		context.drawString(renderer, Component.translatable(dialog.titleKey),
				dx + 8, dy + 8, 0xFFFFFFFF);
		if (dialog == Dialog.ADD_FAVORITE
				|| dialog == Dialog.EDIT_FAVORITE) {
			nameField.setX(dx + 8);
			nameField.setY(dy + 34);
			nameField.setWidth(dialogWidth - 16);
			favoriteCommandField.setX(dx + 8);
			favoriteCommandField.setY(dy + 76);
			favoriteCommandField.setWidth(dialogWidth - 16);
			context.drawString(renderer,
					Component.translatable("chat_canvas.command.name"),
					dx + 8, dy + 23, 0xFF9FAABD);
			context.drawString(renderer,
					Component.translatable("chat_canvas.command.command"),
					dx + 8, dy + 65, 0xFF9FAABD);
			nameField.render(context, mouseX, mouseY, delta);
			favoriteCommandField.render(context, mouseX, mouseY, delta);
		} else {
			context.drawTextWrapped(renderer,
					Component.translatable(dialog == Dialog.CONFIRM_SENSITIVE
							? "chat_canvas.command.plaintext_warning"
							: "chat_canvas.command.cannot_restore"),
					dx + 8, dy + 34, dialogWidth - 16, 0xFFFFB0B0);
		}
		fillButton(context, dx + 8, y + HEIGHT - 32, 100, 18, false);
		fillButton(context, px + WIDTH - 108, y + HEIGHT - 32, 100, 18, true);
		context.drawCenteredString(renderer, Component.translatable("gui.cancel"),
				dx + 58, y + HEIGHT - 27, 0xFFFFFF);
		context.drawCenteredString(renderer, Component.translatable("gui.ok"),
				px + WIDTH - 58, y + HEIGHT - 27, 0xFFFFFF);
	}

	private boolean dialogClick(double mouseX, double mouseY, int button) {
		int px = visualX;
		int dx = px + 8;
		int dy = y + 22;
		if (dialog == Dialog.ADD_FAVORITE
				|| dialog == Dialog.EDIT_FAVORITE) {
			if (nameField.mouseClicked(mouseX, mouseY, button)) {
				focusDialogField(nameField);
				return true;
			}
			if (favoriteCommandField.mouseClicked(mouseX, mouseY, button)) {
				focusDialogField(favoriteCommandField);
				return true;
			}
		}
		if (hit(mouseX, mouseY, dx + 8, y + HEIGHT - 32, 100, 18)) {
			closeDialog();
		} else if (hit(mouseX, mouseY,
				px + WIDTH - 108, y + HEIGHT - 32, 100, 18)) {
			confirmDialog();
		}
		return true;
	}

	private void confirmDialog() {
		long now = System.currentTimeMillis();
		switch (dialog) {
			case ADD_FAVORITE, EDIT_FAVORITE -> saveFavorite(false, now);
			case CONFIRM_SENSITIVE -> saveFavorite(true, now);
			case CONFIRM_DELETE_RECENT -> {
				manager.deleteRecent(pendingId, now);
				closeDialog();
			}
			case CONFIRM_DELETE_FAVORITE -> {
				manager.deleteFavorite(pendingId, now);
				closeDialog();
			}
			case CONFIRM_CLEAR_RECENT -> {
				manager.clearRecent(now);
				closeDialog();
			}
			default -> closeDialog();
		}
		invalidateRows();
	}

	private void openFavoriteDialog(
			FavoriteCommandEntry entry, String currentCommand) {
		String command = CommandTextSanitizer.normalizeCommand(currentCommand);
		if (entry == null && command.length() <= 1) {
			statusKey = "chat_canvas.command.not_command";
			return;
		}
		pendingId = entry == null ? null : entry.entryId();
		nameField.setValue(entry == null
				? CommandTextSanitizer.commandName(command) : entry.name());
		favoriteCommandField.setValue(entry == null ? command : entry.command());
		focusDialogField(nameField);
		dialog = entry == null ? Dialog.ADD_FAVORITE : Dialog.EDIT_FAVORITE;
	}

	private void saveFavorite(boolean sensitiveConfirmed, long now) {
		Dialog operation = dialog == Dialog.CONFIRM_SENSITIVE
				? pendingFavoriteDialog : dialog;
		if (!sensitiveConfirmed
				&& ChatCanvasConfig.instance().commandClipboard().sensitiveWarning()
				&& SensitiveCommandDetector.isSensitive(
						favoriteCommandField.getValue())) {
			pendingFavoriteDialog = operation;
			dialog = Dialog.CONFIRM_SENSITIVE;
			return;
		}
		CommandToolManager.MutationResult result =
				operation == Dialog.EDIT_FAVORITE
						? manager.editFavorite(pendingId, nameField.getValue(),
								favoriteCommandField.getValue(), now)
						: manager.addFavorite(nameField.getValue(),
								favoriteCommandField.getValue(), now);
		statusKey = switch (result) {
			case INVALID -> "chat_canvas.command.not_command";
			case LIMIT_REACHED -> "chat_canvas.command.limit";
			case UNCHANGED -> "chat_canvas.command.duplicate";
			default -> null;
		};
		if (result == CommandToolManager.MutationResult.CHANGED) closeDialog();
		else dialog = operation;
	}

	private void closeDialog() {
		dialog = Dialog.NONE;
		pendingFavoriteDialog = Dialog.NONE;
		pendingId = null;
		nameField.setFocused(false);
		favoriteCommandField.setFocused(false);
	}

	private EditBox focusedDialogField() {
		return favoriteCommandField.isFocused()
				? favoriteCommandField : nameField;
	}

	private void focusDialogField(EditBox selected) {
		nameField.setFocused(selected == nameField);
		favoriteCommandField.setFocused(selected == favoriteCommandField);
	}

	private void selectTab(CommandToolTab value) {
		tab = value;
		scroll = 0;
		selected = 0;
		listFocused = true;
		setSearchFocused(false);
		if (tab == CommandToolTab.CLIPBOARD && !clipboardLoaded) {
			refreshClipboard();
		}
		invalidateRows();
	}

	private void refreshClipboard() {
		try {
			String text = Minecraft.getInstance().keyboardHandler.getClipboard();
			clipboard = ClipboardCommandParser.parse(text);
			clipboardLoaded = true;
			statusKey = null;
		} catch (RuntimeException failure) {
			clipboard = new ClipboardCommandParseResult(List.of(), false, false);
			clipboardLoaded = true;
			statusKey = "chat_canvas.command.clipboard.read_failed";
			CommandToolRuntime.reportToolError("无法读取系统剪贴板", failure);
		}
		invalidateRows();
	}

	private void insert(
			EditBox field, CommandSuggestions suggestor,
			String command, boolean opposite) {
		CommandInsertMode mode =
				ChatCanvasConfig.instance().commandClipboard().insertMode();
		if (opposite) mode = mode.opposite();
		ChatFieldActions.applyCommand(
				field, suggestor,
				CommandTextSanitizer.normalizeCommand(command), mode);
		listFocused = false;
	}

	private List<Row> rows() {
		String query = searchField == null ? "" : searchField.getValue();
		long revision = manager.revision();
		if (cachedTab == tab && cachedRevision == revision
				&& cachedQuery.equals(query)) return cachedRows;
		List<Row> values = new ArrayList<>();
		switch (tab) {
			case RECENT -> manager.searchRecent(query).forEach(entry ->
					values.add(new Row(entry.entryId(),
							CommandTextSanitizer.commandName(entry.command()),
							entry.command(),
							DangerousCommandDetector.mayChangeWorldOrPlayers(
									entry.command()))));
			case FAVORITES -> manager.searchFavorites(query).forEach(entry ->
					values.add(new Row(entry.entryId(), entry.name(), entry.command(),
							DangerousCommandDetector.mayChangeWorldOrPlayers(
									entry.command()))));
			case CLIPBOARD -> {
				String needle = query.strip().toLowerCase(Locale.ROOT);
				for (ClipboardCommandCandidate candidate : clipboard.candidates()) {
					if (!needle.isEmpty()
							&& !candidate.command().toLowerCase(Locale.ROOT)
							.contains(needle)) continue;
					String title = CommandTextSanitizer.commandName(
							candidate.command());
					if (!candidate.hadLeadingSlash()) {
						title = Component.translatable(
								"chat_canvas.command.clipboard.as_command",
								title).getString();
					}
					values.add(new Row(null, title,
							candidate.command(),
							DangerousCommandDetector.mayChangeWorldOrPlayers(
									candidate.command())));
				}
			}
		}
		cachedRows = List.copyOf(values);
		cachedRevision = revision;
		cachedQuery = query;
		cachedTab = tab;
		scroll = Math.min(scroll, Math.max(0, cachedRows.size() - VISIBLE_ROWS));
		selected = Math.max(0, Math.min(selected,
				Math.max(0, cachedRows.size() - 1)));
		return cachedRows;
	}

	private java.util.Optional<FavoriteCommandEntry> favorite(UUID id) {
		return manager.favorites().stream()
				.filter(entry -> entry.entryId().equals(id)).findFirst();
	}

	private void invalidateRows() {
		cachedRevision = Long.MIN_VALUE;
	}

	private void setSearchFocused(boolean focused) {
		if (searchField != null) searchField.setFocused(focused);
	}

	private String emptyStateKey() {
		if (!searchField.getValue().isEmpty()) {
			return "chat_canvas.command.search.empty";
		}
		return switch (tab) {
			case RECENT -> "chat_canvas.command.recent.empty";
			case FAVORITES -> "chat_canvas.command.favorites.empty";
			case CLIPBOARD -> "chat_canvas.command.clipboard.empty";
		};
	}

	private int buttonX(ChatScreen screen, EditBox field) {
		int right = field.getX() + field.getWidth() + 4;
		if (right + 54 <= screen.width - 2) return right;
		return Math.max(2, field.getX() - 58);
	}

	private void clamp(ChatScreen screen) {
		x = Math.max(4, Math.min(Math.max(4, screen.width - WIDTH - 4), x));
		y = Math.max(4, Math.min(Math.max(4, screen.height - HEIGHT - 20), y));
	}

	private boolean contains(double mouseX, double mouseY) {
		return mouseX >= visualX && mouseX < visualX + WIDTH
				&& mouseY >= y && mouseY < y + HEIGHT;
	}

	private static boolean hit(
			double mouseX, double mouseY,
			int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width
				&& mouseY >= y && mouseY < y + height;
	}

	private static void fillButton(
			GuiGraphicsExtractor context, int x, int y,
			int width, int height, boolean active) {
		context.fill(x, y, x + width, y + height,
				active ? 0xFF405C82 : 0xCC30394B);
		context.renderOutline(x, y, width, height, 0xFF5D6A82);
	}

	private static String shorten(String value, int limit) {
		if (value.length() <= limit) return value;
		return value.substring(0, Math.max(1, limit - 1)) + "…";
	}

	private record Row(UUID id, String title, String command, boolean dangerous) {
	}

	private enum Dialog {
		NONE(""),
		ADD_FAVORITE("chat_canvas.command.favorite.add"),
		EDIT_FAVORITE("chat_canvas.command.favorite.edit"),
		CONFIRM_SENSITIVE("chat_canvas.command.sensitive"),
		CONFIRM_DELETE_RECENT("chat_canvas.command.delete"),
		CONFIRM_DELETE_FAVORITE("chat_canvas.command.delete"),
		CONFIRM_CLEAR_RECENT("chat_canvas.command.clear_all");

		private final String titleKey;

		Dialog(String titleKey) {
			this.titleKey = titleKey;
		}
	}
}
