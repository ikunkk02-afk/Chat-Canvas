package io.github.ikunkk02.chatcanvas.chat.layout;

public record ChatBackgroundBounds(int left, int top, int right, int bottom) {
	public int width() {
		return Math.max(0, right - left);
	}

	public int height() {
		return Math.max(0, bottom - top);
	}

	public boolean visible() {
		return right > left && bottom > top;
	}
}
