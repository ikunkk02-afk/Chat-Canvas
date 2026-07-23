package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.editor.EditorSession;
import io.github.ikunkk02.chatcanvas.editor.NumericScrubberMath;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public final class MentionNumericScrubberComponent extends BaseComponent implements NumericScrubber {
	private static final int VALUE_WIDTH = 82;

	private final EditorSession session;
	private final Text label;
	private final Runnable previewChanged;
	private final Runnable historyChanged;
	private MentionConfig dragStart;
	private double dragStartMouseX;
	private int dragStartValue;
	private NumericScrubberMath.Sensitivity sensitivity = NumericScrubberMath.Sensitivity.NORMAL;
	private boolean dragging;
	private boolean changed;
	private boolean valueHovered;
	private float hoverProgress;

	public MentionNumericScrubberComponent(
			EditorSession session, Text label,
			Runnable previewChanged, Runnable historyChanged) {
		this.session = session;
		this.label = label;
		this.previewChanged = previewChanged;
		this.historyChanged = historyChanged;
		sizing(Sizing.fill(100), Sizing.fixed(24));
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		super.update(delta, mouseX, mouseY);
		valueHovered = valueRegionContains(mouseX, mouseY);
		float target = valueHovered || dragging ? 1.0f : 0.0f;
		hoverProgress += (target - hoverProgress)
				* Math.min(1.0f, Math.max(0.08f, delta * 0.35f));
		cursorStyle(valueHovered || dragging ? CursorStyle.HORIZONTAL_RESIZE : CursorStyle.NONE);
	}

	@Override
	public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		int valueLeft = valueLeft();
		int background = dragging ? 0xCC29384D : 0xB02A3543;
		context.fill(valueLeft, y() + 2, x() + width(), y() + height() - 2, background);
		int range = MentionConfig.MAX_DOUBLE_CLICK_INTERVAL_MS
				- MentionConfig.MIN_DOUBLE_CLICK_INTERVAL_MS;
		double progress = (session.mention().doubleClickIntervalMs()
				- MentionConfig.MIN_DOUBLE_CLICK_INTERVAL_MS) / (double) range;
		int progressRight = valueLeft
				+ (int) Math.round((width() - (valueLeft - x())) * progress);
		context.fill(valueLeft, y() + height() - 3, progressRight, y() + height() - 2,
				dragging ? 0xFF8EB8FF : 0xCC6E9ED8);
		int textY = y() + (height() - renderer.fontHeight) / 2;
		context.drawText(renderer, label, x() + 2, textY, 0xFFC7CEDA, false);
		String value = session.mention().doubleClickIntervalMs() + " "
				+ Text.translatable("chat_canvas.unit.milliseconds").getString();
		context.drawText(renderer, value, x() + width() - 8 - renderer.getWidth(value),
				textY, 0xFFE9EDF4, false);
		if (valueHovered || dragging) {
			context.drawText(renderer, "\u2194", valueLeft + 6, textY, 0xFFA9B9CF, false);
		}
	}

	@Override
	public boolean valueRegionContains(double mouseX, double mouseY) {
		return mouseX >= valueLeft() && mouseX <= x() + width()
				&& mouseY >= y() && mouseY <= y() + height();
	}

	@Override
	public boolean beginPointerInteraction(double mouseX, double mouseY, int button,
										   boolean shiftDown, boolean controlDown) {
		if (button != 0 || !valueRegionContains(mouseX, mouseY)) return false;
		dragStart = session.mention();
		dragStartMouseX = mouseX;
		dragStartValue = dragStart.doubleClickIntervalMs();
		sensitivity = NumericScrubberMath.Sensitivity.fromModifiers(shiftDown, controlDown);
		dragging = true;
		changed = false;
		return true;
	}

	@Override
	public boolean dragPointer(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0 || dragStart == null) return false;
		applyValue(dragStartValue
				+ NumericScrubberMath.valueDelta(mouseX - dragStartMouseX, sensitivity));
		return true;
	}

	@Override
	public boolean endPointerInteraction(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0) return false;
		dragging = false;
		dragStart = null;
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
		if (changed && dragStart != null) {
			session.setMention(dragStart);
			previewChanged.run();
		}
		dragging = false;
		dragStart = null;
		changed = false;
	}

	@Override
	public boolean scroll(double amount) {
		if (!valueHovered || amount == 0.0) return false;
		MentionConfig before = session.mention();
		int step = MinecraftClient.getInstance().currentScreen != null
				&& net.minecraft.client.gui.screen.Screen.hasControlDown() ? 25
				: net.minecraft.client.gui.screen.Screen.hasShiftDown() ? 1 : 5;
		applyValue(before.doubleClickIntervalMs() + (amount > 0.0 ? step : -step));
		if (!before.equals(session.mention())) {
			session.commit();
			historyChanged.run();
		}
		changed = false;
		return true;
	}

	@Override
	public boolean restoreDefault() {
		MentionConfig before = session.mention();
		applyValue(MentionConfig.DEFAULT.doubleClickIntervalMs());
		if (!before.equals(session.mention())) {
			session.commit();
			historyChanged.run();
		}
		changed = false;
		return true;
	}

	@Override
	public void resizeViewport(int width, int height) {
	}

	private void applyValue(int value) {
		MentionConfig before = session.mention();
		session.setMention(before.withDoubleClickIntervalMs(value));
		if (!before.equals(session.mention())) {
			changed = true;
			previewChanged.run();
		}
	}

	private int valueLeft() {
		return Math.max(x(), x() + width() - VALUE_WIDTH);
	}
}
