package io.github.ikunkk02.chatcanvas.chat.render;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public record ChatRenderContext(
		OwoUIDrawContext drawContext,
		TextRenderer textRenderer,
		int x,
		int y,
		int width,
		int height,
		float messageOpacity,
		float inputProgress,
		Text inputPlaceholder
) {
	public int right() {
		return x + width;
	}

	public int bottom() {
		return y + height;
	}
}
