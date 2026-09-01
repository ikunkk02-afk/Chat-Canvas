package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.editor.EditorSession;
import io.github.ikunkk02.chatcanvas.editor.EditorUiStyle;
import io.github.ikunkk02.chatcanvas.editor.NumericScrubberMath;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public final class TextNumericScrubberComponent extends BaseComponent implements NumericScrubber {
	private static final int VALUE_WIDTH = 78;

	private final EditorSession session;
	private final Property property;
	private final Component label;
	private final Runnable previewChanged;
	private final Runnable historyChanged;

	private ChatTextConfig dragStartText;
	private double dragStartMouseX;
	private double dragStartValue;
	private NumericScrubberMath.Sensitivity sensitivity = NumericScrubberMath.Sensitivity.NORMAL;
	private boolean dragging;
	private boolean changed;
	private boolean valueHovered;
	private float hoverProgress;

	public TextNumericScrubberComponent(EditorSession session, Property property, Component label,
										Runnable previewChanged, Runnable historyChanged) {
		this.session = session;
		this.property = property;
		this.label = label;
		this.previewChanged = previewChanged;
		this.historyChanged = historyChanged;
		this.sizing(Sizing.fill(100), Sizing.fixed(24));
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		super.update(delta, mouseX, mouseY);
		valueHovered = valueRegionContains(mouseX, mouseY);
		float target = valueHovered || dragging ? 1.0f : 0.0f;
		hoverProgress += (target - hoverProgress) * Math.min(1.0f, Math.max(0.08f, delta * 0.35f));
		cursorStyle(valueHovered || dragging ? CursorStyle.HORIZONTAL_RESIZE : CursorStyle.NONE);
	}

	@Override
	public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
		Font renderer = Minecraft.getInstance().font;
		int valueLeft = valueLeft();
		boolean vanilla = ModernUiTheme.currentStyle() == EditorUiStyle.VANILLA;
		int background = dragging
				? (vanilla ? 0xFF777777 : ModernUiTheme.CONTROL_ACTIVE)
				: (vanilla ? 0xFF555555
					: interpolateColor(0x88222222, ModernUiTheme.CONTROL_HOVER, hoverProgress));
		context.fill(valueLeft, y() + 2, x() + width(), y() + height() - 2, background);
		if (!vanilla) {
			context.fill(valueLeft, y() + height() - 3, x() + width(), y() + height() - 2,
					ModernUiTheme.DIVIDER);
		}

		int progressRight = valueLeft + (int) Math.round((width() - (valueLeft - x())) * valueProgress());
		if (progressRight > valueLeft) {
			context.fill(valueLeft, y() + height() - 3, progressRight, y() + height() - 2,
					dragging ? (vanilla ? 0xFFAAAAAA : ModernUiTheme.ACCENT)
							: (vanilla ? 0xFF999999 : ModernUiTheme.ACCENT_MUTED));
		}

		int textY = y() + (height() - renderer.lineHeight) / 2;
		int labelColor = vanilla ? 0xFFFFFFFF : ModernUiTheme.TEXT_SECONDARY;
		context.drawString(renderer,
				ModernUiTheme.fitText(renderer, label, Math.max(1, valueLeft - x() - 8)),
				x() + 2, textY, labelColor, false);
		String value = displayValue();
		int valueX = x() + width() - 8 - renderer.width(value);
		context.drawString(renderer, value, valueX, textY,
				dragging ? 0xFFFFFFFF : (vanilla ? 0xFFFFFFFF : ModernUiTheme.TEXT_PRIMARY), false);
		if (valueHovered || dragging) {
			context.drawString(renderer, "\u2194", valueLeft + 6, textY,
					vanilla ? 0xFFFFFFFF : ModernUiTheme.TEXT_SECONDARY, false);
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
		dragStartText = session.text();
		dragStartMouseX = mouseX;
		dragStartValue = property.read(dragStartText);
		sensitivity = NumericScrubberMath.Sensitivity.fromModifiers(shiftDown, controlDown);
		dragging = true;
		changed = false;
		return true;
	}

	@Override
	public boolean dragPointer(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0 || dragStartText == null) return false;
		double percentagePointDelta = NumericScrubberMath.percentagePointDelta(
				mouseX - dragStartMouseX, sensitivity);
		applyValue(dragStartValue + property.dragDelta(percentagePointDelta));
		return true;
	}

	@Override
	public boolean endPointerInteraction(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0) return false;
		dragging = false;
		dragStartText = null;
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
		if (changed && dragStartText != null) {
			session.setText(dragStartText);
			previewChanged.run();
		}
		dragging = false;
		dragStartText = null;
		changed = false;
	}

	@Override
	public boolean scroll(double amount) {
		if (!valueHovered || amount == 0.0) return false;
		ChatTextConfig before = session.text();
		applyValue(property.read(before) + Math.signum(amount) * property.scrollStep());
		if (!before.equals(session.text())) {
			session.commit();
			historyChanged.run();
		}
		changed = false;
		return true;
	}

	@Override
	public boolean restoreDefault() {
		ChatTextConfig before = session.text();
		applyValue(property.defaultValue());
		if (!before.equals(session.text())) {
			session.commit();
			historyChanged.run();
		}
		changed = false;
		return true;
	}

	@Override
	public void resizeViewport(int width, int height) {
		// Percentage controls are independent of the viewport.
	}

	private void applyValue(double value) {
		ChatTextConfig before = session.text();
		session.setText(property.write(before, value));
		if (!before.equals(session.text())) {
			changed = true;
			previewChanged.run();
		}
	}

	private double currentValue() {
		return property.read(session.text());
	}

	private int valueLeft() {
		return Math.max(x(), x() + width() - VALUE_WIDTH);
	}

	private double valueProgress() {
		return Math.max(0.0, Math.min(1.0,
				(currentValue() - property.min()) / (property.max() - property.min())));
	}

	private String displayValue() {
		if (!property.percentage()) {
			return String.format(Locale.ROOT, "%.1f", currentValue());
		}
		double percent = currentValue() * 100.0;
		double rounded = Math.rint(percent);
		if (Math.abs(percent - rounded) < 0.001) {
			return (int) rounded + "%";
		}
		return String.format(Locale.ROOT, "%.1f%%", percent);
	}

	private static int interpolateColor(int from, int to, float progress) {
		int a = lerp(from >>> 24, to >>> 24, progress);
		int r = lerp(from >>> 16 & 0xFF, to >>> 16 & 0xFF, progress);
		int g = lerp(from >>> 8 & 0xFF, to >>> 8 & 0xFF, progress);
		int b = lerp(from & 0xFF, to & 0xFF, progress);
		return a << 24 | r << 16 | g << 8 | b;
	}

	private static int lerp(int from, int to, float progress) {
		return Math.round(from + (to - from) * progress);
	}

	public enum Property {
		FONT_SCALE(ChatTextConfig.MIN_FONT_SCALE, ChatTextConfig.MAX_FONT_SCALE,
				ChatTextConfig.DEFAULT.fontScale(), true, 0.01, 0.01) {
			@Override
			double read(ChatTextConfig config) {
				return config.fontScale();
			}

			@Override
			ChatTextConfig write(ChatTextConfig config, double value) {
				return new ChatTextConfig(value, config.lineSpacing(), config.textOpacity(),
						config.alignment(), config.shadow());
			}
		},
		LINE_SPACING(ChatTextConfig.MIN_LINE_SPACING, ChatTextConfig.MAX_LINE_SPACING,
				ChatTextConfig.DEFAULT.lineSpacing(), true, 0.01, 0.01) {
			@Override
			double read(ChatTextConfig config) {
				return config.lineSpacing();
			}

			@Override
			ChatTextConfig write(ChatTextConfig config, double value) {
				return new ChatTextConfig(config.fontScale(), value, config.textOpacity(),
						config.alignment(), config.shadow());
			}
		},
		TEXT_OPACITY(ChatTextConfig.MIN_TEXT_OPACITY, ChatTextConfig.MAX_TEXT_OPACITY,
				ChatTextConfig.DEFAULT.textOpacity(), true, 0.01, 0.01) {
			@Override
			double read(ChatTextConfig config) {
				return config.textOpacity();
			}

			@Override
			ChatTextConfig write(ChatTextConfig config, double value) {
				return new ChatTextConfig(config.fontScale(), config.lineSpacing(), value,
						config.alignment(), config.shadow());
			}
		},
		CHARACTER_SPACING(ChatTextConfig.MIN_CHARACTER_SPACING,
				ChatTextConfig.MAX_CHARACTER_SPACING,
				ChatTextConfig.DEFAULT.characterSpacing(), false, 0.1, 0.1) {
			@Override
			double read(ChatTextConfig config) {
				return config.characterSpacing();
			}

			@Override
			ChatTextConfig write(ChatTextConfig config, double value) {
				return config.withCharacterSpacing(value);
			}
		};

		private final double min;
		private final double max;
		private final double defaultValue;
		private final boolean percentage;
		private final double dragStep;
		private final double scrollStep;

		Property(double min, double max, double defaultValue, boolean percentage,
				 double dragStep, double scrollStep) {
			this.min = min;
			this.max = max;
			this.defaultValue = defaultValue;
			this.percentage = percentage;
			this.dragStep = dragStep;
			this.scrollStep = scrollStep;
		}

		abstract double read(ChatTextConfig config);

		abstract ChatTextConfig write(ChatTextConfig config, double value);

		double min() {
			return min;
		}

		double max() {
			return max;
		}

		double defaultValue() {
			return defaultValue;
		}

		boolean percentage() {
			return percentage;
		}

		double dragDelta(double sensitivityUnits) {
			return sensitivityUnits * dragStep;
		}

		double scrollStep() {
			return scrollStep;
		}
	}
}
