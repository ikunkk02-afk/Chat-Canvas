package io.github.ikunkk02.chatcanvas.voice;

public record VadDecision(
		boolean speechStarted,
		boolean speechActive,
		boolean waitingForEndpoint,
		boolean endpoint
) {
	public static final VadDecision SILENCE = new VadDecision(false, false, false, false);
}
