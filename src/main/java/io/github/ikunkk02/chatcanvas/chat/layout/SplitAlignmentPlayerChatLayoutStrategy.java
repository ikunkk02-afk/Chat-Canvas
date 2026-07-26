package io.github.ikunkk02.chatcanvas.chat.layout;

public final class SplitAlignmentPlayerChatLayoutStrategy implements PlayerChatLayoutStrategy {
	@Override
	public int wrapWidth(int contentWidth, int headWidth,
						 double splitRatio, boolean selfMessage) {
		double safeRatio = Double.isFinite(splitRatio)
				? Math.max(0.5, Math.min(1.0, splitRatio))
				: 0.75;
		int limitedWidth = Math.max(1, (int) Math.floor(
				Math.max(1, contentWidth) * safeRatio));
		return Math.max(1, limitedWidth - Math.max(0, headWidth));
	}

	@Override
	public int textX(int contentLeft, int contentRight, int lineWidth,
					 int headWidth, boolean selfMessage) {
		if (selfMessage) {
			return Math.max(contentLeft, contentRight - Math.max(0, lineWidth));
		}
		return contentLeft + Math.max(0, headWidth);
	}

	@Override
	public int headX(int contentLeft, int textX, int headWidth, boolean selfMessage) {
		if (selfMessage) {
			return Math.max(contentLeft, textX - Math.max(0, headWidth));
		}
		return contentLeft;
	}

	@Override
	public boolean reserveHead(boolean selfMessage) {
		return true;
	}
}
