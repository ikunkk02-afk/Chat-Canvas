package io.github.ikunkk02.chatcanvas.chat.render;

import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundBounds;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundMetrics;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;

public final class ChatBackgroundRenderer {
	public void drawMessageBackground(ChatRenderContext context,
									  ChatBackgroundBounds bounds,
									  double vanillaOpacity) {
		ChatBackgroundConfig background = context.backgroundConfig().sanitized();
		int color = ChatBackgroundMetrics.composeBackgroundColor(
				background.messageColor(),
				background.messageOpacity(),
				vanillaOpacity
		);
		ChatBackgroundDraw.fill(context.graphics(), bounds, color);
	}

	public void drawInputBackground(ChatRenderContext context, int y, int height) {
		ChatBackgroundConfig background = context.backgroundConfig().sanitized();
		int color = ChatBackgroundMetrics.composeBackgroundColor(
				background.inputColor(),
				background.inputOpacity(),
				context.inputProgress()
		);
		if (color >>> 24 != 0) {
			context.graphics().fill(context.x(), y, context.right(), y + height, color);
		}
		if (background.inputBorderEnabled()) {
			int borderColor = ChatBackgroundMetrics.composeBackgroundColor(
					background.inputBorderColor(),
					background.inputBorderOpacity(),
					context.inputProgress()
			);
			ChatBackgroundDraw.drawRectBorder(
					context.graphics(), context.x(), y, context.right(), y + height, borderColor);
		}
	}
}
