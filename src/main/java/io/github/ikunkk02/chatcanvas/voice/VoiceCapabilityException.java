package io.github.ikunkk02.chatcanvas.voice;

final class VoiceCapabilityException extends Exception {
	private final String reasonKey;

	VoiceCapabilityException(String reasonKey) {
		super(reasonKey);
		this.reasonKey = reasonKey;
	}

	String reasonKey() { return reasonKey; }
}
