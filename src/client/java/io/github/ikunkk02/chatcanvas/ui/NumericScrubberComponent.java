package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.LayoutConfig;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.editor.EditorPointerTarget;
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

public final class NumericScrubberComponent extends BaseUIComponent implements NumericScrubber {
	private static final int VALUE_WIDTH = 78;

	private final EditorSession session;
	private final Property property;
	private final Text label;
	private final Runnable geometryChanged;
	private final Runnable historyChanged;

	private PixelLayout dragStartLayout;
	private double dragStartMouseX;
	private int dragStartValue;
	private NumericScrubberMath.Sensitivity sensitivity = NumericScrubberMath.Sensitivity.NORMAL;
	private boolean dragging;
	private boolean changed;
	private boolean valueHovered;
	private float hoverProgress;
	private int screenWidth;
	private int screenHeight;

	public NumericScrubberComponent(EditorSession session, Property property, Text label,
									int screenWidth, int screenHeight,
									Runnable geometryChanged, Runnable historyChanged) {
		this.session = session;
		this.property = property;
		this.label = label;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.geometryChanged = geometryChanged;
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
	public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		int valueLeft = valueLeft();
		boolean vanilla = ModernUiTheme.currentStyle() == EditorUiStyle.VANILLA;
		int background = dragging
				? (vanilla ? 0xFF777777 : 0xCC29384D)
				: (vanilla ? 0xFF555555
					: interpolateColor(0x88202731, 0xB02A3543, hoverProgress));
		context.fill(valueLeft, y() + 2, x() + width(), y() + height() - 2, background);
		if (!vanilla) {
			context.fill(valueLeft, y() + height() - 3, x() + width(), y() + height() - 2, 0x553F526A);
		}

		double progress = valueProgress();
		int progressRight = valueLeft + (int) Math.round((width() - (valueLeft - x())) * progress);
		if (progressRight > valueLeft) {
			context.fill(valueLeft, y() + height() - 3, progressRight, y() + height() - 2,
					dragging ? (vanilla ? 0xFFAAAAAA : 0xFF8EB8FF)
							: (vanilla ? 0xFF999999 : 0xCC6E9ED8));
		}

		int textY = y() + (height() - renderer.fontHeight) / 2;
		int labelColor = vanilla ? 0xFFFFFFFF : 0xFFC7CEDA;
		context.drawText(renderer, label, x() + 2, textY, labelColor, false);
		String value = Integer.toString(currentValue());
		int valueX = x() + width() - 8 - renderer.getWidth(value);
		context.drawText(renderer, value, valueX, textY, dragging ? 0xFFFFFFFF : (vanilla ? 0xFFFFFFFF : 0xFFE9EDF4), false);
		if (valueHovered || dragging) {
			context.drawText(renderer, "\u2194", valueLeft + 6, textY, vanilla ? 0xFFFFFFFF : 0xFFA9B9CF, false);
		}
	}

	public boolean valueRegionContains(double mouseX, double mouseY) {
		return mouseX >= valueLeft() && mouseX <= x() + width()
				&& mouseY >= y() && mouseY <= y() + height();
	}

	@Override
	public boolean beginPointerInteraction(double mouseX, double mouseY, int button,
										   boolean shiftDown, boolean controlDown) {
		if (button != 0 || !valueRegionContains(mouseX, mouseY)) return false;
		dragStartLayout = session.layout();
		dragStartMouseX = mouseX;
		dragStartValue = currentValue();
		sensitivity = NumericScrubberMath.Sensitivity.fromModifiers(shiftDown, controlDown);
		dragging = true;
		changed = false;
		return true;
	}

	@Override
	public boolean dragPointer(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0 || dragStartLayout == null) return false;
		int delta = NumericScrubberMath.valueDelta(mouseX - dragStartMouseX, sensitivity);
		applyValue(dragStartValue + delta);
		return true;
	}

	@Override
	public boolean endPointerInteraction(double mouseX, double mouseY, int button) {
		if (!dragging || button != 0) return false;
		dragging = false;
		dragStartLayout = null;
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
		if (changed && dragStartLayout != null) {
			session.setLayout(dragStartLayout);
			geometryChanged.run();
		}
		dragging = false;
		dragStartLayout = null;
		changed = false;
	}

	public boolean scroll(double amount) {
		if (!valueHovered || amount == 0.0) return false;
		int step = amount > 0.0 ? 1 : -1;
		PixelLayout before = session.layout();
		applyValue(currentValue() + step);
		if (!before.equals(session.layout())) {
			session.commit();
			historyChanged.run();
		}
		changed = false;
		return true;
	}

	public boolean restoreDefault() {
		PixelLayout defaults = LayoutConfig.DEFAULT.toPixels(screenWidth, screenHeight);
		PixelLayout before = session.layout();
		applyValue(property.read(defaults));
		if (!before.equals(session.layout())) {
			session.commit();
			historyChanged.run();
		}
		changed = false;
		return true;
	}

	public void resizeViewport(int width, int height) {
		screenWidth = Math.max(1, width);
		screenHeight = Math.max(1, height);
	}

	private void applyValue(int value) {
		PixelLayout before = session.layout();
		PixelLayout next = property.write(before, value);
		session.setLayout(next);
		if (!before.equals(session.layout())) {
			changed = true;
			geometryChanged.run();
		}
	}

	private int currentValue() {
		return property.read(session.layout());
	}

	private int valueLeft() {
		return Math.max(x(), x() + width() - VALUE_WIDTH);
	}

	private double valueProgress() {
		PixelLayout layout = session.layout();
		int margin = PixelLayout.DEFAULT_SAFE_MARGIN;
		int min;
		int max;
		switch (property) {
			case X -> {
				min = margin;
				max = Math.max(min, screenWidth - margin - layout.width());
			}
			case Y -> {
				min = margin;
				max = Math.max(min, screenHeight - margin - layout.height());
			}
			case WIDTH -> {
				min = Math.min(PixelLayout.DEFAULT_MIN_WIDTH, Math.max(1, screenWidth - margin * 2));
				max = Math.max(min, screenWidth - margin * 2);
			}
			case HEIGHT -> {
				min = Math.min(PixelLayout.DEFAULT_MIN_HEIGHT, Math.max(1, screenHeight - margin * 2));
				max = Math.max(min, screenHeight - margin * 2);
			}
			default -> throw new IllegalStateException("Unexpected property " + property);
		}
		if (max == min) return 1.0;
		return Math.max(0.0, Math.min(1.0, (currentValue() - min) / (double) (max - min)));
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
		X {
			@Override
			int read(PixelLayout layout) {
				return layout.x();
			}

			@Override
			PixelLayout write(PixelLayout layout, int value) {
				return new PixelLayout(value, layout.y(), layout.width(), layout.height());
			}
		},
		Y {
			@Override
			int read(PixelLayout layout) {
				return layout.y();
			}

			@Override
			PixelLayout write(PixelLayout layout, int value) {
				return new PixelLayout(layout.x(), value, layout.width(), layout.height());
			}
		},
		WIDTH {
			@Override
			int read(PixelLayout layout) {
				return layout.width();
			}

			@Override
			PixelLayout write(PixelLayout layout, int value) {
				return new PixelLayout(layout.x(), layout.y(), value, layout.height());
			}
		},
		HEIGHT {
			@Override
			int read(PixelLayout layout) {
				return layout.height();
			}

			@Override
			PixelLayout write(PixelLayout layout, int value) {
				return new PixelLayout(layout.x(), layout.y(), layout.width(), value);
			}
		};

		abstract int read(PixelLayout layout);

		abstract PixelLayout write(PixelLayout layout, int value);
	}
}
