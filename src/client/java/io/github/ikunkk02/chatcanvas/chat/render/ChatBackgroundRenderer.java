package io.github.ikunkk02.chatcanvas.chat.render;

import io.github.ikunkk02.chatcanvas.chat.layout.ChatVerticalMetrics;

public final class ChatBackgroundRenderer {
	private static final int MESSAGE_BACKGROUND_RGB = 0x000000;
	private static final int INPUT_BACKGROUND_RGB = 0x080A0D;

	public void drawMessageBackground(ChatRenderContext context, int x, int textY, int textWidth,
									  ChatVerticalMetrics verticalMetrics, float opacity) {
		int alpha = Math.round(132 * clamp01(opacity));
		int backgroundTop = (int) Math.floor(verticalMetrics.backgroundTop(textY));
		int backgroundBottom = (int) Math.ceil(verticalMetrics.backgroundBottom(textY));
		context.drawContext().fill(x - 2, backgroundTop, x + textWidth + 2,
				backgroundBottom, argb(alpha, MESSAGE_BACKGROUND_RGB));
	}

	public void drawInputBackground(ChatRenderContext context, int y, int height) {
		int alpha = Math.round(178 * clamp01(context.inputProgress()));
		context.drawContext().fill(context.x(), y, context.right(), y + height, argb(alpha, INPUT_BACKGROUND_RGB));
	}

	private static int argb(int alpha, int rgb) {
		return (alpha << 24) | rgb;
	}

	private static float clamp01(float value) {
		return Math.max(0.0f, Math.min(1.0f, value));
	}
}
