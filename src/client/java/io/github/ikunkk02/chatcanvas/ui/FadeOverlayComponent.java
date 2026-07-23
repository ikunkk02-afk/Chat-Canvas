package io.github.ikunkk02.chatcanvas.ui;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;

public final class FadeOverlayComponent extends BaseComponent {
	private float opacity;

	public FadeOverlayComponent(float opacity) {
		this.opacity = clamp01(opacity);
		this.sizing(Sizing.fill(100), Sizing.fill(100));
	}

	public void opacity(float value) {
		opacity = clamp01(value);
	}

	@Override
	public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
		int alpha = Math.round(opacity * 205.0f);
		if (alpha > 0) {
			context.fill(x(), y(), x() + width(), y() + height(), alpha << 24 | 0x191C26);
		}
	}

	private static float clamp01(float value) {
		return Math.max(0.0f, Math.min(1.0f, value));
	}
}
