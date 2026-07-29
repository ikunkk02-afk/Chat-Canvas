package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.editor.ColorPickerState;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.IntConsumer;

public final class ModernColorPickerPopup extends BaseUIComponent {
	public static final int POPUP_WIDTH = 250;
	public static final int POPUP_HEIGHT = 266;

	private static final int SV_X = 10;
	private static final int SV_Y = 24;
	private static final int SV_WIDTH = 190;
	private static final int SV_HEIGHT = 90;
	private static final int HUE_X = 10;
	private static final int HUE_Y = 122;
	private static final int HUE_WIDTH = 190;
	private static final int HUE_HEIGHT = 12;
	private static final int HEX_X = 10;
	private static final int HEX_Y = 154;
	private static final int HEX_WIDTH = 190;
	private static final int HEX_HEIGHT = 22;
	private static final int RECENT_X = 10;
	private static final int RECENT_Y = 202;
	private static final int RECENT_SIZE = 20;
	private static final int RECENT_GAP = 4;
	private static final int BUTTON_Y = 234;

	private final Request request;
	private final Runnable closeAction;
	private final ColorPickerState state;
	private final long openedAt = System.nanoTime();

	private DragSection dragging = DragSection.NONE;
	private boolean hexFocused;
	private boolean selectAllHex;
	private boolean closed;

	public ModernColorPickerPopup(int x, int y, Request request, Runnable closeAction) {
		this.request = request;
		this.closeAction = closeAction;
		this.state = new ColorPickerState(request.initialRgb());
		this.sizing(Sizing.fixed(POPUP_WIDTH), Sizing.fixed(POPUP_HEIGHT));
		this.positioning(Positioning.absolute(x, y));
		this.zIndex(50);
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		super.update(delta, mouseX, mouseY);
		double localX = mouseX - x();
		double localY = mouseY - y();
		boolean colorArea = inside(localX, localY, SV_X, SV_Y, SV_WIDTH, SV_HEIGHT)
				|| inside(localX, localY, HUE_X, HUE_Y, HUE_WIDTH, HUE_HEIGHT);
		cursorStyle(colorArea ? CursorStyle.MOVE : CursorStyle.NONE);
	}

	@Override
	public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
		float progress = Math.min(1.0f, (System.nanoTime() - openedAt) / 100_000_000.0f);
		float eased = 1.0f - (1.0f - progress) * (1.0f - progress);
		float scale = 0.98f + 0.02f * eased;
		float centerX = x() + width() * 0.5f;
		float centerY = y() + height() * 0.5f;
		context.pose().pushMatrix();
		context.pose().translate(centerX, centerY - 4.0f * (1.0f - eased), 0.0f);
		context.pose().scale(scale, scale, 1.0f);
		context.pose().translate(-centerX, -centerY, 0.0f);

		ModernUiTheme.shadow(context, x(), y(), width(), height());
		ModernUiTheme.roundedRect(context, x(), y(), width(), height(), 7, 0xF21A1E28);
		ModernUiTheme.border(context, x(), y(), width(), height(), 0x8860738F);

		Font renderer = Minecraft.getInstance().textRenderer;
		context.drawText(renderer, Component.translatable("chat_canvas.color_picker.title"),
				x() + 10, y() + 8, ModernUiTheme.TEXT_PRIMARY, false);

		int hueColor = 0xFF000000 | ColorPickerState.rgbFromHsv(state.hue(), 1.0f, 1.0f);
		context.drawGradientRect(
				x() + SV_X, y() + SV_Y, SV_WIDTH, SV_HEIGHT,
				0xFFFFFFFF, hueColor, 0xFF000000, 0xFF000000
		);
		int selectorX = x() + SV_X + Math.round(state.saturation() * (SV_WIDTH - 1));
		int selectorY = y() + SV_Y + Math.round((1.0f - state.value()) * (SV_HEIGHT - 1));
		context.drawRectOutline(selectorX - 2, selectorY - 2, 5, 5, 0xFFFFFFFF);
		context.drawRectOutline(selectorX - 1, selectorY - 1, 3, 3, 0xFF10141C);

		context.drawSpectrum(x() + HUE_X, y() + HUE_Y, HUE_WIDTH, HUE_HEIGHT, false);
		int hueX = x() + HUE_X + Math.round(state.hue() * (HUE_WIDTH - 1));
		context.drawRectOutline(hueX - 1, y() + HUE_Y - 1, 3, HUE_HEIGHT + 2, 0xFFFFFFFF);

