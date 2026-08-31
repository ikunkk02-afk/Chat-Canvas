package io.github.ikunkk02.chatcanvas.voice;

public interface VadProcessor extends AutoCloseable {
	VadDecision accept(byte[] pcm16Le, int length) throws Exception;

	void reset();

	@Override
	void close();
}
