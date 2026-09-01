package io.github.ikunkk02.chatcanvas.chat.identity;

import com.mojang.authlib.GameProfile;
import io.github.ikunkk02.chatcanvas.chat.command.CommandToolRuntime;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputController;
import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress;
import io.github.ikunkk02.chatcanvas.chat.message.MessageIngress;
import io.github.ikunkk02.chatcanvas.chat.notification.MentionNotificationController;
import io.github.ikunkk02.chatcanvas.chat.render.DualChatHudRenderer;
import io.github.ikunkk02.chatcanvas.voice.VoiceInputManager;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class PlayerChatCapture {
	private static boolean registered;

	private PlayerChatCapture() {}

	public static synchronized void register() {
		if (registered) return;
		registered = true;
		NeoForge.EVENT_BUS.addListener(PlayerChatCapture::onPlayerMessage);
		NeoForge.EVENT_BUS.addListener(PlayerChatCapture::onSystemMessage);
		NeoForge.EVENT_BUS.addListener(PlayerChatCapture::onOutgoingChat);
		NeoForge.EVENT_BUS.addListener(PlayerChatCapture::onJoin);
		NeoForge.EVENT_BUS.addListener(PlayerChatCapture::onDisconnect);
	}

	private static void onPlayerMessage(ClientChatReceivedEvent.Player event) {
		Component message = event.getMessage();
		PlayerChatMessage signedMessage = event.getPlayerChatMessage();
		Component senderName = event.getBoundChatType().name();
		GameProfile sender = profile(event.getSender());
		Optional<ChatMessageMetadata> metadata =
				standardMetadata(message, signedMessage, sender, senderName);
		metadata.ifPresent(value -> ChatMessageMetadataRegistry.instance()
				.registerIncoming(message, signatureOf(signedMessage), value));
		ChatCanvasMessageIngress.instance().registerIncoming(
				message, signatureOf(signedMessage), MessageIngress.CHAT,
				metadata.map(ChatMessageMetadata::sender).orElse(null), senderName, false);
	}

	private static void onSystemMessage(ClientChatReceivedEvent.System event) {
		if (event.isOverlay()) return;
		Component message = event.getMessage();
		Optional<ChatMessageMetadata> metadata =
				PluginChatFallbackResolver.resolve(message, PlayerRosterTracker.onlinePlayers());
		metadata.ifPresent(value -> ChatMessageMetadataRegistry.instance()
				.registerIncoming(message, null, value));
		ChatCanvasMessageIngress.instance().registerIncoming(
				message, null, MessageIngress.GAME,
				metadata.map(ChatMessageMetadata::sender).orElse(null), null, false);
	}

	private static void onOutgoingChat(ClientChatEvent event) {
		ChatCanvasInputController.instance().recordSentPlayerChat(event.getMessage());
	}

	private static void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
		Minecraft client = Minecraft.getInstance();
		CommandToolRuntime.beginSession(client);
		ChatCanvasInputController.instance().clearSession();
		ChatCanvasMessageIngress.instance().clearWorld();
		DualChatHudRenderer.instance().resetWorld();
		MentionNotificationController.instance().clearSession();
		ClientPacketListener handler = client.getConnection();
		if (handler != null) PlayerRosterTracker.refresh(handler);
		io.github.ikunkk02.chatcanvas.chat.history.LocalChatLogService.instance()
				.switchContext(io.github.ikunkk02.chatcanvas.chat.history.ChatLogContexts.current(client));
	}

	private static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
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
	}

	private static GameProfile profile(java.util.UUID uuid) {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null || uuid == null) return null;
		PlayerInfo info = connection.getPlayerInfo(uuid);
		return info == null ? null : info.getProfile();
	}

	private static Optional<ChatMessageMetadata> standardMetadata(
			Component message, PlayerChatMessage signedMessage,
			GameProfile sender, Component senderName) {
		if (sender != null) {
			return PlayerIdentityResolver.resolveStandard(
					message, senderName, sender.getId(), sender.getName());
		}
		String displayName = senderName == null ? "" : senderName.getString();
		return PlayerRosterTracker.onlinePlayers().stream()
				.filter(player -> displayName.equalsIgnoreCase(player.playerName())
						|| PlayerIdentityResolver.boundedIndexOf(displayName, player.playerName(), 0) >= 0)
				.findFirst()
				.flatMap(player -> PlayerIdentityResolver.resolveStandard(
						message, senderName, player.uuid(), player.playerName()));
	}

	private static MessageSignature signatureOf(PlayerChatMessage message) {
		return message == null ? null : message.signature();
	}
}
