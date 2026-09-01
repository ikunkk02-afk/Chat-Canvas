package io.github.ikunkk02.chatcanvas.voice;

public final class UnavailableAudioCaptureBackend implements AudioCaptureBackend {
	private final CaptureCapabilities capabilities;

	public UnavailableAudioCaptureBackend(String reason) {
		capabilities = CaptureCapabilities.unavailable("none", "Unavailable", reason);
	}

	@Override public CaptureCapabilities capabilities() { return capabilities; }
	@Override public void start(String deviceId, AudioCallback callback) {
		throw new IllegalStateException(capabilities.unavailableReason());
	}
	@Override public void stop() { }
	@Override public Throwable getLastError() { return null; }
	@Override public void close() { }
}
