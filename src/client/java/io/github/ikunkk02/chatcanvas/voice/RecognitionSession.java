package io.github.ikunkk02.chatcanvas.voice;

public interface RecognitionSession extends AutoCloseable {
	String accept(byte[] pcm, int length) throws Exception;

	String finish() throws Exception;

	@Override
	void close();
}
