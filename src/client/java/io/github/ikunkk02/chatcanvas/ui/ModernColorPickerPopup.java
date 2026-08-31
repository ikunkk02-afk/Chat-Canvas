package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.editor.ColorPickerState;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.IntConsumer;

public final class ModernColorPickerPopup extends BaseComponent {
	private final Request request;
	private final Runnable closeAction;
	private final ColorPickerState state;
	private final UiLayoutMetrics.ColorPicker metrics;
	private final long openedAt = System.nanoTime();

	private DragSection dragging = DragSection.NONE;
	private boolean hexFocused;
	private boolean selectAllHex;
	private boolean closed;

	public ModernColorPickerPopup(int x, int y, UiLayoutMetrics.ColorPicker metrics,
			Request request, Runnable closeAction) {
		this.request = request;
		this.closeAction = closeAction;
		this.metrics = metrics;
		this.state = new ColorPickerState(request.initialRgb());
		this.sizing(Sizing.fixed(metrics.width()), Sizing.fixed(metrics.height()));
		this.positioning(Positioning.absolute(x, y));
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		super.update(delta, mouseX, mouseY);
		double localX = mouseX - x();
		double localY = mouseY - y();
		boolean colorArea = inside(localX, localY,
				metrics.svX(), metrics.svY(), metrics.svWidth(), metrics.svHeight())
				|| inside(localX, localY, metrics.svX(), metrics.hueY(),
				metrics.svWidth(), metrics.hueHeight());
		cursorStyle(colorArea ? CursorStyle.MOVE : CursorStyle.NONE);
	}

	@Override
	public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
		float progress = Math.min(1.0f, (System.nanoTime() - openedAt) / 100_000_000.0f);
		float eased = 1.0f - (1.0f - progress) * (1.0f - progress);
		float scale = 0.98f + 0.02f * eased;
		float centerX = x() + width() * 0.5f;
		float centerY = y() + height() * 0.5f;
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(centerX, centerY - 4.0f * (1.0f - eased));
		context.getMatrices().scale(scale, scale);
		context.getMatrices().translate(-centerX, -centerY);

		ModernUiTheme.shadow(context, x(), y(), width(), height());
		ModernUiTheme.roundedRect(context, x(), y(), width(), height(), 2,
				ModernUiTheme.PANEL_ELEVATED);
		ModernUiTheme.border(context, x(), y(), width(), height(), ModernUiTheme.PANEL_BORDER);

		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		context.drawText(renderer, Text.translatable("chat_canvas.color_picker.title"),
				x() + metrics.margin(), y() + (metrics.compact() ? 5 : 8),
				ModernUiTheme.TEXT_PRIMARY, false);

		int hueColor = 0xFF000000 | ColorPickerState.rgbFromHsv(state.hue(), 1.0f, 1.0f);
		context.drawGradientRect(
				x() + metrics.svX(), y() + metrics.svY(), metrics.svWidth(), metrics.svHeight(),
				0xFFFFFFFF, hueColor, 0xFF000000, 0xFF000000
		);
		int selectorX = x() + metrics.svX()
				+ Math.round(state.saturation() * (metrics.svWidth() - 1));
		int selectorY = y() + metrics.svY()
				+ Math.round((1.0f - state.value()) * (metrics.svHeight() - 1));
		context.drawRectOutline(selectorX - 2, selectorY - 2, 5, 5, 0xFFFFFFFF);
		context.drawRectOutline(selectorX - 1, selectorY - 1, 3, 3, 0xFF10141C);

		context.drawSpectrum(x() + metrics.svX(), y() + metrics.hueY(),
				metrics.svWidth(), metrics.hueHeight(), false);
		int hueX = x() + metrics.svX()
				+ Math.round(state.hue() * (metrics.svWidth() - 1));
		context.drawRectOutline(hueX - 1, y() + metrics.hueY() - 1,
				3, metrics.hueHeight() + 2, 0xFFFFFFFF);

