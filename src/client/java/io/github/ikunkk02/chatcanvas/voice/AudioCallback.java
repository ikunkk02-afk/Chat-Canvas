package io.github.ikunkk02.chatcanvas.voice;

@FunctionalInterface
public interface AudioCallback {
	void onPcm16Mono16Khz(byte[] pcm) throws Exception;
}
