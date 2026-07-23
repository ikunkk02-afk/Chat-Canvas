package io.github.ikunkk02.chatcanvas.config;

public record ChatTextConfig(
		double fontScale,
		double lineSpacing,
		double textOpacity,
		ChatTextAlignment alignment,
		boolean shadow
) {
	public static final double MIN_FONT_SCALE = 0.50;
	public static final double MAX_FONT_SCALE = 2.00;
	public static final double MIN_LINE_SPACING = 0.50;
	public static final double MAX_LINE_SPACING = 2.00;
	public static final double MIN_TEXT_OPACITY = 0.10;
	public static final double MAX_TEXT_OPACITY = 1.00;
	public static final ChatTextConfig DEFAULT =
			new ChatTextConfig(1.0, 1.0, 1.0, ChatTextAlignment.LEFT, true);

	public ChatTextConfig sanitized() {
		return new ChatTextConfig(
				sanitizeFinite(fontScale, DEFAULT.fontScale, MIN_FONT_SCALE, MAX_FONT_SCALE),
				sanitizeFinite(lineSpacing, DEFAULT.lineSpacing, MIN_LINE_SPACING, MAX_LINE_SPACING),
				sanitizeFinite(textOpacity, DEFAULT.textOpacity, MIN_TEXT_OPACITY, MAX_TEXT_OPACITY),
				alignment == null ? ChatTextAlignment.LEFT : alignment,
				shadow
		);
	}

	private static double sanitizeFinite(double value, double fallback, double min, double max) {
		if (!Double.isFinite(value)) {
			return fallback;
		}
		return Math.max(min, Math.min(max, value));
	}
}
