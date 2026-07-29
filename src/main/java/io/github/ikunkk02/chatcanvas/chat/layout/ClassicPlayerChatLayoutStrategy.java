package io.github.ikunkk02.chatcanvas.chat.layout;

public final class ClassicPlayerChatLayoutStrategy implements PlayerChatLayoutStrategy {
	@Override
	public int wrapWidth(int contentWidth, int headWidth,
						 double splitRatio, boolean selfMessage) {
		return Math.max(1, contentWidth - Math.max(0, headWidth));
	}

	@Override
	public int textX(int contentLeft, int contentRight, int lineWidth,
					 int headWidth, boolean selfMessage) {
		return contentLeft + Math.max(0, headWidth);
	}

	@Override
	public int headX(int contentLeft, int textX, int headWidth, boolean selfMessage) {
		return contentLeft;
	}

	@Override
	public boolean reserveHead(boolean selfMessage) {
		return true;
	}
}
