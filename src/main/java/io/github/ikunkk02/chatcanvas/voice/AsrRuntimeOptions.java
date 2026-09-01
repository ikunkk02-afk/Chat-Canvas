package io.github.ikunkk02.chatcanvas.voice;

public record AsrRuntimeOptions(int inferenceThreads, boolean debug) {
	public AsrRuntimeOptions {
		inferenceThreads = Math.max(1, Math.min(4, inferenceThreads));
	}
}
