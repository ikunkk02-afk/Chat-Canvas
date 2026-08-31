package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasSettings;
import io.github.ikunkk02.chatcanvas.config.PlayerChatLayoutMode;
import io.github.ikunkk02.chatcanvas.editor.EditorChannel;
import io.github.ikunkk02.chatcanvas.editor.EditorSession;
import io.github.ikunkk02.chatcanvas.editor.EditorUiStyle;
import io.github.ikunkk02.chatcanvas.editor.NumericScrubberMath;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public final class SplitMessageRatioScrubberComponent
		extends BaseUIComponent implements NumericScrubber {
	private static final int VALUE_WIDTH = 78;

	private final EditorSession session;
	private final Text label;
	private final Runnable previewChanged;
	private final Runnable historyChanged;
	private double dragStartMouseX;
	private double dragStartValue;
	private boolean dragging;
	private boolean changed;
	private boolean valueHovered;

	public SplitMessageRatioScrubberComponent(
			EditorSession session, Text label,
			Runnable previewChanged, Runnable historyChanged) {
		this.session = session;
		this.label = label;
		this.previewChanged = previewChanged;
		this.historyChanged = historyChanged;
		this.sizing(Sizing.fill(100), Sizing.fixed(24));
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		super.update(delta, mouseX, mouseY);
		valueHovered = enabled() && valueRegionContains(mouseX, mouseY);
		cursorStyle(valueHovered || dragging
				? CursorStyle.HORIZONTAL_RESIZE : CursorStyle.NONE);
	}

	@Override
	public void draw(OwoUIGraphics context, int mouseX, int mouseY,
					 float partialTicks, float delta) {
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		boolean enabled = enabled();
		boolean vanilla = ModernUiTheme.currentStyle() == EditorUiStyle.VANILLA;
		int valueLeft = valueLeft();
		context.fill(valueLeft, y() + 2, x() + width(), y() + height() - 2,
				enabled
						? vanilla ? 0xFF555555 : ModernUiTheme.CONTROL_BACKGROUND
						: vanilla ? 0xFF333333 : ModernUiTheme.CONTROL_DISABLED);
		if (enabled) {
			int progressRight = valueLeft + (int) Math.round(
					(width() - (valueLeft - x())) * progress());
			context.fill(valueLeft, y() + height() - 3, progressRight,
					y() + height() - 2, ModernUiTheme.ACCENT_MUTED);
		}
		int textY = y() + (height() - renderer.fontHeight) / 2;
		context.drawText(renderer,
				ModernUiTheme.fitText(renderer, label, Math.max(1, valueLeft - x() - 8)),
				x() + 2, textY,
				enabled ? ModernUiTheme.TEXT_SECONDARY : ModernUiTheme.TEXT_DISABLED, false);
		String value = Math.round(session.splitMessageMaxWidthRatio() * 100.0) + "%";
		context.drawText(renderer, value,
				x() + width() - 8 - renderer.getWidth(value), textY,
				enabled ? ModernUiTheme.TEXT_PRIMARY : ModernUiTheme.TEXT_DISABLED, false);
	}

	@Override
	public boolean valueRegionContains(double mouseX, double mouseY) {
		return mouseX >= valueLeft() && mouseX <= x() + width()
				&& mouseY >= y() && mouseY <= y() + height();
	}

	@Override
	public boolean beginPointerInteraction(double mouseX, double mouseY, int button,
										   boolean shiftDown, boolean controlDown) {
		if (!enabled() || button != 0 || !valueRegionContains(mouseX, mouseY)) return false;
		dragStartMouseX = mouseX;
		dragStartValue = session.splitMessageMaxWidthRatio();
		dragging = true;
		changed = false;
		return true;
	}

	@Override
	public boolean dragPointer(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0) return false;
		double delta = NumericScrubberMath.percentagePointDelta(
				mouseX - dragStartMouseX, NumericScrubberMath.Sensitivity.NORMAL);
		apply(dragStartValue + delta * 0.01);
		return true;
	}

	@Override
	public boolean endPointerInteraction(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0) return false;
		dragging = false;
		if (changed) {
			session.commit();
			historyChanged.run();
		}
		changed = false;
		return true;
	}

	@Override
	public void cancelPointerInteraction() {
		if (!dragging) return;
		if (changed) {
			session.setSplitMessageMaxWidthRatio(dragStartValue);
			previewChanged.run();
		}
		dragging = false;
		changed = false;
	}

	@Override
	public boolean scroll(double amount) {
		if (!enabled() || !valueHovered || amount == 0.0) return false;
		double before = session.splitMessageMaxWidthRatio();
		apply(before + Math.signum(amount) * 0.01);
		if (Double.compare(before, session.splitMessageMaxWidthRatio()) != 0) {
			session.commit();
			historyChanged.run();
		}
		changed = false;
		return true;
	}

	@Override
	public boolean restoreDefault() {
		if (!enabled()) return false;
		double before = session.splitMessageMaxWidthRatio();
		apply(ChatCanvasSettings.DEFAULT_SPLIT_MESSAGE_MAX_WIDTH_RATIO);
		if (Double.compare(before, session.splitMessageMaxWidthRatio()) != 0) {
			session.commit();
			historyChanged.run();
		}
		changed = false;
		return true;
	}

	@Override
	public void resizeViewport(int width, int height) {
	}

	private void apply(double value) {
		double before = session.splitMessageMaxWidthRatio();
		session.setSplitMessageMaxWidthRatio(value);
		if (Double.compare(before, session.splitMessageMaxWidthRatio()) != 0) {
			changed = true;
			previewChanged.run();
		}
	}

	private boolean enabled() {
		return session.selectedChannel() == EditorChannel.PLAYER_CHAT
				&& session.playerChatLayoutMode() == PlayerChatLayoutMode.SPLIT_ALIGNMENT;
	}

	private int valueLeft() {
		return Math.max(x(), x() + width() - VALUE_WIDTH);
	}

	private double progress() {
		double min = ChatCanvasSettings.MIN_SPLIT_MESSAGE_MAX_WIDTH_RATIO;
		double max = ChatCanvasSettings.MAX_SPLIT_MESSAGE_MAX_WIDTH_RATIO;
		return (session.splitMessageMaxWidthRatio() - min) / (max - min);
	}
}
