package io.github.ikunkk02.chatcanvas.chat.render;

import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextRenderer;
import net.minecraft.util.FormattedCharSequence;

public final class ChatLineRenderer {
	public void draw(ChatRenderContext context, FormattedCharSequence text, int x, int y,
					 float opacity, boolean shadow) {
		int alpha = Math.round(255 * Math.max(0.0f, Math.min(1.0f, opacity)));
		double spacing = context.textConfig() == null
				? 0.0
				: context.textConfig().characterSpacing();
		SpacedTextRenderer.draw(
				context.graphics(), context.font(), text, x, y,
				(alpha << 24) | 0xFFFFFF, shadow, spacing);
	}
}
