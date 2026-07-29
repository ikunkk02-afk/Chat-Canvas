package io.github.ikunkk02.chatcanvas.chat.emoji;

import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputController;
import io.github.ikunkk02.chatcanvas.chat.input.ChatInputSnapshot;
import io.github.ikunkk02.chatcanvas.chat.text.UnicodeTextNavigator;
import io.github.ikunkk02.chatcanvas.mixin.client.ChatInputSuggestorAccessor;
import io.github.ikunkk02.chatcanvas.mixin.client.SuggestionWindowAccessor;
import io.github.ikunkk02.chatcanvas.mixin.client.TextFieldWidgetAccessor;
import io.github.ikunkk02.chatcanvas.ui.ModernUiTheme;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import io.github.ikunkk02.chatcanvas.chat.render.ChatBackgroundDraw;

public final class EmojiPickerPanel {
	public static final int BUTTON_SPACE = 20;
	private static final int MAX_WIDTH = 320;
	private static final int MAX_HEIGHT = 230;
	private static final int BUTTON_WIDTH = 18;
	private static final int BUTTON_HEIGHT = 14;

	private final ChatCanvasInputController inputController =
			ChatCanvasInputController.instance();
	private final Map<EmojiCategory, ButtonComponent> categoryButtons =
			new EnumMap<>(EmojiCategory.class);
	private ChatScreen owner;
	private TextFieldWidget playerField;
	private ChatInputSuggestor suggestor;
	private OwoUIAdapter<FlowLayout> adapter;
	private TextBoxComponent searchField;
	private VirtualizedEmojiGrid grid;
	private List<EmojiEntry> supported = List.of();
	private List<EmojiCategory> availableCategories = List.of();
	private EmojiCategory category = EmojiCategory.RECENT;
	private FocusTarget focus = FocusTarget.CHAT;
	private boolean open;
	private float openProgress;
	private int x;
	private int y;
	private int width;
	private int height;
	private int buttonX;
	private int buttonY;
	private long fontEpoch = Long.MIN_VALUE;
	private int adapterX = Integer.MIN_VALUE;
	private int adapterY = Integer.MIN_VALUE;
	private int adapterWidth = -1;
	private int adapterHeight = -1;
	private String statusKey;
	private long statusUntil;

	public void init(
			ChatScreen screen, TextFieldWidget field,
			ChatInputSuggestor inputSuggestor) {
		dispose();
		owner = screen;
		playerField = field;
		suggestor = inputSuggestor;
		refreshFontEntries();
		position();
		buildAdapter();
	}

	public boolean isOpen() {
		return open;
	}

	public void toggle() {
		if (open) close();
		else open();
	}

	public void open() {
		if (owner == null || playerField == null) return;
		open = true;
		statusKey = null;
		refreshFontEntries();
		updateGrid();
		focusChat();
	}

	public void close() {
		open = false;
		if (searchField != null && !searchField.getText().isEmpty()) {
			searchField.setText("");
		}
		focusChat();
	}

	public void dispose() {
		open = false;
		openProgress = 0.0f;
		if (adapter != null) adapter.dispose();
		adapter = null;
		adapterX = Integer.MIN_VALUE;
		adapterY = Integer.MIN_VALUE;
		adapterWidth = -1;
		adapterHeight = -1;
		searchField = null;
		grid = null;
		categoryButtons.clear();
		supported = List.of();
		availableCategories = List.of();
		owner = null;
		playerField = null;
		suggestor = null;
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (playerField == null) return false;
		if (hit(mouseX, mouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
			toggle();
			return true;
		}
		if (!open) return false;
		if (!contains(mouseX, mouseY)) {
			close();
			return false;
		}
		if (adapter != null && adapter.mouseClicked(
				new net.minecraft.client.gui.Click(
					mouseX - x, mouseY - y,
					new net.minecraft.client.input.MouseInput(button, 0)),
				false)) {
			if (searchField != null && searchField.isFocused()) {
				focus = FocusTarget.SEARCH;
				playerField.setFocused(false);
			}
			return true;
		}
		return true;
	}

	public boolean mouseScrolled(
			double mouseX, double mouseY, double horizontal, double vertical) {
		return open && contains(mouseX, mouseY) && adapter != null
				&& adapter.mouseScrolled(
						mouseX - x, mouseY - y, horizontal, vertical);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean control = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
		if (control && keyCode == GLFW.GLFW_KEY_E) {
			toggle();
			return true;
		}
		if (control && keyCode == GLFW.GLFW_KEY_F) {
			if (!open) open();
			focusSearch();
			return true;
		}
		if (!open) return false;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (searchField != null && !searchField.getText().isEmpty()) {
				searchField.setText("");
				updateGrid();
			} else {
				close();
			}
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			cycleFocus((modifiers & GLFW.GLFW_MOD_SHIFT) != 0 ? -1 : 1);
			return true;
		}
		if (focus == FocusTarget.SEARCH) {
			return adapter != null
					&& adapter.keyPressed(new net.minecraft.client.input.KeyInput(keyCode, scanCode, modifiers));
		}
		if (focus == FocusTarget.CATEGORIES) {
			if (keyCode == GLFW.GLFW_KEY_LEFT
					|| keyCode == GLFW.GLFW_KEY_RIGHT) {
				int direction = keyCode == GLFW.GLFW_KEY_LEFT ? -1 : 1;
				selectAdjacentCategory(direction);
				return true;
			}
			if (keyCode == GLFW.GLFW_KEY_ENTER
					|| keyCode == GLFW.GLFW_KEY_KP_ENTER
					|| keyCode == GLFW.GLFW_KEY_DOWN) {
				focusGrid();
				return true;
			}
		}
		return focus == FocusTarget.GRID && grid != null
				&& grid.keyPressed(keyCode);
	}

