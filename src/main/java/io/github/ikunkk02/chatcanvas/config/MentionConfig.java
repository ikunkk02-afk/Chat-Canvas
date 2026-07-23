package io.github.ikunkk02.chatcanvas.config;

public record MentionConfig(
		boolean doubleClickEnabled,
		int doubleClickIntervalMs,
		boolean highlightEnabled,
		int highlightColor,
		boolean highlightBold,
		boolean requireAtSymbol
) {
	public static final int MIN_DOUBLE_CLICK_INTERVAL_MS = 150;
	public static final int MAX_DOUBLE_CLICK_INTERVAL_MS = 600;
	public static final MentionConfig DEFAULT = new MentionConfig(
			true, 350, true, 0xFF4FD8, true, true);

	public MentionConfig {
		doubleClickIntervalMs = Math.max(MIN_DOUBLE_CLICK_INTERVAL_MS,
				Math.min(MAX_DOUBLE_CLICK_INTERVAL_MS, doubleClickIntervalMs));
		highlightColor = Math.max(0, Math.min(0xFFFFFF, highlightColor));
	}

	public MentionConfig sanitized() {
		return new MentionConfig(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled,
				highlightColor, highlightBold, requireAtSymbol);
	}

	public MentionConfig withDoubleClickEnabled(boolean value) {
		return new MentionConfig(value, doubleClickIntervalMs, highlightEnabled,
				highlightColor, highlightBold, requireAtSymbol);
	}

	public MentionConfig withDoubleClickIntervalMs(int value) {
		return new MentionConfig(doubleClickEnabled, value, highlightEnabled,
				highlightColor, highlightBold, requireAtSymbol);
	}

	public MentionConfig withHighlightEnabled(boolean value) {
		return new MentionConfig(doubleClickEnabled, doubleClickIntervalMs, value,
				highlightColor, highlightBold, requireAtSymbol);
	}

	public MentionConfig withHighlightColor(int value) {
		return new MentionConfig(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled,
				value, highlightBold, requireAtSymbol);
	}

	public MentionConfig withHighlightBold(boolean value) {
		return new MentionConfig(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled,
				highlightColor, value, requireAtSymbol);
	}

	public MentionConfig withRequireAtSymbol(boolean value) {
		return new MentionConfig(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled,
				highlightColor, highlightBold, value);
	}
}
