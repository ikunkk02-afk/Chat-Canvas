package io.github.ikunkk02.chatcanvas.chat.input;

public record ChatInputSnapshot(String text, int cursor, int selectionEnd) {
	public static final ChatInputSnapshot EMPTY = new ChatInputSnapshot("", 0, 0);

	public ChatInputSnapshot {
		text = text == null ? "" : text;
		cursor = clamp(cursor, text.length());
		selectionEnd = clamp(selectionEnd, text.length());
	}

	public static ChatInputSnapshot atEnd(String text) {
		String safe = text == null ? "" : text;
		return new ChatInputSnapshot(safe, safe.length(), safe.length());
	}

	private static int clamp(int value, int maximum) {
		return Math.max(0, Math.min(maximum, value));
	}
}
