package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
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

public final class BackgroundNumericScrubberComponent extends BaseComponent implements NumericScrubber {
	private static final int VALUE_WIDTH = 78;

	private final EditorSession session;
	private final Property property;
	private final Component label;
	private final Runnable previewChanged;
	private final Runnable historyChanged;

	private ChatBackgroundConfig dragStartBackground;
	private double dragStartMouseX;
	private double dragStartValue;
	private NumericScrubberMath.Sensitivity sensitivity = NumericScrubberMath.Sensitivity.NORMAL;
	private boolean dragging;
	private boolean changed;
	private boolean valueHovered;
	private float hoverProgress;

	public BackgroundNumericScrubberComponent(EditorSession session, Property property, Component label,
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

		int progressRight = valueLeft + (int) Math.round(
				(width() - (valueLeft - x())) * valueProgress());
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
		dragStartBackground = session.background();
		dragStartMouseX = mouseX;
		dragStartValue = property.read(dragStartBackground);
		sensitivity = NumericScrubberMath.Sensitivity.fromModifiers(shiftDown, controlDown);
		dragging = true;
		changed = false;
		return true;
	}

	@Override
	public boolean dragPointer(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0 || dragStartBackground == null) return false;
		double delta = property.percentage()
				? NumericScrubberMath.percentagePointDelta(
						mouseX - dragStartMouseX, sensitivity) / 100.0
				: NumericScrubberMath.valueDelta(mouseX - dragStartMouseX, sensitivity);
		applyValue(dragStartValue + delta);
		return true;
	}

	@Override
	public boolean endPointerInteraction(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0) return false;
		dragging = false;
		dragStartBackground = null;
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
		if (changed && dragStartBackground != null) {
			session.setBackground(dragStartBackground);
			previewChanged.run();
		}
		dragging = false;
		dragStartBackground = null;
		changed = false;
	}

	@Override
	public boolean scroll(double amount) {
		if (!valueHovered || amount == 0.0) return false;
		ChatBackgroundConfig before = session.background();
		double step = property.percentage() ? 0.01 : 1.0;
		applyValue(property.read(before) + (amount > 0.0 ? step : -step));
		if (!before.equals(session.background())) {
			session.commit();
			historyChanged.run();
		}
		changed = false;
		return true;
	}

	@Override
	public boolean restoreDefault() {
		ChatBackgroundConfig before = session.background();
		applyValue(property.defaultValue());
		if (!before.equals(session.background())) {
			session.commit();
			historyChanged.run();
		}
		changed = false;
		return true;
	}

	@Override
	public void resizeViewport(int width, int height) {
		// Background values are independent of the viewport.
	}

	private void applyValue(double value) {
		ChatBackgroundConfig before = session.background();
		session.setBackground(property.write(before, value));
		if (!before.equals(session.background())) {
			changed = true;
			previewChanged.run();
		}
	}

	private int valueLeft() {
		return Math.max(x(), x() + width() - VALUE_WIDTH);
	}

	private double valueProgress() {
		return Math.max(0.0, Math.min(1.0,
				(property.read(session.background()) - property.min())
						/ (property.max() - property.min())));
	}

	private String displayValue() {
		double value = property.read(session.background());
		if (property.percentage()) {
			double percent = value * 100.0;
			double rounded = Math.rint(percent);
			return Math.abs(percent - rounded) < 0.001
					? (int) rounded + "%"
					: String.format(Locale.ROOT, "%.1f%%", percent);
		}
		return Integer.toString((int) Math.round(value));
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
		MESSAGE_OPACITY(0.0, 1.0, ChatBackgroundConfig.DEFAULT.messageOpacity(), true) {
			@Override
			double read(ChatBackgroundConfig config) {
				return config.messageOpacity();
			}

			@Override
			ChatBackgroundConfig write(ChatBackgroundConfig config, double value) {
				return copy(config, value, config.horizontalPadding(), config.verticalPadding(),
						config.inputOpacity(), config.inputBorderOpacity());
			}
		},
		HORIZONTAL_PADDING(0, 12, ChatBackgroundConfig.DEFAULT.horizontalPadding(), false) {
			@Override
			double read(ChatBackgroundConfig config) {
				return config.horizontalPadding();
			}

			@Override
			ChatBackgroundConfig write(ChatBackgroundConfig config, double value) {
				return copy(config, config.messageOpacity(), (int) Math.round(value),
						config.verticalPadding(), config.inputOpacity(), config.inputBorderOpacity());
			}
		},
		VERTICAL_PADDING(0, 6, ChatBackgroundConfig.DEFAULT.verticalPadding(), false) {
			@Override
			double read(ChatBackgroundConfig config) {
				return config.verticalPadding();
			}

			@Override
			ChatBackgroundConfig write(ChatBackgroundConfig config, double value) {
				return copy(config, config.messageOpacity(), config.horizontalPadding(),
						(int) Math.round(value), config.inputOpacity(), config.inputBorderOpacity());
			}
		},
		INPUT_OPACITY(0.0, 1.0, ChatBackgroundConfig.DEFAULT.inputOpacity(), true) {
			@Override
			double read(ChatBackgroundConfig config) {
				return config.inputOpacity();
			}

			@Override
			ChatBackgroundConfig write(ChatBackgroundConfig config, double value) {
				return copy(config, config.messageOpacity(), config.horizontalPadding(),
						config.verticalPadding(), value, config.inputBorderOpacity());
			}
		},
		BORDER_OPACITY(0.0, 1.0, ChatBackgroundConfig.DEFAULT.inputBorderOpacity(), true) {
			@Override
			double read(ChatBackgroundConfig config) {
				return config.inputBorderOpacity();
			}

			@Override
			ChatBackgroundConfig write(ChatBackgroundConfig config, double value) {
				return copy(config, config.messageOpacity(), config.horizontalPadding(),
						config.verticalPadding(), config.inputOpacity(), value);
			}
		};

		private final double min;
		private final double max;
		private final double defaultValue;
		private final boolean percentage;

		Property(double min, double max, double defaultValue, boolean percentage) {
			this.min = min;
			this.max = max;
			this.defaultValue = defaultValue;
			this.percentage = percentage;
		}

		abstract double read(ChatBackgroundConfig config);

		abstract ChatBackgroundConfig write(ChatBackgroundConfig config, double value);

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

		private static ChatBackgroundConfig copy(
				ChatBackgroundConfig config,
				double messageOpacity,
				int horizontalPadding,
				int verticalPadding,
				double inputOpacity,
				double borderOpacity
		) {
			return new ChatBackgroundConfig(
					config.messageMode(),
					config.messageColor(),
					messageOpacity,
					horizontalPadding,
					verticalPadding,
					config.inputColor(),
					inputOpacity,
					config.inputBorderEnabled(),
					config.inputBorderColor(),
					borderOpacity
			).sanitized();
		}
	}
}
