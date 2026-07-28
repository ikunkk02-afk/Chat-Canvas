package io.github.ikunkk02.chatcanvas.chat.message;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record MessageContext(
		MessageIngress ingress,
		@Nullable MessageSignature signature,
		@Nullable PlayerChatIdentity sender,
		@Nullable Component senderName,
		List<PlayerChatIdentity> onlinePlayers,
		@Nullable UUID localPlayerUuid,
		String localPlayerName,
		boolean overlay
) {
	public MessageContext {
		if (ingress == null) ingress = MessageIngress.DIRECT_HUD;
		onlinePlayers = onlinePlayers == null ? List.of() : List.copyOf(onlinePlayers);
		localPlayerName = localPlayerName == null ? "" : localPlayerName;
	}

	public static MessageContext direct(
			List<PlayerChatIdentity> onlinePlayers, UUID localUuid, String localName) {
		return new MessageContext(MessageIngress.DIRECT_HUD, null, null, null,
				onlinePlayers, localUuid, localName, false);
	}
}
