package io.github.ikunkk02.chatcanvas.config;

public record CommandSystemConfig(
		boolean enabled,
		LayoutConfig layout,
		ChatTextConfig text,
		ChatBackgroundConfig background,
		int textColor,
		int maximumMessages,
		int fadeSeconds,
		double messageSpacing,
		double scrollSpeed,
		boolean outline,
		int outlineColor,
		double outlineOpacity
) {
	public static final CommandSystemConfig DEFAULT = new CommandSystemConfig(
			true,
			new LayoutConfig(0.61, 0.06, 0.35, 0.28),
			ChatTextConfig.DEFAULT,
			ChatBackgroundConfig.DEFAULT,
			0xFFFFFF,
			300,
			10,
			2.0,
			18.0,
			false,
			0x000000,
			1.0
	);

	public CommandSystemConfig(boolean enabled, LayoutConfig layout, ChatTextConfig text,
							   ChatBackgroundConfig background, int textColor,
							   int maximumMessages, int fadeSeconds,
							   double messageSpacing, double scrollSpeed) {
		this(enabled, layout, text, background, textColor, maximumMessages, fadeSeconds,
				messageSpacing, scrollSpeed, false, 0x000000, 1.0);
	}

	public CommandSystemConfig sanitized() {
		return new CommandSystemConfig(
				enabled,
				layout == null ? DEFAULT.layout : layout.sanitized(),
				text == null ? DEFAULT.text : text.sanitized(),
				background == null ? DEFAULT.background : background.sanitized(),
				Math.max(0, Math.min(0xFFFFFF, textColor)),
				Math.max(50, Math.min(2_000, maximumMessages)),
				Math.max(1, Math.min(120, fadeSeconds)),
				clamp(messageSpacing, 0.0, 12.0, DEFAULT.messageSpacing),
				clamp(scrollSpeed, 1.0, 100.0, DEFAULT.scrollSpeed),
				outline,
				Math.max(0, Math.min(0xFFFFFF, outlineColor)),
				clamp(outlineOpacity, 0.0, 1.0, DEFAULT.outlineOpacity)
		);
	}

	private static double clamp(double value, double min, double max, double fallback) {
		return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
	}
}
