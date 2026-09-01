package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.editor.EditorSession;
import io.github.ikunkk02.chatcanvas.editor.EditorUiStyle;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class MentionNumericScrubberComponent extends BaseUIComponent implements NumericScrubber {
	private static final int VALUE_WIDTH = 92;
	private final EditorSession session;
	private final Property property;
	private final Component label;
	private final Runnable previewChanged;
	private final Runnable historyChanged;
	private MentionConfig dragStart;
	private double dragStartMouseX;
	private double dragStartValue;
	private boolean dragging;
	private boolean changed;
	private boolean valueHovered;

	public MentionNumericScrubberComponent(
			EditorSession session, Property property, Component label,
			Runnable previewChanged, Runnable historyChanged) {
		this.session = session;
		this.property = property;
		this.label = label;
		this.previewChanged = previewChanged;
		this.historyChanged = historyChanged;
		sizing(Sizing.fill(100), Sizing.fixed(24));
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		super.update(delta, mouseX, mouseY);
		valueHovered = valueRegionContains(mouseX, mouseY);
		cursorStyle(valueHovered || dragging ? CursorStyle.HORIZONTAL_RESIZE : CursorStyle.NONE);
	}

	@Override
	public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
		Font renderer = Minecraft.getInstance().font;
		int valueLeft = valueLeft();
		boolean vanilla = ModernUiTheme.currentStyle() == EditorUiStyle.VANILLA;
		context.fill(valueLeft, y() + 2, x() + width(), y() + height() - 2,
				dragging ? (vanilla ? 0xFF777777 : ModernUiTheme.CONTROL_ACTIVE)
						: (vanilla ? 0xFF555555 : ModernUiTheme.CONTROL_BACKGROUND));
		double progress = (property.read(session.mention()) - property.min)
				/ Math.max(0.0001, property.max - property.min);
		int progressRight = valueLeft
				+ (int) Math.round((width() - (valueLeft - x())) * progress);
		context.fill(valueLeft, y() + height() - 3, progressRight, y() + height() - 2,
				dragging ? (vanilla ? 0xFFAAAAAA : ModernUiTheme.ACCENT)
						: (vanilla ? 0xFF999999 : ModernUiTheme.ACCENT_MUTED));
		int textY = y() + (height() - renderer.lineHeight) / 2;
		int labelColor = vanilla ? 0xFFFFFFFF : ModernUiTheme.TEXT_SECONDARY;
		context.text(renderer,
				ModernUiTheme.fitText(renderer, label, Math.max(1, valueLeft - x() - 8)),
				x() + 2, textY, labelColor, false);
		String value = property.format(property.read(session.mention()));
		context.text(renderer, value, x() + width() - 8 - renderer.width(value),
				textY, vanilla ? 0xFFFFFFFF : ModernUiTheme.TEXT_PRIMARY, false);
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
		dragStartValue = property.read(dragStart);
		dragging = true;
		changed = false;
		return true;
	}

	@Override
	public boolean dragPointer(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0 || dragStart == null) return false;
		double modifier = Minecraft.getInstance().hasShiftDown() ? 0.2 : Minecraft.getInstance().hasControlDown() ? 5.0 : 1.0;
		applyValue(dragStartValue + (mouseX - dragStartMouseX) * property.dragStep * modifier);
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
		double modifier = Minecraft.getInstance().hasShiftDown() ? 0.2 : Minecraft.getInstance().hasControlDown() ? 5.0 : 1.0;
		MentionConfig before = session.mention();
		applyValue(property.read(before) + Math.signum(amount) * property.scrollStep * modifier);
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
		applyValue(property.read(MentionConfig.DEFAULT));
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

	private void applyValue(double value) {
		MentionConfig before = session.mention();
		session.setMention(property.write(before, value));
		if (!before.equals(session.mention())) {
			changed = true;
			previewChanged.run();
		}
	}

	private int valueLeft() {
		return Math.max(x(), x() + width() - VALUE_WIDTH);
	}

	public enum Property {
		DOUBLE_CLICK(MentionConfig.MIN_DOUBLE_CLICK_INTERVAL_MS,
				MentionConfig.MAX_DOUBLE_CLICK_INTERVAL_MS, 1.0, 5.0) {
			double read(MentionConfig c) { return c.doubleClickIntervalMs(); }
			MentionConfig write(MentionConfig c, double v) { return c.withDoubleClickIntervalMs((int) Math.round(v)); }
			String format(double v) { return Math.round(v) + " ms"; }
		},
		SOUND_VOLUME(0.0, 1.0, 0.005, 0.01) {
			double read(MentionConfig c) { return c.soundVolume(); }
			MentionConfig write(MentionConfig c, double v) { return c.withSoundVolume(v); }
			String format(double v) { return Math.round(v * 100.0) + "%"; }
		},
		SOUND_PITCH(0.5, 2.0, 0.005, 0.05) {
			double read(MentionConfig c) { return c.soundPitch(); }
			MentionConfig write(MentionConfig c, double v) { return c.withSoundPitch(v); }
		},
		TOAST_LENGTH(20, 160, 0.25, 1.0) {
			double read(MentionConfig c) { return c.toastMessageLength(); }
			MentionConfig write(MentionConfig c, double v) { return c.withToastMessageLength((int) Math.round(v)); }
			String format(double v) { return Long.toString(Math.round(v)); }
		},
		FLASH_OPACITY(0.0, 0.6, 0.003, 0.01) {
			double read(MentionConfig c) { return c.flashOpacity(); }
			MentionConfig write(MentionConfig c, double v) { return c.withFlashOpacity(v); }
			String format(double v) { return Math.round(v * 100.0) + "%"; }
		},
		FLASH_DURATION(100, 1_500, 2.0, 25.0) {
			double read(MentionConfig c) { return c.flashDurationMs(); }
			MentionConfig write(MentionConfig c, double v) { return c.withFlashDurationMs((int) Math.round(v)); }
			String format(double v) { return Math.round(v) + " ms"; }
		};

		final double min;
		final double max;
		final double dragStep;
		final double scrollStep;

		Property(double min, double max, double dragStep, double scrollStep) {
			this.min = min;
			this.max = max;
			this.dragStep = dragStep;
			this.scrollStep = scrollStep;
		}

		abstract double read(MentionConfig config);
		abstract MentionConfig write(MentionConfig config, double value);
		String format(double value) {
			return String.format(Locale.ROOT, "%.2f", value);
		}
	}
}
