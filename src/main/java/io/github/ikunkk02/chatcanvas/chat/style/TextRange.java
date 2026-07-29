package io.github.ikunkk02.chatcanvas.chat.style;

public record TextRange(int startCodePoint, int endCodePoint) {
	public TextRange {
		startCodePoint = Math.max(0, startCodePoint);
		endCodePoint = Math.max(startCodePoint, endCodePoint);
	}

	public boolean contains(int codePointIndex) {
		return codePointIndex >= startCodePoint && codePointIndex < endCodePoint;
	}

	public boolean intersects(int start, int end) {
		return end > startCodePoint && start < endCodePoint;
	}

	public TextRange shifted(int delta) {
		return new TextRange(startCodePoint + delta, endCodePoint + delta);
	}
}
