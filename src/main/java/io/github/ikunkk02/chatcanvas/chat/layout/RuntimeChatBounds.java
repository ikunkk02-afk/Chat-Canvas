package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.PixelLayout;

public record RuntimeChatBounds(
		int left,
		int top,
		int right,
		int bottom,
		int messageTop,
		int messageBottom,
		int inputTop,
		int inputBottom
) {
	public static final int DEFAULT_INPUT_GAP = 3;

	public static RuntimeChatBounds calculate(PixelLayout layout, boolean chatOpen,
											  int inputHeight, int inputGap,
											  int minimumMessageHeight) {
		int left = layout.x();
		int top = layout.y();
		int right = layout.right();
		int bottom = layout.bottom();
		if (!chatOpen || inputHeight <= 0) {
			return new RuntimeChatBounds(left, top, right, bottom, top, bottom, bottom, bottom);
		}

		int totalHeight = Math.max(1, bottom - top);
		int requiredMessageHeight = Math.max(1, Math.min(minimumMessageHeight, totalHeight));
		int safeInputHeight = Math.max(0, Math.min(inputHeight, totalHeight - requiredMessageHeight));
		int remainingAfterInput = totalHeight - safeInputHeight;
		int safeGap = Math.max(0, Math.min(inputGap, remainingAfterInput - requiredMessageHeight));
		int inputBottom = bottom;
		int inputTop = inputBottom - safeInputHeight;
		int messageBottom = inputTop - safeGap;
		return new RuntimeChatBounds(
				left,
				top,
				right,
				bottom,
				top,
				Math.max(top + requiredMessageHeight, messageBottom),
				inputTop,
				inputBottom
		);
	}

	public int messageWidth() {
		return right - left;
	}

	public int messageHeight() {
		return messageBottom - messageTop;
	}

	public int inputHeight() {
		return inputBottom - inputTop;
	}

	public int inputGap() {
		return inputTop - messageBottom;
	}
}
