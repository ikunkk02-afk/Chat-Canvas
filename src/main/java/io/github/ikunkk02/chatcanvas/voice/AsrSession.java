package io.github.ikunkk02.chatcanvas.voice;

public interface AsrSession extends AutoCloseable {
	AsrAcceptResult acceptAudio(byte[] pcm16Le, int length) throws Exception;

	String finish() throws Exception;

	@Override
	void close();
}