		drawCurrentPreview(context);
		drawHexInput(context, renderer);
		drawRecentColors(context, renderer);
		drawActions(context, renderer, mouseX, mouseY);
		context.getMatrices().popMatrix();
	}

	private void drawCurrentPreview(OwoUIDrawContext context) {
		int left = x() + metrics.previewX();
		int top = y() + metrics.svY();
		ModernUiTheme.roundedRect(context, left, top,
				metrics.previewWidth(), metrics.previewHeight(), 1,
				0xFF000000 | state.rgb());
		context.drawRectOutline(left, top, metrics.previewWidth(), metrics.previewHeight(),
				ModernUiTheme.PANEL_BORDER);
	}

	private void drawHexInput(OwoUIDrawContext context, TextRenderer renderer) {
		context.drawText(renderer, Text.translatable("chat_canvas.color_picker.hex"),
				x() + metrics.svX(), y() + metrics.hexY() - 11,
				ModernUiTheme.TEXT_SECONDARY, false);
		int border = state.hexValid()
				? hexFocused ? ModernUiTheme.ACCENT : ModernUiTheme.PANEL_BORDER
				: ModernUiTheme.DANGER;
		ModernUiTheme.roundedRect(context, x() + metrics.svX(), y() + metrics.hexY(),
				metrics.svWidth(), metrics.hexHeight(), 1, ModernUiTheme.CONTROL_BACKGROUND);
		context.drawRectOutline(x() + metrics.svX(), y() + metrics.hexY(),
				metrics.svWidth(), metrics.hexHeight(), border);
		String text = state.hexInput();
		String visible = renderer.trimToWidth(text, metrics.svWidth() - 12, true);
		context.drawText(renderer, visible, x() + metrics.svX() + 6,
				y() + metrics.hexY() + (metrics.hexHeight() - renderer.fontHeight) / 2,
				state.hexValid() ? ModernUiTheme.TEXT_PRIMARY : ModernUiTheme.DANGER, false);
		if (hexFocused && (System.currentTimeMillis() / 350L) % 2L == 0L) {
			int cursorX = Math.min(x() + metrics.svX() + metrics.svWidth() - 5,
					x() + metrics.svX() + 6 + renderer.getWidth(visible));
			context.fill(cursorX, y() + metrics.hexY() + 4, cursorX + 1,
					y() + metrics.hexY() + metrics.hexHeight() - 4, ModernUiTheme.TEXT_PRIMARY);
		}
		if (!state.hexValid() && !metrics.compact()) {
			context.drawText(renderer, Text.translatable("chat_canvas.color_picker.invalid"),
					x() + metrics.svX(), y() + metrics.hexY() + metrics.hexHeight() + 3,
					ModernUiTheme.DANGER, false);
		}
	}

	private void drawRecentColors(OwoUIDrawContext context, TextRenderer renderer) {
		context.drawText(renderer, Text.translatable("chat_canvas.color_picker.recent"),
				x() + metrics.margin(), y() + metrics.recentY() - 10,
				ModernUiTheme.TEXT_SECONDARY, false);
		for (int index = 0; index < 8; index++) {
			int left = x() + metrics.margin()
					+ index * (metrics.recentSize() + metrics.recentGap());
			int color = index < request.recentColors().size()
					? 0xFF000000 | request.recentColors().get(index)
					: ModernUiTheme.CONTROL_DISABLED;
			ModernUiTheme.roundedRect(context, left, y() + metrics.recentY(),
					metrics.recentSize(), metrics.recentSize(), 1, color);
			context.drawRectOutline(left, y() + metrics.recentY(),
					metrics.recentSize(), metrics.recentSize(), ModernUiTheme.PANEL_BORDER);
		}
	}

	private void drawActions(OwoUIDrawContext context, TextRenderer renderer,
							 int mouseX, int mouseY) {
		drawAction(context, renderer, x() + metrics.restoreX(), y() + metrics.buttonY(),
				metrics.restoreWidth(), metrics.buttonHeight(),
				Text.translatable("chat_canvas.color_picker.restore_default"),
				inside(mouseX, mouseY, x() + metrics.restoreX(), y() + metrics.buttonY(),
						metrics.restoreWidth(), metrics.buttonHeight()), true);
		drawAction(context, renderer, x() + metrics.cancelX(), y() + metrics.buttonY(),
				metrics.cancelWidth(), metrics.buttonHeight(),
				Text.translatable("chat_canvas.action.cancel"),
				inside(mouseX, mouseY, x() + metrics.cancelX(), y() + metrics.buttonY(),
						metrics.cancelWidth(), metrics.buttonHeight()), true);
		drawAction(context, renderer, x() + metrics.confirmX(), y() + metrics.buttonY(),
				metrics.confirmWidth(), metrics.buttonHeight(),
				Text.translatable("chat_canvas.action.confirm"),
				inside(mouseX, mouseY, x() + metrics.confirmX(), y() + metrics.buttonY(),
						metrics.confirmWidth(), metrics.buttonHeight()),
				state.hexValid());
	}

	private static void drawAction(OwoUIDrawContext context, TextRenderer renderer,
								   int x, int y, int width, int height,
								   Text label, boolean hovered, boolean active) {
		int background = !active
				? ModernUiTheme.CONTROL_DISABLED
				: hovered ? ModernUiTheme.CONTROL_HOVER : ModernUiTheme.CONTROL_BACKGROUND;
		ModernUiTheme.roundedRect(context, x, y, width, height, 1, background);
		ModernUiTheme.border(context, x, y, width, height, ModernUiTheme.PANEL_BORDER);
		int color = active ? ModernUiTheme.TEXT_PRIMARY : ModernUiTheme.TEXT_DISABLED;
		String fitted = ModernUiTheme.fitText(renderer, label, Math.max(1, width - 6));
		int textX = x + Math.max(3, (width - renderer.getWidth(fitted)) / 2);
		int textY = y + (height - renderer.fontHeight) / 2;
		context.drawText(renderer, fitted, textX, textY, color, false);
	}

	@Override
	public boolean onMouseDown(Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || closed) {
			return true;
		}
		if (hexFocused && !inside(mouseX, mouseY, metrics.svX(), metrics.hexY(),
				metrics.svWidth(), metrics.hexHeight())) {
			hexFocused = false;
			selectAllHex = false;
			if (state.hexValid()) {
				state.normalizeHexInput();
			}
		}
		if (inside(mouseX, mouseY, metrics.svX(), metrics.svY(),
				metrics.svWidth(), metrics.svHeight())) {
			dragging = DragSection.SATURATION_VALUE;
			updateFromPointer(mouseX, mouseY);
		} else if (inside(mouseX, mouseY, metrics.svX(), metrics.hueY(),
				metrics.svWidth(), metrics.hueHeight())) {
			dragging = DragSection.HUE;
			updateFromPointer(mouseX, mouseY);
		} else if (inside(mouseX, mouseY, metrics.svX(), metrics.hexY(),
				metrics.svWidth(), metrics.hexHeight())) {
			hexFocused = true;
			selectAllHex = true;
		} else if (inside(mouseX, mouseY, metrics.margin(), metrics.recentY(),
				8 * (metrics.recentSize() + metrics.recentGap()) - metrics.recentGap(),
				metrics.recentSize())) {
			int offset = (int) mouseX - metrics.margin();
			int index = offset / (metrics.recentSize() + metrics.recentGap());
			int within = offset % (metrics.recentSize() + metrics.recentGap());
			if (within < metrics.recentSize()
					&& index >= 0 && index < request.recentColors().size()) {
				applyRgb(request.recentColors().get(index));
			}
		} else if (inside(mouseX, mouseY, metrics.restoreX(), metrics.buttonY(),
				metrics.restoreWidth(), metrics.buttonHeight())) {
			applyRgb(request.defaultRgb());
		} else if (inside(mouseX, mouseY, metrics.cancelX(), metrics.buttonY(),
				metrics.cancelWidth(), metrics.buttonHeight())) {
			cancel();
		} else if (inside(mouseX, mouseY, metrics.confirmX(), metrics.buttonY(),
				metrics.confirmWidth(), metrics.buttonHeight())) {
			confirm();
		}
		return true;
	}

	@Override
	public boolean onMouseDrag(Click click, double deltaX, double deltaY) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && dragging != DragSection.NONE) {
			updateFromPointer(mouseX, mouseY);
		}
		return true;
	}

	@Override
	public boolean onMouseUp(Click click) {
		dragging = DragSection.NONE;
		return true;
	}

	@Override
	public boolean onKeyPress(KeyInput input) {
		int keyCode = input.getKeycode();
		if (!hexFocused || closed) {
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			confirm();
			return true;
		}
		if (MinecraftClient.getInstance().isCtrlPressed()
				&& keyCode == GLFW.GLFW_KEY_A) {
			selectAllHex = true;
			return true;
		}
		if (MinecraftClient.getInstance().isCtrlPressed()
				&& keyCode == GLFW.GLFW_KEY_V) {
			replaceOrAppend(MinecraftClient.getInstance().keyboard.getClipboard());
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			String hexInput = state.hexInput();
			if (selectAllHex) {
				state.updateHexInput("");
				selectAllHex = false;
			} else if (!hexInput.isEmpty()) {
				state.updateHexInput(hexInput.substring(0, hexInput.length() - 1));
			}
			return true;
		}
		return true;
	}

	@Override
	public boolean onCharTyped(CharInput input) {
		if (!hexFocused || closed || !input.isValidChar()) {
			return false;
		}
		replaceOrAppend(new String(Character.toChars(input.codepoint())));
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
			float saturation = clamp01((float) ((mouseX - metrics.svX()) / metrics.svWidth()));
			float value = 1.0f - clamp01((float) ((mouseY - metrics.svY()) / metrics.svHeight()));
			state.setHsv(state.hue(), saturation, value);
		} else if (dragging == DragSection.HUE) {
			float hue = clamp01((float) ((mouseX - metrics.svX()) / metrics.svWidth()));
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
