package io.github.ikunkk02.chatcanvas.voice;

import java.util.List;

public interface AudioCaptureBackend extends AutoCloseable {
	CaptureCapabilities capabilities();

	default boolean isAvailable() { return capabilities().available(); }

	/** Runs capture on the calling audio worker until stop() is requested. */
	void start(String deviceId, AudioCallback callback) throws Exception;

	void stop();

	Throwable getLastError();

	default List<AudioCaptureDevice> devices() { return capabilities().devices(); }

	@Override
	void close();
}