	public boolean charTyped(char character, int modifiers) {
		return open && focus == FocusTarget.SEARCH && adapter != null
				&& adapter.charTyped(new net.minecraft.client.input.CharInput(character, modifiers));
	}

	public boolean writeComposedText(String text) {
		if (!open || focus != FocusTarget.SEARCH
				|| searchField == null || text == null || text.isEmpty()) return false;
		searchField.write(text);
		return true;
	}

	public void render(
			DrawContext context, int mouseX, int mouseY, float delta) {
		if (owner == null || playerField == null) return;
		if (fontEpoch != EmojiFontSupport.epoch()) {
			refreshFontEntries();
			buildAdapter();
		}
		position();
		renderButton(context, mouseX, mouseY);
		float target = open ? 1.0f : 0.0f;
		openProgress += (target - openProgress)
				* Math.min(1.0f, Math.max(0.14f, delta * 0.4f));
		if (Math.abs(openProgress - target) < 0.01f) openProgress = target;
		if (openProgress <= 0.01f || adapter == null) return;
		if (adapterX != x || adapterY != y
				|| adapterWidth != width || adapterHeight != height) {
			adapter.moveAndResize(x, y, width, height);
			adapterX = x;
			adapterY = y;
			adapterWidth = width;
			adapterHeight = height;
		}
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(0.0f, Math.round((1.0f - openProgress) * 8.0f));
		adapter.render(context, mouseX, mouseY, delta);
		context.getMatrices().popMatrix();
		EmojiEntry hovered = grid == null ? null : grid.hoveredEntry();
		if (hovered != null) {
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(0.0f, 0.0f);
			context.drawTooltip(MinecraftClient.getInstance().textRenderer,
					Text.literal(hovered.unicode() + "  "
							+ hovered.chineseName() + " / " + hovered.englishName()),
					mouseX, mouseY);
			context.getMatrices().popMatrix();
		}
		if (statusKey != null && System.currentTimeMillis() < statusUntil) {
			context.drawTextWithShadow(
					MinecraftClient.getInstance().textRenderer,
					Text.translatable(statusKey), x + 7, y + height - 12,
					0xFFFF858D);
		}
	}

