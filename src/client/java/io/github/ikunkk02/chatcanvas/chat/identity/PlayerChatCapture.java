package io.github.ikunkk02.chatcanvas.chat.identity;

import com.mojang.authlib.GameProfile;
import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.notification.MentionNotificationController;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;

import java.util.Optional;

public final class PlayerChatCapture {
	private PlayerChatCapture() {
	}

	public static void register() {
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, timestamp) -> {
			Optional<ChatMessageMetadata> metadata = standardMetadata(
					message, signedMessage, sender, params.name());
			metadata.ifPresent(value -> ChatMessageMetadataRegistry.instance()
					.registerIncoming(message, signatureOf(signedMessage), value));
			MentionNotificationController.instance().receive(
					message,
					signatureOf(signedMessage),
					metadata.map(ChatMessageMetadata::sender),
					timestamp == null ? System.currentTimeMillis() : timestamp.toEpochMilli());
		});
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (overlay) return;
			Optional<ChatMessageMetadata> metadata =
					PluginChatFallbackResolver.resolve(message, PlayerRosterTracker.onlinePlayers());
			metadata.ifPresent(value -> ChatMessageMetadataRegistry.instance()
					.registerIncoming(message, null, value));
			MentionNotificationController.instance().receive(
					message, null, metadata.map(ChatMessageMetadata::sender),
					System.currentTimeMillis());
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
				PlayerRosterTracker.refresh(handler));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			PlayerRosterTracker.clear();
			ChatMessageMetadataRegistry.instance().clearAll();
			PlayerNameHitboxRegistry.clear();
			PlayerNameDoubleClickHandler.instance().reset();
			MentionNotificationController.instance().clearSession();
		});
	}

	private static Optional<ChatMessageMetadata> standardMetadata(
			Text message, SignedMessage signedMessage, GameProfile sender, Text senderName) {
		if (sender != null) {
			return PlayerIdentityResolver.resolveStandard(
					message, senderName, sender.getId(), sender.getName());
		}
		String displayName = senderName == null ? "" : senderName.getString();
		return PlayerRosterTracker.onlinePlayers().stream()
				.filter(player -> displayName.equalsIgnoreCase(player.playerName())
						|| PlayerIdentityResolver.boundedIndexOf(
								displayName, player.playerName(), 0) >= 0)
				.findFirst()
				.flatMap(player -> PlayerIdentityResolver.resolveStandard(
						message, senderName, player.uuid(), player.playerName()));
	}

	private static MessageSignatureData signatureOf(SignedMessage message) {
		return message == null ? null : message.signature();
	}
}
