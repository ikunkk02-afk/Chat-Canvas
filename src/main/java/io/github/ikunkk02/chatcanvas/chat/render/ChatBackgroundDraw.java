package io.github.ikunkk02.chatcanvas.chat.render;

import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundBounds;
import net.minecraft.client.gui.GuiGraphics;

public final class ChatBackgroundDraw {
	private ChatBackgroundDraw() {
	}

	public static void fill(GuiGraphics context, ChatBackgroundBounds bounds, int color) {
		if (bounds == null || !bounds.visible() || color >>> 24 == 0) {
			return;
		}
		context.fill(bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), color);
	}

	public static void drawRectBorder(GuiGraphics context, int left, int top,
									  int right, int bottom, int color) {
		if (color >>> 24 == 0 || right - left <= 1 || bottom - top <= 1) {
			return;
		}
		context.fill(left, top, right, top + 1, color);
		context.fill(left, bottom - 1, right, bottom, color);
		if (bottom - top > 2) {
			context.fill(left, top + 1, left + 1, bottom - 1, color);
			context.fill(right - 1, top + 1, right, bottom - 1, color);
		}
	}
}
