package io.github.ikunkk02.chatcanvas.voice;

public record VoiceSettings(
		boolean enabled,
		String microphoneId,
		int maximumSeconds,
		boolean showInputLevel,
		double noiseThreshold,
		boolean showPartialResults,
		boolean addFinalPunctuation
) {
	public static final VoiceSettings DEFAULT =
			new VoiceSettings(true, "", 15, true, 0.015, true, false);

	public VoiceSettings {
		microphoneId = microphoneId == null ? "" : microphoneId;
		maximumSeconds = Math.max(5, Math.min(60, maximumSeconds));
		if (!Double.isFinite(noiseThreshold)) noiseThreshold = 0.015;
		noiseThreshold = Math.max(0.0, Math.min(1.0, noiseThreshold));
	}
}
