package io.github.ikunkk02.chatcanvas.voice;

public record AudioCaptureDevice(String id, String displayName, boolean preferredFormat) {
	public AudioCaptureDevice {
		id = id == null ? "" : id;
		displayName = displayName == null ? id : displayName;
	}
}