	private void buildAdapter() {
		if (owner == null) return;
		if (adapter != null) adapter.dispose();
		categoryButtons.clear();
		adapter = OwoUIAdapter.createWithoutScreen(
				x, y, width, height, Containers::verticalFlow);
		adapterX = x;
		adapterY = y;
		adapterWidth = width;
		adapterHeight = height;
		FlowLayout root = adapter.rootComponent;
		root.surface(ModernUiTheme.PANEL_SURFACE);
		root.padding(Insets.of(6));
		root.gap(4);

		searchField = Components.textBox(Sizing.fill(100));
		searchField.setMaxLength(64);
		searchField.setPlaceholder(Text.translatable("chat_canvas.emoji.search"));
		searchField.sizing(Sizing.fill(100), Sizing.fixed(18));
		searchField.onChanged().subscribe(value -> updateGrid());
		root.child(searchField);

		FlowLayout categoryRows = Containers.verticalFlow(
				Sizing.fill(100), Sizing.fixed(44));
		categoryRows.gap(2);
		int categoryWidth = Math.max(20, (width - 20) / 5);
		for (int rowIndex = 0; rowIndex < 2; rowIndex++) {
			FlowLayout row = Containers.horizontalFlow(
					Sizing.fill(100), Sizing.fixed(21));
			row.gap(2);
			for (int column = 0; column < 5; column++) {
				int index = rowIndex * 5 + column;
				if (index >= availableCategories.size()) break;
				EmojiCategory target = availableCategories.get(index);
				ButtonComponent button = ModernUiTheme.button(
						categoryText(target), clicked -> {
							category = target;
							focus = FocusTarget.CATEGORIES;
							setSearchFocused(false);
							updateCategoryButtons();
							updateGrid();
						});
				button.sizing(Sizing.fixed(categoryWidth), Sizing.fixed(20));
				categoryButtons.put(target, button);
				row.child(button);
			}
			categoryRows.child(row);
		}
		root.child(categoryRows);

		grid = new VirtualizedEmojiGrid(this::insert);
		root.child(grid);
		adapter.inflateAndMount();
		updateCategoryButtons();
		updateGrid();
	}

	private void updateGrid() {
		if (grid == null) return;
		String query = searchField == null ? "" : searchField.getText();
		List<EmojiEntry> values;
		if (!query.isBlank()) {
			values = EmojiRegistry.instance().search(query).stream()
					.filter(supported::contains).toList();
		} else if (category == EmojiCategory.RECENT) {
			values = EmojiRuntime.recent().entries().stream()
					.filter(supported::contains).toList();
		} else {
			values = EmojiRegistry.instance().category(category).stream()
					.filter(supported::contains).toList();
		}
		grid.emptyMessageKey(query.isBlank()
				&& category == EmojiCategory.RECENT
				? "chat_canvas.emoji.recent_empty"
				: "chat_canvas.emoji.empty");
		grid.entries(values);
	}

	private void refreshFontEntries() {
		supported = EmojiFontSupport.supportedEntries(
				MinecraftClient.getInstance().textRenderer);
		List<EmojiCategory> categories = new ArrayList<>();
		categories.add(EmojiCategory.RECENT);
		for (EmojiCategory candidate : EmojiCategory.values()) {
			if (candidate == EmojiCategory.RECENT) continue;
			boolean present = supported.stream()
					.anyMatch(entry -> entry.category() == candidate);
			if (present) categories.add(candidate);
		}
		availableCategories = List.copyOf(categories);
		if (!availableCategories.contains(category)) {
			category = availableCategories.getFirst();
		}
		fontEpoch = EmojiFontSupport.epoch();
	}

