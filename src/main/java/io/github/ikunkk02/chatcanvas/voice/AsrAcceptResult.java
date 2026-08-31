package io.github.ikunkk02.chatcanvas.voice;

public record AsrAcceptResult(String partial, boolean speechDetected, boolean endpoint) {
	public static final AsrAcceptResult EMPTY = new AsrAcceptResult("", false, false);

	public AsrAcceptResult {
		partial = partial == null ? "" : partial;
	}
}
