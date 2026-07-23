package io.github.ikunkk02.chatcanvas.ui;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;

import java.util.function.IntSupplier;

public final class SelectionIndicatorComponent extends BaseComponent {
	private final IntSupplier selectedIndex;
	private final int optionCount;
	private double animatedIndex;

	public SelectionIndicatorComponent(IntSupplier selectedIndex, int optionCount) {
		this.selectedIndex = selectedIndex;
		this.optionCount = Math.max(1, optionCount);
		this.animatedIndex = clampIndex(selectedIndex.getAsInt());
		this.sizing(Sizing.fill(100), Sizing.fill(100));
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		super.update(delta, mouseX, mouseY);
		double target = clampIndex(selectedIndex.getAsInt());
		double factor = Math.min(1.0, Math.max(0.08, delta * 0.45));
		animatedIndex += (target - animatedIndex) * factor;
	}

	@Override
	public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
		int segmentWidth = Math.max(1, width() / optionCount);
		int indicatorX = x() + (int) Math.round(animatedIndex * segmentWidth);
		ModernUiTheme.roundedRect(context, x(), y(), width(), height(), 5, 0x7A202731);
		ModernUiTheme.roundedRect(context, indicatorX + 1, y() + 1,
				Math.max(1, segmentWidth - 2), Math.max(1, height() - 2), 4, 0xC53A536F);
	}

	private int clampIndex(int index) {
		return Math.max(0, Math.min(optionCount - 1, index));
	}
}
