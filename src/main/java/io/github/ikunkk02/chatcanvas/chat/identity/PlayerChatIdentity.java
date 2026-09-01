package io.github.ikunkk02.chatcanvas.chat.identity;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record PlayerChatIdentity(
		@Nullable UUID uuid,
		String playerName,
		boolean reliable
) {
	public PlayerChatIdentity {
		playerName = playerName == null ? "" : playerName;
	}
}
