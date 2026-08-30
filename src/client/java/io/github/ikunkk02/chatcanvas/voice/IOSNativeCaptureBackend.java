package io.github.ikunkk02.chatcanvas.voice;

import java.util.List;
import java.util.ServiceLoader;

public final class IOSNativeCaptureBackend implements AudioCaptureBackend {
	private final NativeAudioCaptureBridge bridge;
	private volatile Throwable lastError;

	public IOSNativeCaptureBackend() {
		bridge = ServiceLoader.load(NativeAudioCaptureBridge.class).stream()
				.map(ServiceLoader.Provider::get)
				.filter(candidate -> "ios-arm64".equals(candidate.platformId()))
				.findFirst().orElse(null);
	}

	@Override
	public CaptureCapabilities capabilities() {
		if (VoicePlatformSupport.current().os() != VoicePlatformSupport.OperatingSystem.IOS
				|| bridge == null || !safeAvailable()) {
			String reason = bridge == null ? "chat_canvas.voice.error.ios_runtime_unavailable"
					: bridge.unavailableReason();
			return CaptureCapabilities.unavailable("ios_native", "iOS native capture", reason);
		}
		return new CaptureCapabilities(true, "ios_native", "iOS native capture", 16_000, 1,
				true, "", List.of(new AudioCaptureDevice("", "iOS default", true)));
	}

	private boolean safeAvailable() {
		try { return bridge.isAvailable(); }
		catch (Throwable throwable) { lastError = throwable; return false; }
	}

	@Override public void start(String deviceId, AudioCallback callback) throws Exception {
		if (bridge == null) throw new IllegalStateException("No launcher-signed iOS capture bridge");
		try { bridge.start(callback); }
		catch (LinkageError | SecurityException error) {
			lastError = error;
			throw new IllegalStateException("iOS native capture bridge could not be loaded", error);
		}
	}

	@Override public void stop() { if (bridge != null) try { bridge.stop(); } catch (Throwable ignored) { } }
	@Override public Throwable getLastError() { return lastError; }
	@Override public void close() { stop(); }
}
