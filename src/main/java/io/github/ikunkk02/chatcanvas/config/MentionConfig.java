package io.github.ikunkk02.chatcanvas.config;

public record MentionConfig(
		boolean doubleClickEnabled,
		int doubleClickIntervalMs,
		boolean highlightEnabled,
		int highlightColor,
		boolean highlightBold,
		boolean requireAtSymbol,
		boolean soundEnabled,
		MentionSound sound,
		double soundVolume,
		double soundPitch,
		boolean toastEnabled,
		boolean toastWhenChatOpen,
		int toastMessageLength,
		boolean flashEnabled,
		int flashColor,
		double flashOpacity,
		int flashDurationMs,
		boolean ignoreOwnMessages,
		boolean playerQuickActionsEnabled,
		String privateMessageTemplate
) {
	public static final int MIN_DOUBLE_CLICK_INTERVAL_MS = 150;
	public static final int MAX_DOUBLE_CLICK_INTERVAL_MS = 600;
	public static final double MIN_SOUND_VOLUME = 0.0;
	public static final double MAX_SOUND_VOLUME = 1.0;
	public static final double MIN_SOUND_PITCH = 0.5;
	public static final double MAX_SOUND_PITCH = 2.0;
	public static final int MIN_TOAST_MESSAGE_LENGTH = 20;
	public static final int MAX_TOAST_MESSAGE_LENGTH = 160;
	public static final double MIN_FLASH_OPACITY = 0.0;
	public static final double MAX_FLASH_OPACITY = 0.6;
	public static final int MIN_FLASH_DURATION_MS = 100;
	public static final int MAX_FLASH_DURATION_MS = 1_500;
	public static final String DEFAULT_PRIVATE_MESSAGE_TEMPLATE = "/msg {player} ";

	public static final MentionConfig DEFAULT = new MentionConfig(
			true, 350, true, 0xFF4FD8, true, true,
			true, MentionSound.EXPERIENCE_ORB, 0.8, 1.0,
			true, false, 80,
			false, 0xFF4FD8, 0.22, 350,
			true, true, DEFAULT_PRIVATE_MESSAGE_TEMPLATE);

	public MentionConfig {
		doubleClickIntervalMs = clamp(doubleClickIntervalMs,
				MIN_DOUBLE_CLICK_INTERVAL_MS, MAX_DOUBLE_CLICK_INTERVAL_MS);
		highlightColor = clampColor(highlightColor);
		sound = sound == null ? MentionSound.EXPERIENCE_ORB : sound;
		soundVolume = clampFinite(soundVolume, 0.8,
				MIN_SOUND_VOLUME, MAX_SOUND_VOLUME);
		soundPitch = clampFinite(soundPitch, 1.0,
				MIN_SOUND_PITCH, MAX_SOUND_PITCH);
		toastMessageLength = clamp(toastMessageLength,
				MIN_TOAST_MESSAGE_LENGTH, MAX_TOAST_MESSAGE_LENGTH);
		flashColor = clampColor(flashColor);
		flashOpacity = clampFinite(flashOpacity, 0.22,
				MIN_FLASH_OPACITY, MAX_FLASH_OPACITY);
		flashDurationMs = clamp(flashDurationMs,
				MIN_FLASH_DURATION_MS, MAX_FLASH_DURATION_MS);
		privateMessageTemplate = sanitizeTemplate(privateMessageTemplate);
	}

	public MentionConfig(
			boolean doubleClickEnabled, int doubleClickIntervalMs,
			boolean highlightEnabled, int highlightColor,
			boolean highlightBold, boolean requireAtSymbol) {
		this(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol,
				DEFAULT.soundEnabled(), DEFAULT.sound(), DEFAULT.soundVolume(), DEFAULT.soundPitch(),
				DEFAULT.toastEnabled(), DEFAULT.toastWhenChatOpen(),
				DEFAULT.toastMessageLength(), DEFAULT.flashEnabled(), DEFAULT.flashColor(),
				DEFAULT.flashOpacity(), DEFAULT.flashDurationMs(), DEFAULT.ignoreOwnMessages(),
				DEFAULT.playerQuickActionsEnabled(), DEFAULT.privateMessageTemplate());
	}

	public MentionConfig sanitized() {
		return new MentionConfig(
				doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withDoubleClickEnabled(boolean value) {
		return copy(value, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withDoubleClickIntervalMs(int value) {
		return copy(doubleClickEnabled, value, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withHighlightEnabled(boolean value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, value, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withHighlightColor(int value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, value,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withHighlightBold(boolean value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				value, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withRequireAtSymbol(boolean value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, value, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withSoundEnabled(boolean value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, value, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withSound(MentionSound value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, value, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withSoundVolume(double value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, value, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withSoundPitch(double value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, value,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withToastEnabled(boolean value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				value, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withToastWhenChatOpen(boolean value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, value, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withToastMessageLength(int value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, value, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withFlashEnabled(boolean value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, value, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withFlashColor(int value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, value,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withFlashOpacity(double value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				value, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withFlashDurationMs(int value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, value, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withIgnoreOwnMessages(boolean value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, value, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	public MentionConfig withPlayerQuickActionsEnabled(boolean value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, value,
				privateMessageTemplate);
	}

	public MentionConfig withPrivateMessageTemplate(String value) {
		return copy(doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				value);
	}

	private static MentionConfig copy(
			boolean doubleClickEnabled, int doubleClickIntervalMs, boolean highlightEnabled,
			int highlightColor, boolean highlightBold, boolean requireAtSymbol,
			boolean soundEnabled, MentionSound sound, double soundVolume, double soundPitch,
			boolean toastEnabled, boolean toastWhenChatOpen, int toastMessageLength,
			boolean flashEnabled, int flashColor, double flashOpacity, int flashDurationMs,
			boolean ignoreOwnMessages, boolean playerQuickActionsEnabled,
			String privateMessageTemplate) {
		return new MentionConfig(
				doubleClickEnabled, doubleClickIntervalMs, highlightEnabled, highlightColor,
				highlightBold, requireAtSymbol, soundEnabled, sound, soundVolume, soundPitch,
				toastEnabled, toastWhenChatOpen, toastMessageLength, flashEnabled, flashColor,
				flashOpacity, flashDurationMs, ignoreOwnMessages, playerQuickActionsEnabled,
				privateMessageTemplate);
	}

	private static String sanitizeTemplate(String value) {
		if (value == null) return DEFAULT_PRIVATE_MESSAGE_TEMPLATE;
		String singleLine = value.replace('\r', ' ').replace('\n', ' ');
		if (!singleLine.contains("{player}")) return DEFAULT_PRIVATE_MESSAGE_TEMPLATE;
		return singleLine;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static int clampColor(int value) {
		return Math.max(0, Math.min(0xFFFFFF, value));
	}

	private static double clampFinite(double value, double fallback, double min, double max) {
		return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
	}
}
