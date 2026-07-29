package io.github.ikunkk02.chatcanvas.chat.emoji;

import java.util.List;

public record EmojiRecentData(int version, List<RecentEmojiEntry> recent) {
	public static final int CURRENT_VERSION = 1;
	public static final EmojiRecentData EMPTY =
			new EmojiRecentData(CURRENT_VERSION, List.of());

	public EmojiRecentData {
		version = CURRENT_VERSION;
		recent = List.copyOf(recent == null ? List.of() : recent);
	}
}
