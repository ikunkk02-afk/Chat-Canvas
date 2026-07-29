package io.github.ikunkk02.chatcanvas.config;

public record PixelLayout(int x, int y, int width, int height) {
	public static final int DEFAULT_MIN_WIDTH = 180;
	public static final int DEFAULT_MIN_HEIGHT = 96;
	public static final int DEFAULT_SAFE_MARGIN = 12;

	public PixelLayout constrained(int screenWidth, int screenHeight) {
		return constrained(screenWidth, screenHeight, DEFAULT_SAFE_MARGIN);
	}

	public PixelLayout constrained(int screenWidth, int screenHeight, int safeMargin) {
		int marginX = Math.min(Math.max(0, safeMargin), Math.max(0, screenWidth / 2));
		int marginY = Math.min(Math.max(0, safeMargin), Math.max(0, screenHeight / 2));
		int availableWidth = Math.max(1, screenWidth - marginX * 2);
		int availableHeight = Math.max(1, screenHeight - marginY * 2);
		int minWidth = Math.min(DEFAULT_MIN_WIDTH, availableWidth);
		int minHeight = Math.min(DEFAULT_MIN_HEIGHT, availableHeight);
		int clampedWidth = clamp(width, minWidth, availableWidth);
		int clampedHeight = clamp(height, minHeight, availableHeight);
		int clampedX = clamp(x, marginX, Math.max(marginX, screenWidth - marginX - clampedWidth));
		int clampedY = clamp(y, marginY, Math.max(marginY, screenHeight - marginY - clampedHeight));
		return new PixelLayout(clampedX, clampedY, clampedWidth, clampedHeight);
	}

	public int right() {
		return x + width;
	}

	public int bottom() {
		return y + height;
	}

	public double centerX() {
		return x + width / 2.0;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
