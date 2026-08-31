package io.github.ikunkk02.chatcanvas.voice;

/** Service contract for launcher-signed iOS capture bridges. */
public interface NativeAudioCaptureBridge {
	String platformId();
	boolean isAvailable();
	String unavailableReason();
	void start(AudioCallback callback) throws Exception;
	void stop();
}
