package io.github.ikunkk02.chatcanvas.chat.input;

public final class PlayerChatInputState {
	private String text = "";
	private int cursor;
	private int selectionEnd;
	private int historyIndex;

	public ChatInputSnapshot snapshot() {
		return new ChatInputSnapshot(text, cursor, selectionEnd);
	}

	void restore(ChatInputSnapshot snapshot) {
		ChatInputSnapshot safe = snapshot == null ? ChatInputSnapshot.EMPTY : snapshot;
		text = safe.text();
		cursor = safe.cursor();
		selectionEnd = safe.selectionEnd();
	}

	public String text() {
		return text;
	}

	public int cursor() {
		return cursor;
	}

	public int selectionEnd() {
		return selectionEnd;
	}

	public int historyIndex() {
		return historyIndex;
	}

	void historyIndex(int historyIndex) {
		this.historyIndex = Math.max(0, historyIndex);
	}
}
