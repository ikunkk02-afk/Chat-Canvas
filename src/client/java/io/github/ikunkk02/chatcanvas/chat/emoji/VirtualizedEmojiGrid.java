package io.github.ikunkk02.chatcanvas.chat.emoji;

import io.github.ikunkk02.chatcanvas.ui.ModernUiTheme;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import io.github.ikunkk02.chatcanvas.chat.render.ChatBackgroundDraw;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public final class VirtualizedEmojiGrid extends BaseUIComponent {
	private static final int CELL_WIDTH = 28;
	private static final int CELL_HEIGHT = 23;

	private final Consumer<EmojiEntry> selectedCallback;
	private List<EmojiEntry> entries = List.of();
	private int selected;
	private int scrollRow;
	private EmojiEntry hoveredEntry;
	private String emptyMessageKey = "chat_canvas.emoji.empty";

	public VirtualizedEmojiGrid(Consumer<EmojiEntry> selectedCallback) {
		this.selectedCallback = selectedCallback;
		this.sizing(Sizing.fill(100), Sizing.fill(100));
		this.cursorStyle(CursorStyle.HAND);
	}

	public void entries(List<EmojiEntry> values) {
		List<EmojiEntry> next = List.copyOf(values == null ? List.of() : values);
		boolean contentChanged = !entries.equals(next);
		entries = next;
		selected = Math.max(0, Math.min(selected, Math.max(0, entries.size() - 1)));
		if (contentChanged) scrollRow = 0;
		else ensureSelectedVisible();
	}

	public List<EmojiEntry> entries() {
		return entries;
	}

	public EmojiEntry selectedEntry() {
		return entries.isEmpty() ? null : entries.get(selected);
	}

	public EmojiEntry hoveredEntry() {
		return hoveredEntry;
	}

	public void emptyMessageKey(String translationKey) {
		emptyMessageKey = translationKey == null
				? "chat_canvas.emoji.empty" : translationKey;
	}

	public boolean keyPressed(int keyCode) {
		if (entries.isEmpty()) return keyCode == GLFW.GLFW_KEY_ENTER;
		int columns = columns();
		int next = selected;
		if (keyCode == GLFW.GLFW_KEY_LEFT) next--;
		else if (keyCode == GLFW.GLFW_KEY_RIGHT) next++;
		else if (keyCode == GLFW.GLFW_KEY_UP) next -= columns;
		else if (keyCode == GLFW.GLFW_KEY_DOWN) next += columns;
		else if (keyCode == GLFW.GLFW_KEY_ENTER
				|| keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			selectedCallback.accept(entries.get(selected));
			return true;
		} else {
			return false;
		}
		selected = Math.max(0, Math.min(entries.size() - 1, next));
		ensureSelectedVisible();
		return true;
	}

	@Override
	public boolean onMouseDown(net.minecraft.client.gui.Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT
				|| !containsLocal(mouseX, mouseY)) return false;
		int index = indexAt(mouseX, mouseY);
		if (index < 0 || index >= entries.size()) return true;
		selected = index;
		selectedCallback.accept(entries.get(index));
		return true;
	}

	@Override
	public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
		if (!containsLocal(mouseX, mouseY)) return false;
		int max = maxScrollRow();
		scrollRow = Math.max(0, Math.min(max,
				scrollRow + (amount < 0 ? 1 : -1)));
		return true;
	}

	@Override
	public void draw(
			OwoUIGraphics context, int mouseX, int mouseY,
			float partialTicks, float delta) {
		context.fill(x(), y(), x() + width(), y() + height(),
				ModernUiTheme.CONTROL_BACKGROUND);
		hoveredEntry = null;
		if (entries.isEmpty()) {
			Text empty = Text.translatable(emptyMessageKey);
			context.drawCenteredTextWithShadow(
					MinecraftClient.getInstance().textRenderer,
					empty, x() + width() / 2,
					y() + Math.max(2, height() / 2 - 4), ModernUiTheme.TEXT_SECONDARY);
			return;
		}
		context.enableScissor(x(), y(), x() + width(), y() + height());
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		int columns = columns();
		int start = scrollRow * columns;
		int visibleRows = Math.max(1, (height() + CELL_HEIGHT - 1) / CELL_HEIGHT);
		int end = Math.min(entries.size(), start + visibleRows * columns);
		for (int index = start; index < end; index++) {
			int local = index - start;
			int cellX = x() + (local % columns) * CELL_WIDTH;
			int cellY = y() + (local / columns) * CELL_HEIGHT;
			boolean hovered = mouseX >= cellX && mouseX < cellX + CELL_WIDTH
					&& mouseY >= cellY && mouseY < cellY + CELL_HEIGHT;
			if (hovered) {
				hoveredEntry = entries.get(index);
				context.fill(cellX + 1, cellY + 1,
						cellX + CELL_WIDTH - 1, cellY + CELL_HEIGHT - 1,
						ModernUiTheme.CONTROL_HOVER);
			}
			if (index == selected) {
				ChatBackgroundDraw.drawBorder(context, cellX, cellY,
						CELL_WIDTH, CELL_HEIGHT, ModernUiTheme.ACCENT);
				context.fill(cellX + 3, cellY + CELL_HEIGHT - 3,
						cellX + CELL_WIDTH - 3, cellY + CELL_HEIGHT - 1,
						ModernUiTheme.ACCENT);
			}
			String emoji = entries.get(index).unicode();
			context.drawTextWithShadow(renderer, emoji,
					cellX + (CELL_WIDTH - renderer.getWidth(emoji)) / 2,
					cellY + (CELL_HEIGHT - renderer.fontHeight) / 2,
					ModernUiTheme.TEXT_PRIMARY);
		}
		context.disableScissor();
		if (maxScrollRow() > 0) {
			int thumbHeight = Math.max(8,
					height() * visibleRows / Math.max(visibleRows, totalRows()));
			int track = Math.max(1, height() - thumbHeight);
			int thumbY = y() + track * scrollRow / maxScrollRow();
			context.fill(x() + width() - 2, thumbY,
					x() + width(), thumbY + thumbHeight, ModernUiTheme.SCROLLBAR);
		}
	}

	private int indexAt(double mouseX, double mouseY) {
		int column = (int) (mouseX / CELL_WIDTH);
		int row = (int) (mouseY / CELL_HEIGHT);
		if (column < 0 || column >= columns() || row < 0) return -1;
		return (scrollRow + row) * columns() + column;
	}

	private boolean containsLocal(double mouseX, double mouseY) {
		return mouseX >= 0 && mouseX < width()
				&& mouseY >= 0 && mouseY < height();
	}

	private int columns() {
		return Math.max(1, width() / CELL_WIDTH);
	}

	private int totalRows() {
		return (entries.size() + columns() - 1) / columns();
	}

	private int maxScrollRow() {
		int visibleRows = Math.max(1, height() / CELL_HEIGHT);
		return Math.max(0, totalRows() - visibleRows);
	}

	private void ensureSelectedVisible() {
		int row = selected / columns();
		int visibleRows = Math.max(1, height() / CELL_HEIGHT);
		if (row < scrollRow) scrollRow = row;
		if (row >= scrollRow + visibleRows) scrollRow = row - visibleRows + 1;
		scrollRow = Math.max(0, Math.min(maxScrollRow(), scrollRow));
	}
}
