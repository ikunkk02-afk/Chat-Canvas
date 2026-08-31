package io.github.ikunkk02.chatcanvas.voice;

public record VoiceRecognitionResult(
		String text,
		boolean empty,
		long durationMillis,
		VoiceFinishReason reason
) {
	public VoiceRecognitionResult(String text, boolean empty, long durationMillis) {
		this(text, empty, durationMillis, VoiceFinishReason.ENDPOINT);
	}

	public VoiceRecognitionResult {
		text = text == null ? "" : text;
		empty = text.isBlank();
		durationMillis = Math.max(0L, durationMillis);
		reason = reason == null ? VoiceFinishReason.ENDPOINT : reason;
	}
}
