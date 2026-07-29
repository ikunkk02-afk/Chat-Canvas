package io.github.ikunkk02.chatcanvas.config;

public record LayoutConfig(
		double chatXRatio,
		double chatYRatio,
		double chatWidthRatio,
		double chatHeightRatio
) {
	public static final LayoutConfig DEFAULT = new LayoutConfig(0.04, 0.68, 0.35, 0.28);

	public LayoutConfig sanitized() {
		if (!isFinite(chatXRatio) || !isFinite(chatYRatio)
				|| !isFinite(chatWidthRatio) || !isFinite(chatHeightRatio)
				|| chatWidthRatio <= 0 || chatHeightRatio <= 0) {
			return DEFAULT;
		}

		double width = clamp(chatWidthRatio, 0.10, 1.0);
		double height = clamp(chatHeightRatio, 0.10, 1.0);
		double x = clamp(chatXRatio, 0.0, Math.max(0.0, 1.0 - width));
		double y = clamp(chatYRatio, 0.0, Math.max(0.0, 1.0 - height));
		return new LayoutConfig(x, y, width, height);
	}

	public PixelLayout toPixels(int screenWidth, int screenHeight) {
		LayoutConfig safe = sanitized();
		int width = (int) Math.round(safe.chatWidthRatio * screenWidth);
		int height = (int) Math.round(safe.chatHeightRatio * screenHeight);
		int x = (int) Math.round(safe.chatXRatio * screenWidth);
		int y = (int) Math.round(safe.chatYRatio * screenHeight);
		return new PixelLayout(x, y, width, height).constrained(screenWidth, screenHeight);
	}

	public static LayoutConfig fromPixels(PixelLayout layout, int screenWidth, int screenHeight) {
		if (screenWidth <= 0 || screenHeight <= 0) {
			return DEFAULT;
		}
		PixelLayout safe = layout.constrained(screenWidth, screenHeight);
		return new LayoutConfig(
				safe.x() / (double) screenWidth,
				safe.y() / (double) screenHeight,
				safe.width() / (double) screenWidth,
				safe.height() / (double) screenHeight
		).sanitized();
	}

	private static boolean isFinite(double value) {
		return Double.isFinite(value);
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}
