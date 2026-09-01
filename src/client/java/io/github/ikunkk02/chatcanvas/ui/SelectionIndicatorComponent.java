package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.editor.EditorUiStyle;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;

import java.util.function.IntSupplier;
import java.util.function.DoubleSupplier;

public final class SelectionIndicatorComponent extends BaseUIComponent {
	private final IntSupplier selectedIndex;
	private final DoubleSupplier directPosition;
	private final int optionCount;
	private double animatedIndex;

	public SelectionIndicatorComponent(IntSupplier selectedIndex, int optionCount) {
		this.selectedIndex = selectedIndex;
		this.directPosition = null;
		this.optionCount = Math.max(1, optionCount);
		this.animatedIndex = clampIndex(selectedIndex.getAsInt());
		this.sizing(Sizing.fill(100), Sizing.fill(100));
	}

	private SelectionIndicatorComponent(DoubleSupplier directPosition, int optionCount) {
		this.selectedIndex = null;
		this.directPosition = directPosition;
		this.optionCount = Math.max(1, optionCount);
		this.animatedIndex = clampPosition(directPosition.getAsDouble());
		this.sizing(Sizing.fill(100), Sizing.fill(100));
	}

	public static SelectionIndicatorComponent following(DoubleSupplier position, int optionCount) {
		return new SelectionIndicatorComponent(position, optionCount);
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		super.update(delta, mouseX, mouseY);
		if (directPosition != null) return;
		double target = clampIndex(selectedIndex.getAsInt());
		double factor = Math.min(1.0, Math.max(0.08, delta * 0.45));
		animatedIndex += (target - animatedIndex) * factor;
	}

	@Override
	public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
		int segmentWidth = Math.max(1, width() / optionCount);
		double position = directPosition == null
				? animatedIndex
				: clampPosition(directPosition.getAsDouble());
		int indicatorX = x() + (int) Math.round(position * segmentWidth);
		boolean vanilla = ModernUiTheme.currentStyle() == EditorUiStyle.VANILLA;
		if (vanilla) {
			context.fill(x(), y(), x() + width(), y() + height(), 0xFF333333);
			int selLeft = indicatorX + 1;
			int selTop = y() + 1;
			int selRight = Math.min(x() + width() - 1, indicatorX + segmentWidth - 1);
			int selBottom = y() + height() - 1;
			if (selRight > selLeft && selBottom > selTop) {
				context.fill(selLeft, selTop, selRight, selBottom, 0xFF666666);
			}
		} else {
			ModernUiTheme.roundedRect(context, x(), y(), width(), height(), 2,
					ModernUiTheme.CONTROL_BACKGROUND);
			ModernUiTheme.roundedRect(context, indicatorX + 1, y() + 1,
					Math.max(1, segmentWidth - 2), Math.max(1, height() - 2), 1,
					ModernUiTheme.CONTROL_ACTIVE);
			context.fill(indicatorX + 2, y() + height() - 2,
					Math.min(x() + width() - 2, indicatorX + segmentWidth - 2),
					y() + height() - 1, ModernUiTheme.ACCENT);
		}
	}

	private int clampIndex(int index) {
		return Math.max(0, Math.min(optionCount - 1, index));
	}

	private double clampPosition(double position) {
		if (!Double.isFinite(position)) return 0.0;
		return Math.max(0.0, Math.min(optionCount - 1.0, position));
	}
}