	private void insert(EmojiEntry entry) {
		if (entry == null || playerField == null) return;
		TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) playerField;
		inputController.capture(
				io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputMode.PLAYER_CHAT,
				new ChatInputSnapshot(
						playerField.getText(), playerField.getCursor(),
						accessor.chat_canvas$selectionEnd()));
		UnicodeTextNavigator.EditResult result =
				inputController.insertPlayerText(entry.unicode(), 256);
		if (result.limitExceeded()) {
			statusKey = "chat_canvas.emoji.input_too_long";
			statusUntil = System.currentTimeMillis() + 2500L;
			focusChat();
			return;
		}
		playerField.setText(result.text());
		playerField.setSelectionStart(result.cursor());
		playerField.setSelectionEnd(result.selectionEnd());
		inputController.capture(
				io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputMode.PLAYER_CHAT,
				new ChatInputSnapshot(
						result.text(), result.cursor(), result.selectionEnd()));
		EmojiRuntime.recent().recordSelected(entry.unicode());
		if (suggestor != null) {
			suggestor.setWindowActive(!result.text().isEmpty());
			suggestor.refresh();
		}
		updateGrid();
		focusChat();
	}

	private void position() {
		if (owner == null || playerField == null) return;
		buttonX = playerField.getX() + playerField.getWidth() + 1;
		buttonY = playerField.getY() - 1;
		int nextWidth = Math.max(80, Math.min(MAX_WIDTH, owner.width - 8));
		int nextHeight = Math.max(90, Math.min(MAX_HEIGHT, owner.height - 8));
		if (nextWidth != width || nextHeight != height) {
			width = nextWidth;
			height = nextHeight;
		}
		Rect2i suggestions = suggestionArea();
		int[][] candidates = {
				{buttonX + BUTTON_WIDTH - width, playerField.getY() - height - 18},
				{playerField.getX() + playerField.getWidth() + BUTTON_SPACE + 2,
						playerField.getY() - height / 2},
				{playerField.getX() - width - 4, playerField.getY() - height / 2},
				{buttonX + BUTTON_WIDTH - width, playerField.getY() + 20}
		};
		long bestScore = Long.MAX_VALUE;
		for (int[] candidate : candidates) {
			int candidateX = clamp(candidate[0], 4, Math.max(4, owner.width - width - 4));
			int candidateY = clamp(candidate[1], 4, Math.max(4, owner.height - height - 4));
			long score = overlap(candidateX, candidateY, width, height, suggestions)
					* 1_000_000L + Math.abs(candidateX - buttonX)
					+ Math.abs(candidateY - buttonY);
			if (score >= bestScore) continue;
			bestScore = score;
			x = candidateX;
			y = candidateY;
		}
	}

	private void renderButton(DrawContext context, int mouseX, int mouseY) {
		boolean hovered = hit(mouseX, mouseY,
				buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
		context.fill(buttonX, buttonY,
				buttonX + BUTTON_WIDTH, buttonY + BUTTON_HEIGHT,
				open ? 0xE0526684 : hovered ? 0xD0445066 : 0xB02A3240);
		ChatBackgroundDraw.drawBorder(context, buttonX, buttonY,
				BUTTON_WIDTH, BUTTON_HEIGHT, open ? 0xFFF6C85F : 0xFF71809A);
		context.drawCenteredTextWithShadow(
				MinecraftClient.getInstance().textRenderer,
				Text.literal("😀"), buttonX + BUTTON_WIDTH / 2,
				buttonY + 3, 0xFFFFFFFF);
	}

	private void focusSearch() {
		focus = FocusTarget.SEARCH;
		setSearchFocused(true);
		if (playerField != null) playerField.setFocused(false);
	}

	private void focusGrid() {
		focus = FocusTarget.GRID;
		setSearchFocused(false);
		if (playerField != null) playerField.setFocused(false);
	}

	private void focusChat() {
		focus = FocusTarget.CHAT;
		setSearchFocused(false);
		if (owner != null && playerField != null) {
			((ParentElement) owner).setFocused(playerField);
			playerField.setFocused(true);
		}
	}

	private void cycleFocus(int direction) {
		FocusTarget[] order = FocusTarget.values();
		int next = Math.floorMod(focus.ordinal() + direction, order.length);
		focus = order[next];
		if (focus == FocusTarget.SEARCH) focusSearch();
		else if (focus == FocusTarget.GRID) focusGrid();
		else if (focus == FocusTarget.CHAT) focusChat();
		else {
			setSearchFocused(false);
			if (playerField != null) playerField.setFocused(false);
		}
	}

	private void selectAdjacentCategory(int direction) {
		int current = Math.max(0, availableCategories.indexOf(category));
		category = availableCategories.get(
				Math.floorMod(current + direction, availableCategories.size()));
		updateCategoryButtons();
		updateGrid();
	}

	private void updateCategoryButtons() {
		categoryButtons.forEach((candidate, button) ->
				button.setMessage(categoryText(candidate)));
	}

	private Text categoryText(EmojiCategory candidate) {
		String marker = candidate == category ? "▶ " : "";
		return Text.literal(marker).append(
				Text.translatable(candidate.translationKey() + ".short"));
	}

	private void setSearchFocused(boolean focused) {
		if (searchField != null) searchField.setFocused(focused);
	}

	private Rect2i suggestionArea() {
		if (suggestor == null) return null;
		ChatInputSuggestor.SuggestionWindow window =
				((ChatInputSuggestorAccessor) suggestor).chat_canvas$window();
		return window == null ? null
				: ((SuggestionWindowAccessor) window).chat_canvas$area();
	}

	private boolean contains(double mouseX, double mouseY) {
		return hit(mouseX, mouseY, x, y, width, height);
	}

	private static long overlap(
			int x, int y, int width, int height, Rect2i other) {
		if (other == null) return 0L;
		int overlapWidth = Math.max(0,
				Math.min(x + width, other.getX() + other.getWidth())
						- Math.max(x, other.getX()));
		int overlapHeight = Math.max(0,
				Math.min(y + height, other.getY() + other.getHeight())
						- Math.max(y, other.getY()));
		return (long) overlapWidth * overlapHeight;
	}

	private static boolean hit(
			double mouseX, double mouseY,
			int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width
				&& mouseY >= y && mouseY < y + height;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private enum FocusTarget {
		CHAT,
		SEARCH,
		CATEGORIES,
		GRID
	}

}
