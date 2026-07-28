package io.github.ikunkk02.chatcanvas.chat.identity;

import com.mojang.authlib.GameProfile;
import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputController;
import io.github.ikunkk02.chatcanvas.chat.command.CommandToolRuntime;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress;
import io.github.ikunkk02.chatcanvas.chat.render.DualChatHudRenderer;
import io.github.ikunkk02.chatcanvas.chat.message.MessageIngress;
import io.github.ikunkk02.chatcanvas.chat.notification.MentionNotificationController;
import io.github.ikunkk02.chatcanvas.voice.VoiceInputManager;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public final class PlayerChatCapture {
	private static boolean registered;

	private PlayerChatCapture() {
	}

	public static synchronized void register() {
		if (registered) return;
		registered = true;
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, timestamp) -> {
			Optional<ChatMessageMetadata> metadata = standardMetadata(
					message, signedMessage, sender, params.name());
			metadata.ifPresent(value -> ChatMessageMetadataRegistry.instance()
					.registerIncoming(message, signatureOf(signedMessage), value));
			ChatCanvasMessageIngress.instance().registerIncoming(
					message,
					signatureOf(signedMessage),
					MessageIngress.CHAT,
					metadata.map(ChatMessageMetadata::sender).orElse(null),
					params.name(),
					false);
		});
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (overlay) return;
			Optional<ChatMessageMetadata> metadata =
					PluginChatFallbackResolver.resolve(message, PlayerRosterTracker.onlinePlayers());
			metadata.ifPresent(value -> ChatMessageMetadataRegistry.instance()
					.registerIncoming(message, null, value));
			ChatCanvasMessageIngress.instance().registerIncoming(
					message, null, MessageIngress.GAME,
					metadata.map(ChatMessageMetadata::sender).orElse(null),
					null, false);
		});
		ClientSendMessageEvents.CHAT.register(
				message -> ChatCanvasInputController.instance()
						.recordSentPlayerChat(message));
		ClientSendMessageEvents.COMMAND.register(command -> {
			ChatCanvasInputController.instance().recordExecutedCommand(command);
			ChatCanvasMessageIngress.instance().acceptCommand(command);
			CommandToolRuntime.recordExecuted(command);
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			CommandToolRuntime.beginSession(client);
			ChatCanvasInputController.instance().clearSession();
			ChatCanvasMessageIngress.instance().clearWorld();
			DualChatHudRenderer.instance().resetWorld();
			MentionNotificationController.instance().clearSession();
			PlayerRosterTracker.refresh(handler);
			io.github.ikunkk02.chatcanvas.chat.history.LocalChatLogService.instance()
					.switchContext(io.github.ikunkk02.chatcanvas.chat.history.ChatLogContexts.current(client));
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			VoiceInputManager.instance().cancel();
			CommandToolRuntime.endSession();
			ChatCanvasInputController.instance().clearSession();
			PlayerRosterTracker.clear();
			ChatMessageMetadataRegistry.instance().clearAll();
			PlayerNameHitboxRegistry.clear();
			PlayerNameDoubleClickHandler.instance().reset();
			ChatCanvasMessageIngress.instance().clearWorld();
			DualChatHudRenderer.instance().resetWorld();
			MentionNotificationController.instance().clearSession();
			io.github.ikunkk02.chatcanvas.chat.history.LocalChatLogService.instance().switchContext(null);
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

	private static MessageSignature signatureOf(SignedMessage message) {
		return message == null ? null : message.signature();
	}
}
