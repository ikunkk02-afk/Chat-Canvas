package io.github.ikunkk02.chatcanvas.voice;

import java.nio.file.Path;

public interface SpeechRecognitionBackend extends AutoCloseable {
	void initialize(Path modelPath) throws Exception;

	RecognitionSession createSession(float sampleRate) throws Exception;

	boolean isReady();

	@Override
	void close();
}
