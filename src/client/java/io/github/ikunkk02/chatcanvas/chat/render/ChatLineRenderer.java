package io.github.ikunkk02.chatcanvas.chat.render;

import net.minecraft.text.OrderedText;

public final class ChatLineRenderer {
	public void draw(ChatRenderContext context, OrderedText text, int x, int y,
					 float opacity, boolean shadow) {
		int alpha = Math.round(255 * Math.max(0.0f, Math.min(1.0f, opacity)));
		context.drawContext().drawText(context.textRenderer(), text, x, y,
				(alpha << 24) | 0xFFFFFF, shadow);
	}
}
