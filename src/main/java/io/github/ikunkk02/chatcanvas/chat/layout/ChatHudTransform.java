package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.PixelLayout;

public record ChatHudTransform(PixelLayout layout, int scaledWindowHeight, double chatScale,
							   RuntimeChatBounds bounds) {
	public static final int VANILLA_OFFSET_FROM_BOTTOM = 40;

	public ChatHudTransform {
		scaledWindowHeight = Math.max(1, scaledWindowHeight);
		chatScale = Double.isFinite(chatScale) && chatScale > 0.0 ? chatScale : 1.0;
		if (bounds == null) {
			bounds = RuntimeChatBounds.calculate(layout, false, 0, 0, 1);
		}
	}

	public ChatHudTransform(PixelLayout layout, int scaledWindowHeight, double chatScale) {
		this(layout, scaledWindowHeight, chatScale,
				RuntimeChatBounds.calculate(layout, false, 0, 0, 1));
	}

	public double offsetX() {
		return layout.x();
	}

	public double offsetY() {
		return bounds.messageBottom() - vanillaBottom();
	}

	public int vanillaBottom() {
		return scaledWindowHeight - VANILLA_OFFSET_FROM_BOTTOM;
	}

	public double screenToChatX(double screenX) {
		return screenX - offsetX();
	}

	public double screenToChatY(double screenY) {
		return screenY - offsetY();
	}

	public double chatToScreenX(double chatX) {
		return chatX + offsetX();
	}

	public double chatToScreenY(double chatY) {
		return chatY + offsetY();
	}

	public int configuredWidth() {
		return layout.width();
	}

	public int configuredInternalHeight() {
		return Math.max(1, (int) Math.floor(bounds.messageHeight() / chatScale));
	}

	public int internalWrapWidth() {
		return Math.max(1, (int) Math.floor(layout.width() / chatScale));
	}
}
