package io.github.ikunkk02.chatcanvas.chat.emoji;

public record RecentEmojiEntry(String unicode, long lastUsedAt, int useCount) {
	public RecentEmojiEntry {
		unicode = unicode == null ? "" : unicode;
		lastUsedAt = Math.max(0L, lastUsedAt);
		useCount = Math.max(0, useCount);
	}

	public boolean valid() {
		return !unicode.isBlank() && lastUsedAt > 0L && useCount > 0;
	}
}
