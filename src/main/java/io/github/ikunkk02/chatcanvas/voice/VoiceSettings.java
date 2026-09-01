package io.github.ikunkk02.chatcanvas.voice;

public record VoiceSettings(
		boolean enabled,
		String microphoneId,
		int maximumSeconds,
		boolean showInputLevel,
		double noiseThreshold,
		boolean showPartialResults,
		boolean addFinalPunctuation,
		String selectedModelId,
		int noSpeechTimeoutMillis,
		int endpointSilenceMillis,
		int tailPaddingMillis,
		int inferenceThreads
) {
	public static final VoiceSettings DEFAULT =
			new VoiceSettings(true, "", 25, true, 0.015, true, false,
					"", 5_000, 800, 200, 0);

	/** Compatibility constructor for pre-registry callers and old tests. */
	public VoiceSettings(boolean enabled, String microphoneId, int maximumSeconds,
						 boolean showInputLevel, double noiseThreshold,
						 boolean showPartialResults, boolean addFinalPunctuation) {
		this(enabled, microphoneId, maximumSeconds, showInputLevel, noiseThreshold,
				showPartialResults, addFinalPunctuation, "", 5_000, 800, 200, 0);
	}

	public VoiceSettings {
		microphoneId = microphoneId == null ? "" : microphoneId;
		selectedModelId = selectedModelId == null ? "" : selectedModelId;
		maximumSeconds = Math.max(5, Math.min(60, maximumSeconds));
		if (!Double.isFinite(noiseThreshold)) noiseThreshold = 0.015;
		noiseThreshold = Math.max(0.0, Math.min(1.0, noiseThreshold));
		if (noSpeechTimeoutMillis <= 0) noSpeechTimeoutMillis = 5_000;
		if (endpointSilenceMillis <= 0) endpointSilenceMillis = 800;
		if (tailPaddingMillis < 0) tailPaddingMillis = 200;
		noSpeechTimeoutMillis = Math.max(1_000, Math.min(15_000, noSpeechTimeoutMillis));
		endpointSilenceMillis = Math.max(300, Math.min(3_000, endpointSilenceMillis));
		tailPaddingMillis = Math.max(0, Math.min(1_000, tailPaddingMillis));
		inferenceThreads = Math.max(0, Math.min(4, inferenceThreads));
	}

	public VoiceSettings withSelectedModel(String modelId) {
		return new VoiceSettings(enabled, microphoneId, maximumSeconds,
				showInputLevel, noiseThreshold, showPartialResults,
				addFinalPunctuation, modelId, noSpeechTimeoutMillis,
				endpointSilenceMillis, tailPaddingMillis, inferenceThreads);
	}

	public VoiceSettings withEnabled(boolean value) {
		return copy(value, microphoneId, maximumSeconds, showInputLevel, noiseThreshold,
				showPartialResults, addFinalPunctuation, selectedModelId,
				noSpeechTimeoutMillis, endpointSilenceMillis, tailPaddingMillis, inferenceThreads);
	}

	public VoiceSettings withMicrophoneId(String value) {
		return copy(enabled, value, maximumSeconds, showInputLevel, noiseThreshold,
				showPartialResults, addFinalPunctuation, selectedModelId,
				noSpeechTimeoutMillis, endpointSilenceMillis, tailPaddingMillis, inferenceThreads);
	}

	public VoiceSettings withMaximumSeconds(int value) {
		return copy(enabled, microphoneId, value, showInputLevel, noiseThreshold,
				showPartialResults, addFinalPunctuation, selectedModelId,
				noSpeechTimeoutMillis, endpointSilenceMillis, tailPaddingMillis, inferenceThreads);
	}

	public VoiceSettings withShowInputLevel(boolean value) {
		return copy(enabled, microphoneId, maximumSeconds, value, noiseThreshold,
				showPartialResults, addFinalPunctuation, selectedModelId,
				noSpeechTimeoutMillis, endpointSilenceMillis, tailPaddingMillis, inferenceThreads);
	}

	public VoiceSettings withNoiseThreshold(double value) {
		return copy(enabled, microphoneId, maximumSeconds, showInputLevel, value,
				showPartialResults, addFinalPunctuation, selectedModelId,
				noSpeechTimeoutMillis, endpointSilenceMillis, tailPaddingMillis, inferenceThreads);
	}

	public VoiceSettings withShowPartialResults(boolean value) {
		return copy(enabled, microphoneId, maximumSeconds, showInputLevel, noiseThreshold,
				value, addFinalPunctuation, selectedModelId,
				noSpeechTimeoutMillis, endpointSilenceMillis, tailPaddingMillis, inferenceThreads);
	}

	public VoiceSettings withFinalPunctuation(boolean value) {
		return copy(enabled, microphoneId, maximumSeconds, showInputLevel, noiseThreshold,
				showPartialResults, value, selectedModelId,
				noSpeechTimeoutMillis, endpointSilenceMillis, tailPaddingMillis, inferenceThreads);
	}

	public VoiceSettings withEndpointTiming(int noSpeech, int endpointSilence, int tailPadding) {
		return copy(enabled, microphoneId, maximumSeconds, showInputLevel, noiseThreshold,
				showPartialResults, addFinalPunctuation, selectedModelId,
				noSpeech, endpointSilence, tailPadding, inferenceThreads);
	}

	public VoiceSettings withInferenceThreads(int value) {
		return copy(enabled, microphoneId, maximumSeconds, showInputLevel, noiseThreshold,
				showPartialResults, addFinalPunctuation, selectedModelId,
				noSpeechTimeoutMillis, endpointSilenceMillis, tailPaddingMillis, value);
	}

	private static VoiceSettings copy(boolean enabled, String microphoneId,
			int maximumSeconds, boolean showInputLevel, double noiseThreshold,
			boolean showPartialResults, boolean addFinalPunctuation,
			String selectedModelId, int noSpeechTimeoutMillis,
			int endpointSilenceMillis, int tailPaddingMillis, int inferenceThreads) {
		return new VoiceSettings(enabled, microphoneId, maximumSeconds, showInputLevel,
				noiseThreshold, showPartialResults, addFinalPunctuation, selectedModelId,
				noSpeechTimeoutMillis, endpointSilenceMillis, tailPaddingMillis, inferenceThreads);
	}
}
