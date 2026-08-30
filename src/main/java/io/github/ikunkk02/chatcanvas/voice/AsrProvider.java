package io.github.ikunkk02.chatcanvas.voice;

import java.nio.file.Path;

public interface AsrProvider extends AutoCloseable {
	String id();

	void loadModel(VoiceModelDescriptor descriptor, Path modelPath,
				   AsrRuntimeOptions options) throws Exception;

	void unloadModel();

	AsrSession createSession() throws Exception;

	boolean isLoaded();

	default boolean supportsStreaming() { return false; }

	default boolean suppliesEndpoint() { return false; }

	@Override
	default void close() { unloadModel(); }
}
