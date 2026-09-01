package io.github.ikunkk02.chatcanvas.voice;

public record VoiceModelCapability(boolean available, String reasonKey, String detail) {
	public static VoiceModelCapability supported() { return new VoiceModelCapability(true, "", ""); }
	public static VoiceModelCapability unavailable(String key, String detail) {
		return new VoiceModelCapability(false, key, detail == null ? "" : detail);
	}
}
