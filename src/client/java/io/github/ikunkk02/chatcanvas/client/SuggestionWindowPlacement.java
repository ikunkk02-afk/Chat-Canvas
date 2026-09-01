package io.github.ikunkk02.chatcanvas.client;

/** Positions command suggestions next to a chat field that may be moved by Chat Canvas. */
public final class SuggestionWindowPlacement {
	private SuggestionWindowPlacement() {
	}

	public static int calculateY(
			int inputY, int inputHeight, int suggestionHeight, int screenHeight) {
		int safeHeight = Math.max(0, suggestionHeight);
		int maxY = Math.max(0, screenHeight - safeHeight);
		int below = inputY + Math.max(0, inputHeight);
		if (below + safeHeight <= screenHeight) {
			return Math.max(0, below);
		}
		return Math.max(0, Math.min(maxY, inputY - safeHeight));
	}
}
