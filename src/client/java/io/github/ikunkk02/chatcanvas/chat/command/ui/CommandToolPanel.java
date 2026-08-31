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
import io.github.ikunkk02.chatcanvas.ui.ModernUiTheme;
import io.github.ikunkk02.chatcanvas.ui.UiLayoutMetrics;
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

public final class CommandToolPanel {
	private static final int ROW_HEIGHT = 31;
	private static final WeakHashMap<ChatScreen, CommandToolPanel> ACTIVE =
			new WeakHashMap<>();
	private static boolean openNextScreen;

	private final CommandToolManager manager = CommandToolRuntime.manager();
	private ChatScreen owner;
	private TextFieldWidget searchField;
	private TextFieldWidget nameField;
	private TextFieldWidget favoriteCommandField;
	private CommandToolTab tab = CommandToolTab.RECENT;
	private ClipboardCommandParseResult clipboard =
			new ClipboardCommandParseResult(List.of(), false, false);
	private boolean clipboardLoaded;
	private boolean open;
	private float openProgress;
	private int x;
	private int y;
	private int visualX;
	private int panelWidth = 292;
	private int panelHeight = 260;
	private int visibleRows = 5;
	private UiLayoutMetrics.CommandPanel layoutMetrics =
			new UiLayoutMetrics.CommandPanel(panelWidth, panelHeight, visibleRows);
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

