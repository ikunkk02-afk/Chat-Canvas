package io.github.ikunkk02.chatcanvas.config;

public record ChatBackgroundConfig(
		MessageBackgroundMode messageMode,
		int messageColor,
		double messageOpacity,
		int horizontalPadding,
		int verticalPadding,
		int inputColor,
		double inputOpacity,
		boolean inputBorderEnabled,
		int inputBorderColor,
		double inputBorderOpacity
) {
	public static final double MIN_OPACITY = 0.0;
	public static final double MAX_OPACITY = 1.0;
	public static final int MIN_HORIZONTAL_PADDING = 0;
	public static final int MAX_HORIZONTAL_PADDING = 12;
	public static final int MIN_VERTICAL_PADDING = 0;
	public static final int MAX_VERTICAL_PADDING = 6;

	public static final ChatBackgroundConfig DEFAULT = new ChatBackgroundConfig(
			MessageBackgroundMode.FOLLOW_TEXT,
			0x000000,
			0.50,
			2,
			1,
			0x000000,
			0.70,
			false,
			0x6EA8FF,
			0.80
	);

	public ChatBackgroundConfig sanitized() {
		return new ChatBackgroundConfig(
				messageMode == null ? DEFAULT.messageMode : messageMode,
				sanitizeRgb(messageColor),
				sanitizeOpacity(messageOpacity, DEFAULT.messageOpacity),
				clamp(horizontalPadding, MIN_HORIZONTAL_PADDING, MAX_HORIZONTAL_PADDING),
				clamp(verticalPadding, MIN_VERTICAL_PADDING, MAX_VERTICAL_PADDING),
				sanitizeRgb(inputColor),
				sanitizeOpacity(inputOpacity, DEFAULT.inputOpacity),
				inputBorderEnabled,
				sanitizeRgb(inputBorderColor),
				sanitizeOpacity(inputBorderOpacity, DEFAULT.inputBorderOpacity)
		);
	}

	public ChatBackgroundConfig withMessageMode(MessageBackgroundMode value) {
		return new ChatBackgroundConfig(
				value, messageColor, messageOpacity, horizontalPadding, verticalPadding,
				inputColor, inputOpacity, inputBorderEnabled, inputBorderColor, inputBorderOpacity
		).sanitized();
	}

	public ChatBackgroundConfig withMessageColor(int value) {
		return new ChatBackgroundConfig(
				messageMode, value, messageOpacity, horizontalPadding, verticalPadding,
				inputColor, inputOpacity, inputBorderEnabled, inputBorderColor, inputBorderOpacity
		).sanitized();
	}

	public ChatBackgroundConfig withInputColor(int value) {
		return new ChatBackgroundConfig(
				messageMode, messageColor, messageOpacity, horizontalPadding, verticalPadding,
				value, inputOpacity, inputBorderEnabled, inputBorderColor, inputBorderOpacity
		).sanitized();
	}

	public ChatBackgroundConfig withInputBorderColor(int value) {
		return new ChatBackgroundConfig(
				messageMode, messageColor, messageOpacity, horizontalPadding, verticalPadding,
				inputColor, inputOpacity, inputBorderEnabled, value, inputBorderOpacity
		).sanitized();
	}

	public ChatBackgroundConfig withInputBorderEnabled(boolean value) {
		return new ChatBackgroundConfig(
				messageMode, messageColor, messageOpacity, horizontalPadding, verticalPadding,
				inputColor, inputOpacity, value, inputBorderColor, inputBorderOpacity
		).sanitized();
	}

	private static double sanitizeOpacity(double value, double fallback) {
		if (!Double.isFinite(value)) {
			return fallback;
		}
		return Math.max(MIN_OPACITY, Math.min(MAX_OPACITY, value));
	}

	private static int sanitizeRgb(int value) {
		return clamp(value, 0, 0xFFFFFF);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
