package io.github.ikunkk02.chatcanvas.chat.render;

public final class ChatBackgroundRenderer {
	private static final int MESSAGE_BACKGROUND_RGB = 0x000000;
	private static final int INPUT_BACKGROUND_RGB = 0x080A0D;

	public void drawMessageBackground(ChatRenderContext context, int x, int y, int textWidth, float opacity) {
		int alpha = Math.round(132 * clamp01(opacity));
		context.drawContext().fill(x - 2, y - 1, x + textWidth + 2,
				y + context.textRenderer().fontHeight + 1, argb(alpha, MESSAGE_BACKGROUND_RGB));
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