		drawCurrentPreview(context);
		drawHexInput(context, renderer);
		drawRecentColors(context, renderer);
		drawActions(context, renderer, mouseX, mouseY);
		context.pose().popMatrix();
	}

	private void drawCurrentPreview(OwoUIGraphics context) {
		int left = x() + 210;
		int top = y() + SV_Y;
		ModernUiTheme.roundedRect(context, left, top, 30, 110, 5,
				0xFF000000 | state.rgb());
		context.drawRectOutline(left, top, 30, 110, 0x88798BA5);
	}

	private void drawHexInput(OwoUIGraphics context, Font renderer) {
		context.drawText(renderer, Component.translatable("chat_canvas.color_picker.hex"),
				x() + HEX_X, y() + 143, ModernUiTheme.TEXT_SECONDARY, false);
		int border = state.hexValid()
				? hexFocused ? ModernUiTheme.ACCENT : 0x66596A82
				: 0xFFE36D76;
		ModernUiTheme.roundedRect(context, x() + HEX_X, y() + HEX_Y,
				HEX_WIDTH, HEX_HEIGHT, 4, 0xCC242A36);
		context.drawRectOutline(x() + HEX_X, y() + HEX_Y, HEX_WIDTH, HEX_HEIGHT, border);
		String text = state.hexInput();
		String visible = renderer.trimToWidth(text, HEX_WIDTH - 12, true);
		context.drawText(renderer, visible, x() + HEX_X + 6,
				y() + HEX_Y + (HEX_HEIGHT - renderer.lineHeight) / 2,
				state.hexValid() ? 0xFFF0F3F8 : 0xFFFFA3A8, false);
		if (hexFocused && (System.currentTimeMillis() / 350L) % 2L == 0L) {
			int cursorX = Math.min(x() + HEX_X + HEX_WIDTH - 5,
					x() + HEX_X + 6 + renderer.width(visible));
			context.fill(cursorX, y() + HEX_Y + 5, cursorX + 1,
					y() + HEX_Y + HEX_HEIGHT - 5, 0xFFFFFFFF);
		}
		if (!state.hexValid()) {
			context.drawText(renderer, Component.translatable("chat_canvas.color_picker.invalid"),
					x() + HEX_X, y() + 179, 0xFFFF858D, false);
		}
	}

	private void drawRecentColors(OwoUIGraphics context, Font renderer) {
		context.drawText(renderer, Component.translatable("chat_canvas.color_picker.recent"),
				x() + RECENT_X, y() + 190, ModernUiTheme.TEXT_SECONDARY, false);
		for (int index = 0; index < 8; index++) {
			int left = x() + RECENT_X + index * (RECENT_SIZE + RECENT_GAP);
			int color = index < request.recentColors().size()
					? 0xFF000000 | request.recentColors().get(index)
					: 0x552B313E;
			ModernUiTheme.roundedRect(context, left, y() + RECENT_Y,
					RECENT_SIZE, RECENT_SIZE, 4, color);
			context.drawRectOutline(left, y() + RECENT_Y,
					RECENT_SIZE, RECENT_SIZE, 0x665E6B80);
		}
	}

	private void drawActions(OwoUIGraphics context, Font renderer,
							 int mouseX, int mouseY) {
		drawAction(context, renderer, x() + 10, y() + BUTTON_Y, 96, 22,
				Component.translatable("chat_canvas.color_picker.restore_default"),
				inside(mouseX, mouseY, x() + 10, y() + BUTTON_Y, 96, 22), true);
		drawAction(context, renderer, x() + 112, y() + BUTTON_Y, 60, 22,
				Component.translatable("chat_canvas.action.cancel"),
				inside(mouseX, mouseY, x() + 112, y() + BUTTON_Y, 60, 22), true);
		drawAction(context, renderer, x() + 178, y() + BUTTON_Y, 62, 22,
				Component.translatable("chat_canvas.action.confirm"),
				inside(mouseX, mouseY, x() + 178, y() + BUTTON_Y, 62, 22),
				state.hexValid());
	}

	private static void drawAction(OwoUIGraphics context, Font renderer,
								   int x, int y, int width, int height,
								   Component label, boolean hovered, boolean active) {
		int background = !active
				? 0x55343A48
				: hovered ? 0xE04B5970 : 0xC8374256;
		ModernUiTheme.roundedRect(context, x, y, width, height, 5, background);
		int color = active ? 0xFFF0F3F8 : 0xFF737C8C;
		int textX = x + Math.max(3, (width - renderer.width(label)) / 2);
		int textY = y + (height - renderer.lineHeight) / 2;
		context.drawText(renderer, label, textX, textY, color, false);
	}

	@Override
	public boolean onMouseDown(double mouseX, double mouseY, int button) {
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || closed) {
			return true;
		}
		if (hexFocused && !inside(mouseX, mouseY, HEX_X, HEX_Y, HEX_WIDTH, HEX_HEIGHT)) {
			hexFocused = false;
			selectAllHex = false;
			if (state.hexValid()) {
				state.normalizeHexInput();
			}
		}
		if (inside(mouseX, mouseY, SV_X, SV_Y, SV_WIDTH, SV_HEIGHT)) {
			dragging = DragSection.SATURATION_VALUE;
			updateFromPointer(mouseX, mouseY);
		} else if (inside(mouseX, mouseY, HUE_X, HUE_Y, HUE_WIDTH, HUE_HEIGHT)) {
			dragging = DragSection.HUE;
			updateFromPointer(mouseX, mouseY);
		} else if (inside(mouseX, mouseY, HEX_X, HEX_Y, HEX_WIDTH, HEX_HEIGHT)) {
			hexFocused = true;
			selectAllHex = true;
		} else if (inside(mouseX, mouseY, RECENT_X, RECENT_Y,
				8 * (RECENT_SIZE + RECENT_GAP) - RECENT_GAP, RECENT_SIZE)) {
			int index = (int) ((mouseX - RECENT_X) / (RECENT_SIZE + RECENT_GAP));
			int within = (int) ((mouseX - RECENT_X) % (RECENT_SIZE + RECENT_GAP));
			if (within < RECENT_SIZE && index >= 0 && index < request.recentColors().size()) {
				applyRgb(request.recentColors().get(index));
			}
		} else if (inside(mouseX, mouseY, 10, BUTTON_Y, 96, 22)) {
			applyRgb(request.defaultRgb());
		} else if (inside(mouseX, mouseY, 112, BUTTON_Y, 60, 22)) {
			cancel();
		} else if (inside(mouseX, mouseY, 178, BUTTON_Y, 62, 22)) {
			confirm();
		}
		return true;
	}

	@Override
	public boolean onMouseDrag(double mouseX, double mouseY, double deltaX,
							   double deltaY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && dragging != DragSection.NONE) {
			updateFromPointer(mouseX, mouseY);
		}
		return true;
	}

	@Override
	public boolean onMouseUp(double mouseX, double mouseY, int button) {
		dragging = DragSection.NONE;
		return true;
	}

	@Override
	public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
		if (!hexFocused || closed) {
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			confirm();
			return true;
		}
		if (Screen.isSelectAll(keyCode)) {
			selectAllHex = true;
			return true;
		}
		if (Screen.isPaste(keyCode)) {
			replaceOrAppend(Minecraft.getInstance().keyboard.getClipboard());
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			String input = state.hexInput();
			if (selectAllHex) {
				state.updateHexInput("");
				selectAllHex = false;
			} else if (!input.isEmpty()) {
				state.updateHexInput(input.substring(0, input.length() - 1));
			}
			return true;
		}
		return true;
	}

	@Override
	public boolean onCharTyped(char chr, int modifiers) {
		if (!hexFocused || closed || Character.isISOControl(chr)) {
			return false;
		}
		replaceOrAppend(Character.toString(chr));
		return true;
	}

	@Override
	public boolean canFocus(FocusSource source) {
		return true;
	}

	public boolean containsScreen(double mouseX, double mouseY) {
		return mouseX >= x() && mouseX <= x() + width()
				&& mouseY >= y() && mouseY <= y() + height();
	}

	public void cancel() {
		if (closed) return;
		closed = true;
		request.livePreview().accept(request.initialRgb());
		request.cancelled().run();
		closeAction.run();
	}

	public void confirm() {
		if (closed || !state.hexValid()) {
			return;
		}
		closed = true;
		state.normalizeHexInput();
		request.livePreview().accept(state.rgb());
		request.confirmed().accept(state.rgb());
		closeAction.run();
	}

	private void updateFromPointer(double mouseX, double mouseY) {
		if (dragging == DragSection.SATURATION_VALUE) {
			float saturation = clamp01((float) ((mouseX - SV_X) / SV_WIDTH));
			float value = 1.0f - clamp01((float) ((mouseY - SV_Y) / SV_HEIGHT));
			state.setHsv(state.hue(), saturation, value);
		} else if (dragging == DragSection.HUE) {
			float hue = clamp01((float) ((mouseX - HUE_X) / HUE_WIDTH));
			state.setHsv(hue, state.saturation(), state.value());
		}
		request.livePreview().accept(state.rgb());
	}

	private void replaceOrAppend(String value) {
		String addition = value == null ? "" : value;
		String next = selectAllHex ? addition : state.hexInput() + addition;
		selectAllHex = false;
		if (next.length() > 16) {
			next = next.substring(0, 16);
		}
		if (state.updateHexInput(next)) {
			request.livePreview().accept(state.rgb());
		}
	}

	private void applyRgb(int rgb) {
		state.setRgb(rgb);
		request.livePreview().accept(state.rgb());
	}

	private static float clamp01(float value) {
		return Math.max(0.0f, Math.min(1.0f, value));
	}

	private static boolean inside(double mouseX, double mouseY,
								  double x, double y, double width, double height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	public record Request(
			int initialRgb,
			int defaultRgb,
			List<Integer> recentColors,
			IntConsumer livePreview,
			IntConsumer confirmed,
			Runnable cancelled
	) {
		public Request {
			recentColors = recentColors == null ? List.of() : List.copyOf(recentColors);
		}
	}

	private enum DragSection {
		NONE,
		SATURATION_VALUE,
		HUE
	}
}
