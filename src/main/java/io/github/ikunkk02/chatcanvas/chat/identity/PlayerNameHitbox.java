package io.github.ikunkk02.chatcanvas.chat.identity;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record PlayerNameHitbox(
		@Nullable UUID playerUuid,
		String playerName,
		int messageIndex,
		double left,
		double top,
		double right,
		double bottom
) {
}