	public void init(ChatScreen screen, TextFieldWidget commandField) {
		owner = screen;
		ACTIVE.put(screen, this);
		UiLayoutMetrics.CommandPanel metrics = UiLayoutMetrics.commandPanel(
				screen.width, screen.height);
		layoutMetrics = metrics;
		panelWidth = metrics.width();
		panelHeight = metrics.height();
		visibleRows = metrics.visibleRows();
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		searchField = new TextFieldWidget(renderer, 0, 0, panelWidth - 46, 18,
				Text.translatable("chat_canvas.command.search"));
		searchField.setPlaceholder(Text.translatable("chat_canvas.command.search"));
		searchField.setMaxLength(128);
		nameField = new TextFieldWidget(renderer, 0, 0, panelWidth - 32, 18,
				Text.translatable("chat_canvas.command.name"));
		nameField.setPlaceholder(Text.translatable("chat_canvas.command.name"));
		favoriteCommandField = new TextFieldWidget(renderer, 0, 0, panelWidth - 32, 18,
				Text.translatable("chat_canvas.command.command"));
		favoriteCommandField.setPlaceholder(Text.translatable(
				"chat_canvas.command.command"));
		favoriteCommandField.setMaxLength(CommandTextSanitizer.MAX_COMMAND_LENGTH);
		int rightSpace = screen.width - commandField.getX() - commandField.getWidth();
		x = rightSpace >= panelWidth + 8
				? commandField.getX() + commandField.getWidth() + 4
				: Math.max(4, commandField.getX() - panelWidth - 4);
		y = Math.max(4, commandField.getY() - panelHeight - 4);
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
			TextFieldWidget commandField,
			ChatInputSuggestor suggestor,
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
			if (hit(mouseX, mouseY, tabX(px, i), y + 20, tabWidth(), 18)) {
				selectTab(CommandToolTab.values()[i]);
				return true;
			}
		}
		if (searchField.mouseClicked(new net.minecraft.client.gui.Click(
				mouseX, mouseY, new net.minecraft.client.input.MouseInput(button, 0)), false)) {
			setSearchFocused(true);
			listFocused = false;
			return true;
		}
		if (hit(mouseX, mouseY, px + panelWidth - 32, y + 43, 24, 18)) {
			if (tab == CommandToolTab.CLIPBOARD) refreshClipboard();
			else {
				searchField.setText("");
				invalidateRows();
			}
			return true;
		}
		if (hit(mouseX, mouseY, px + 8, y + 66, toolbarPrimaryWidth(), 18)) {
			if (tab == CommandToolTab.FAVORITES) openFavoriteDialog(
					null, commandField.getText());
			else if (tab == CommandToolTab.RECENT) {
				openFavoriteDialog(null, commandField.getText());
			} else refreshClipboard();
			return true;
		}
		if (tab == CommandToolTab.RECENT
				&& hit(mouseX, mouseY, toolbarSecondaryX(px), y + 66,
						toolbarSecondaryWidth(), 18)) {
			dialog = Dialog.CONFIRM_CLEAR_RECENT;
			return true;
		}
		if (mouseY < y + 88
				|| mouseY >= y + 88 + visibleRows * ROW_HEIGHT) return true;
		int rowIndex = ((int) mouseY - (y + 88)) / ROW_HEIGHT + scroll;
		List<Row> rows = rows();
		if (rowIndex < 0 || rowIndex >= rows.size()) return true;
		selected = rowIndex;
		listFocused = true;
		setSearchFocused(false);
		Row row = rows.get(rowIndex);
		int right = px + panelWidth - 8;
		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			MinecraftClient.getInstance().keyboard.setClipboard(row.command());
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
			if (searchField.getText().isEmpty()) draggingFavorite = row.id();
		} else if (tab == CommandToolTab.FAVORITES && mouseX >= right - 78) {
			MinecraftClient.getInstance().keyboard.setClipboard(row.command());
		} else if (tab == CommandToolTab.RECENT && mouseX >= right - 38) {
			openFavoriteDialog(null, row.command());
		} else if (tab == CommandToolTab.RECENT && mouseX >= right - 58) {
			MinecraftClient.getInstance().keyboard.setClipboard(row.command());
		} else if (tab == CommandToolTab.CLIPBOARD && mouseX >= right - 20) {
			MinecraftClient.getInstance().keyboard.setClipboard(row.command());
		} else {
			insert(commandField, suggestor, row.command(), MinecraftClient.getInstance().isShiftPressed());
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
		int max = Math.max(0, rows().size() - visibleRows);
		scroll = Math.max(0, Math.min(max, scroll + (amount < 0 ? 1 : -1)));
		selected = Math.max(scroll,
				Math.min(selected, scroll + visibleRows - 1));
		return true;
	}

	public boolean keyPressed(
			int keyCode, int scanCode, int modifiers,
			TextFieldWidget commandField, ChatInputSuggestor suggestor) {
		if (!open) {
			if (keyCode == GLFW.GLFW_KEY_F && MinecraftClient.getInstance().isCtrlPressed()) {
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
				return focusedDialogField().keyPressed(new net.minecraft.client.input.KeyInput(
						keyCode, scanCode, modifiers));
			}
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (!searchField.getText().isEmpty()) {
				searchField.setText("");
				invalidateRows();
			} else close();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_F && MinecraftClient.getInstance().isCtrlPressed()) {
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
			return searchField.keyPressed(new net.minecraft.client.input.KeyInput(
					keyCode, scanCode, modifiers));
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
				if (selected >= scroll + visibleRows) {
					scroll = selected - visibleRows + 1;
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
						MinecraftClient.getInstance().isShiftPressed());
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
			return focusedDialogField().charTyped(new net.minecraft.client.input.CharInput(
					character, modifiers));
		}
		return open && searchField.isFocused()
				&& searchField.charTyped(new net.minecraft.client.input.CharInput(
						character, modifiers));
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
			ChatScreen screen, TextFieldWidget commandField, DrawContext context,
			int mouseX, int mouseY, float delta) {
		CommandClipboardConfig config = ChatCanvasConfig.instance().commandClipboard();
		if (!config.enabled()) return;
		float target = open ? 1.0f : 0.0f;
		openProgress += (target - openProgress)
				* Math.min(1.0f, Math.max(0.12f, delta * 0.35f));
		if (Math.abs(target - openProgress) < 0.01f) openProgress = target;
		if (config.showPanelButton() && !open && openProgress <= 0.01f) {
			int bx = buttonX(screen, commandField);
			fillButton(context, bx, commandField.getY(), 54, 14,
					mouseX, mouseY, false);
			context.drawCenteredTextWithShadow(
					MinecraftClient.getInstance().textRenderer,
					Text.translatable("chat_canvas.command.tools"),
					bx + 27, commandField.getY() + 3, ModernUiTheme.TEXT_PRIMARY);
		}
		if (openProgress <= 0.01f) return;
		clamp(screen);
		visualX = x + Math.round((1.0f - openProgress)
				* (x < screen.width / 2 ? -24 : 24));
		int px = visualX;
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		ModernUiTheme.drawFixedPanel(context, px, y, panelWidth, panelHeight, true);
		context.drawTextWithShadow(renderer,
				Text.translatable("chat_canvas.command.tools"),
				px + 8, y + 5, ModernUiTheme.TEXT_PRIMARY);
		if (dialog != Dialog.NONE) {
			renderDialog(context, renderer, mouseX, mouseY, delta);
			return;
		}
		renderTabs(context, renderer, px, mouseX, mouseY);
		searchField.setX(px + 8);
		searchField.setY(y + 43);
		searchField.setWidth(panelWidth - 46);
		searchField.render(context, mouseX, mouseY, delta);
		fillButton(context, px + panelWidth - 32, y + 43, 24, 18,
				mouseX, mouseY, false);
		context.drawCenteredTextWithShadow(renderer,
				Text.literal(tab == CommandToolTab.CLIPBOARD ? "↻" : "×"),
				px + panelWidth - 20, y + 48, ModernUiTheme.TEXT_PRIMARY);
		renderToolbar(context, renderer, px, mouseX, mouseY);
		List<Row> rows = rows();
		if (rows.isEmpty()) {
			context.drawCenteredTextWithShadow(renderer,
					Text.translatable(emptyStateKey()), px + panelWidth / 2,
					y + 88 + visibleRows * ROW_HEIGHT / 2,
					ModernUiTheme.TEXT_SECONDARY);
		} else {
			for (int visible = 0; visible < visibleRows; visible++) {
				int index = scroll + visible;
				if (index >= rows.size()) break;
				renderRow(context, renderer, rows.get(index), index,
						y + 88 + visible * ROW_HEIGHT, mouseX, mouseY);
			}
		}
		if (statusKey != null) {
			context.drawTextWithShadow(renderer, Text.translatable(statusKey),
					px + 8, y + panelHeight - 14, ModernUiTheme.WARNING);
		} else if (tab == CommandToolTab.CLIPBOARD && clipboard.multipleLines()) {
			context.drawTextWithShadow(renderer,
					Text.translatable("chat_canvas.command.clipboard.multiple"),
					px + 8, y + panelHeight - 14, ModernUiTheme.WARNING);
		}
	}

	private void renderTabs(DrawContext context, TextRenderer renderer, int px,
			int mouseX, int mouseY) {
		String[] keys = {
				"chat_canvas.command.tab.recent",
				"chat_canvas.command.tab.favorites",
				"chat_canvas.command.tab.clipboard"
		};
		for (int i = 0; i < keys.length; i++) {
			boolean active = tab.ordinal() == i;
			int tabX = tabX(px, i);
			int tabWidth = tabWidth();
			fillButton(context, tabX, y + 20, tabWidth, 18,
					mouseX, mouseY, active);
			String fitted = ModernUiTheme.fitText(renderer,
					Text.translatable(keys[i]), Math.max(1, tabWidth - 6));
			context.drawCenteredTextWithShadow(renderer,
					Text.literal(fitted), tabX + tabWidth / 2, y + 25,
					ModernUiTheme.TEXT_PRIMARY);
			if (active) context.fill(tabX + 3, y + 36,
					tabX + tabWidth - 3, y + 38, ModernUiTheme.ACCENT);
		}
	}

	private void renderToolbar(DrawContext context, TextRenderer renderer, int px,
			int mouseX, int mouseY) {
		int primaryWidth = toolbarPrimaryWidth();
		fillButton(context, px + 8, y + 66, primaryWidth, 18,
				mouseX, mouseY, false);
		String primary = switch (tab) {
			case RECENT, FAVORITES -> "chat_canvas.command.favorite_current";
			case CLIPBOARD -> "chat_canvas.command.clipboard.refresh";
		};
		String fittedPrimary = ModernUiTheme.fitText(renderer,
				Text.translatable(primary), Math.max(1, primaryWidth - 6));
		context.drawCenteredTextWithShadow(renderer, Text.literal(fittedPrimary),
				px + 8 + primaryWidth / 2, y + 71, ModernUiTheme.TEXT_PRIMARY);
		if (tab == CommandToolTab.RECENT) {
			int secondaryX = toolbarSecondaryX(px);
			int secondaryWidth = toolbarSecondaryWidth();
			fillButton(context, secondaryX, y + 66, secondaryWidth, 18,
					mouseX, mouseY, false);
			String fittedSecondary = ModernUiTheme.fitText(renderer,
					Text.translatable("chat_canvas.command.clear_all"),
					Math.max(1, secondaryWidth - 6));
			context.drawCenteredTextWithShadow(renderer,
					Text.literal(fittedSecondary), secondaryX + secondaryWidth / 2,
					y + 71, ModernUiTheme.DANGER);
		}
	}

	private void renderRow(
			DrawContext context, TextRenderer renderer, Row row, int index,
			int rowY, int mouseX, int mouseY) {
		int px = visualX;
		boolean hover = hit(mouseX, mouseY, px + 8, rowY,
				panelWidth - 16, ROW_HEIGHT - 2);
		boolean active = listFocused && index == selected;
		ModernUiTheme.drawFixedControl(context, px + 8, rowY,
				panelWidth - 16, ROW_HEIGHT - 2, hover, active, true);
		context.drawTextWithShadow(renderer,
				Text.literal(shorten(row.title(), 30)), px + 12, rowY + 3,
				ModernUiTheme.TEXT_PRIMARY);
		int commandColor = row.dangerous() ? ModernUiTheme.WARNING : ModernUiTheme.TEXT_SECONDARY;
		context.drawTextWithShadow(renderer,
				Text.literal((row.dangerous() ? "⚠ " : "")
						+ shorten(row.command(), 34)),
				px + 12, rowY + 16, commandColor);
		String actions = switch (tab) {
			case RECENT -> "C  ☆  ×";
			case FAVORITES -> "C  ↕  E  ×";
			case CLIPBOARD -> "C";
		};
		if (!actions.isEmpty()) {
			context.drawTextWithShadow(renderer, Text.literal(actions),
					px + panelWidth - renderer.getWidth(actions) - 12,
					rowY + 7, ModernUiTheme.ACCENT);
		}
		if (hover && renderer.getWidth(row.command()) > panelWidth - 34) {
			context.drawTooltip(renderer, Text.literal(row.command()), mouseX, mouseY);
		} else if (hover && row.dangerous()) {
			context.drawTooltip(renderer,
					Text.translatable("chat_canvas.command.dangerous"),
					mouseX, mouseY);
		}
	}

	private void renderDialog(
			DrawContext context, TextRenderer renderer,
			int mouseX, int mouseY, float delta) {
		int px = visualX;
		int dx = px + 8;
		int dy = y + 22;
		int dialogWidth = panelWidth - 16;
		context.fill(px + 1, y + 18, px + panelWidth - 1, y + panelHeight - 1,
				ModernUiTheme.PANEL_BACKGROUND);
		ModernUiTheme.drawFixedPanel(context, dx, dy, dialogWidth,
				panelHeight - 30, false);
		context.drawTextWithShadow(renderer, Text.translatable(dialog.titleKey),
				dx + 8, dy + 8, ModernUiTheme.TEXT_PRIMARY);
		if (dialog == Dialog.ADD_FAVORITE
				|| dialog == Dialog.EDIT_FAVORITE) {
			nameField.setX(dx + 8);
			nameField.setY(dy + 34);
			nameField.setWidth(dialogWidth - 16);
			favoriteCommandField.setX(dx + 8);
			favoriteCommandField.setY(dy + 76);
			favoriteCommandField.setWidth(dialogWidth - 16);
			context.drawTextWithShadow(renderer,
					Text.translatable("chat_canvas.command.name"),
					dx + 8, dy + 23, ModernUiTheme.TEXT_SECONDARY);
			context.drawTextWithShadow(renderer,
					Text.translatable("chat_canvas.command.command"),
					dx + 8, dy + 65, ModernUiTheme.TEXT_SECONDARY);
			nameField.render(context, mouseX, mouseY, delta);
			favoriteCommandField.render(context, mouseX, mouseY, delta);
		} else {
			context.drawWrappedTextWithShadow(renderer,
					Text.translatable(dialog == Dialog.CONFIRM_SENSITIVE
							? "chat_canvas.command.plaintext_warning"
							: "chat_canvas.command.cannot_restore"),
					dx + 8, dy + 34, dialogWidth - 16, ModernUiTheme.DANGER);
		}
		int buttonWidth = dialogButtonWidth();
		int cancelX = dialogCancelX(px);
		int confirmX = dialogConfirmX(px);
		fillButton(context, cancelX, y + panelHeight - 32, buttonWidth, 18,
				mouseX, mouseY, false);
		fillButton(context, confirmX, y + panelHeight - 32, buttonWidth, 18,
				mouseX, mouseY, true);
		context.drawCenteredTextWithShadow(renderer, Text.translatable("gui.cancel"),
				cancelX + buttonWidth / 2, y + panelHeight - 27,
				ModernUiTheme.TEXT_PRIMARY);
		context.drawCenteredTextWithShadow(renderer, Text.translatable("gui.ok"),
				confirmX + buttonWidth / 2, y + panelHeight - 27,
				ModernUiTheme.TEXT_PRIMARY);
	}

	private boolean dialogClick(double mouseX, double mouseY, int button) {
		int px = visualX;
		int dx = px + 8;
		int dy = y + 22;
		if (dialog == Dialog.ADD_FAVORITE
				|| dialog == Dialog.EDIT_FAVORITE) {
			if (nameField.mouseClicked(new net.minecraft.client.gui.Click(
					mouseX, mouseY, new net.minecraft.client.input.MouseInput(button, 0)), false)) {
				focusDialogField(nameField);
				return true;
			}
			if (favoriteCommandField.mouseClicked(new net.minecraft.client.gui.Click(
					mouseX, mouseY, new net.minecraft.client.input.MouseInput(button, 0)), false)) {
				focusDialogField(favoriteCommandField);
				return true;
			}
		}
		if (hit(mouseX, mouseY, dialogCancelX(px), y + panelHeight - 32,
				dialogButtonWidth(), 18)) {
			closeDialog();
		} else if (hit(mouseX, mouseY,
				dialogConfirmX(px), y + panelHeight - 32,
				dialogButtonWidth(), 18)) {
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
		nameField.setText(entry == null
				? CommandTextSanitizer.commandName(command) : entry.name());
		favoriteCommandField.setText(entry == null ? command : entry.command());
		focusDialogField(nameField);
		dialog = entry == null ? Dialog.ADD_FAVORITE : Dialog.EDIT_FAVORITE;
	}

	private void saveFavorite(boolean sensitiveConfirmed, long now) {
		Dialog operation = dialog == Dialog.CONFIRM_SENSITIVE
				? pendingFavoriteDialog : dialog;
		if (!sensitiveConfirmed
				&& ChatCanvasConfig.instance().commandClipboard().sensitiveWarning()
				&& SensitiveCommandDetector.isSensitive(
						favoriteCommandField.getText())) {
			pendingFavoriteDialog = operation;
			dialog = Dialog.CONFIRM_SENSITIVE;
			return;
		}
		CommandToolManager.MutationResult result =
				operation == Dialog.EDIT_FAVORITE
						? manager.editFavorite(pendingId, nameField.getText(),
								favoriteCommandField.getText(), now)
						: manager.addFavorite(nameField.getText(),
								favoriteCommandField.getText(), now);
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

	private TextFieldWidget focusedDialogField() {
		return favoriteCommandField.isFocused()
				? favoriteCommandField : nameField;
	}

	private void focusDialogField(TextFieldWidget selected) {
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
			String text = MinecraftClient.getInstance().keyboard.getClipboard();
			clipboard = ClipboardCommandParser.parse(text);
			clipboardLoaded = true;
			statusKey = null;
		} catch (RuntimeException failure) {
			clipboard = new ClipboardCommandParseResult(List.of(), false, false);
			clipboardLoaded = true;
			statusKey = "chat_canvas.command.clipboard.read_failed";
			CommandToolRuntime.reportToolError(
					"chat_canvas.command.clipboard.read_failed", failure);
		}
		invalidateRows();
	}

	private void insert(
			TextFieldWidget field, ChatInputSuggestor suggestor,
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
		String query = searchField == null ? "" : searchField.getText();
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
						title = Text.translatable(
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
		scroll = Math.min(scroll, Math.max(0, cachedRows.size() - visibleRows));
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
		if (!searchField.getText().isEmpty()) {
			return "chat_canvas.command.search.empty";
		}
		return switch (tab) {
			case RECENT -> "chat_canvas.command.recent.empty";
			case FAVORITES -> "chat_canvas.command.favorites.empty";
			case CLIPBOARD -> "chat_canvas.command.clipboard.empty";
		};
	}

	private int buttonX(ChatScreen screen, TextFieldWidget field) {
		int right = field.getX() + field.getWidth() + 4;
		if (right + 54 <= screen.width - 2) return right;
		return Math.max(2, field.getX() - 58);
	}

	private void clamp(ChatScreen screen) {
		x = Math.max(4, Math.min(Math.max(4, screen.width - panelWidth - 4), x));
		y = Math.max(4, Math.min(Math.max(4, screen.height - panelHeight - 4), y));
	}

	private boolean contains(double mouseX, double mouseY) {
		return mouseX >= visualX && mouseX < visualX + panelWidth
				&& mouseY >= y && mouseY < y + panelHeight;
	}

	private static boolean hit(
			double mouseX, double mouseY,
			int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width
				&& mouseY >= y && mouseY < y + height;
	}

	private static void fillButton(
			DrawContext context, int x, int y,
			int width, int height, int mouseX, int mouseY, boolean active) {
		ModernUiTheme.drawFixedControl(context, x, y, width, height,
				hit(mouseX, mouseY, x, y, width, height), active, true);
	}

	private int tabWidth() {
		return layoutMetrics.tabWidth();
	}

	private int tabX(int px, int index) {
		return px + layoutMetrics.tabX(index);
	}

	private int toolbarPrimaryWidth() {
		return layoutMetrics.toolbarPrimaryWidth(tab == CommandToolTab.RECENT);
	}

	private int toolbarSecondaryX(int px) {
		return px + layoutMetrics.toolbarSecondaryX();
	}

	private int toolbarSecondaryWidth() {
		return layoutMetrics.toolbarSecondaryWidth();
	}

	private int dialogButtonWidth() {
		return Math.max(1, (panelWidth - 40) / 2);
	}

	private int dialogCancelX(int px) {
		return px + 16;
	}

	private int dialogConfirmX(int px) {
		return dialogCancelX(px) + dialogButtonWidth() + 8;
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
