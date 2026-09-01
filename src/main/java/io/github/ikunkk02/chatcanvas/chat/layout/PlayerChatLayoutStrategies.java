package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.PlayerChatLayoutMode;

public final class PlayerChatLayoutStrategies {
	private static final PlayerChatLayoutStrategy CLASSIC =
			new ClassicPlayerChatLayoutStrategy();
	private static final PlayerChatLayoutStrategy SPLIT =
			new SplitAlignmentPlayerChatLayoutStrategy();

	private PlayerChatLayoutStrategies() {
	}

	public static PlayerChatLayoutStrategy forMode(PlayerChatLayoutMode mode) {
		return mode == PlayerChatLayoutMode.SPLIT_ALIGNMENT ? SPLIT : CLASSIC;
	}
}
