package io.github.ikunkk02.chatcanvas.chat.message;

import io.github.ikunkk02.chatcanvas.chat.command.SensitiveCommandMasker;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerRosterTracker;
import io.github.ikunkk02.chatcanvas.chat.mention.MentionMatcher;
import io.github.ikunkk02.chatcanvas.chat.notification.MentionNotificationController;
import io.github.ikunkk02.chatcanvas.chat.notification.MentionDebugPolicy;
import io.github.ikunkk02.chatcanvas.ChatCanvas;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

public final class ChatCanvasMessageIngress {
	private static final ChatCanvasMessageIngress INSTANCE = new ChatCanvasMessageIngress();
	private final PendingMessageContextRegistry pending = new PendingMessageContextRegistry();
	private final MessageClassifier classifier = new DefaultMessageClassifier();
	private final ChatCanvasMessageManager manager = ChatCanvasMessageManager.instance();
	private boolean fallbackBridge;

	private ChatCanvasMessageIngress() {
	}

	public static ChatCanvasMessageIngress instance() {
		return INSTANCE;
	}

	public void registerIncoming(
			Component message, MessageSignature signature,
			MessageIngress ingress, PlayerChatIdentity sender, Component senderName,
			boolean overlay) {
		pending.register(message, signature,
				context(ingress, signature, sender, senderName, overlay));
	}

	public boolean acceptFromChatHud(Component message, MessageSignature signature) {
		if (fallbackBridge) return false;
		PendingMessageContextRegistry.PendingMessage registered =
				pending.consume(message, signature);
		MessageContext context = registered == null
				? context(MessageIngress.DIRECT_HUD, signature, null, null, false)
				: registered.context();
		UUID id = registered == null
			? signature == null ? UUID.randomUUID() : UUID.nameUUIDFromBytes(signature.bytes())
				: registered.messageId();
		return accept(id, message, context, System.currentTimeMillis());
	}

	public boolean acceptCommand(String commandWithoutSlash) {
		Component content = Component.literal(SensitiveCommandMasker.display(commandWithoutSlash));
		boolean accepted = accept(UUID.randomUUID(), content,
				context(MessageIngress.COMMAND_INPUT, null, null, null, false),
				System.currentTimeMillis());
		if (accepted) mirrorToVanilla(content);
		return accepted;
	}

	public void reportError(Component summary, Throwable throwable) {
		if (throwable != null) {
			io.github.ikunkk02.chatcanvas.ChatCanvas.LOGGER.error(summary.getString(), throwable);
		}
		if (accept(UUID.randomUUID(), summary,
				context(MessageIngress.CHAT_CANVAS_ERROR, null, null, null, false),
				System.currentTimeMillis())) {
			mirrorToVanilla(summary);
		}
	}

	public void clearWorld() {
		pending.clear();
		manager.clearWorld();
	}

	private boolean accept(UUID id, Component message, MessageContext context, long receivedAt) {
		ClassifiedMessage classified = classifier.classify(message, context);
		Minecraft client = Minecraft.getInstance();
		boolean debugSelfMention = MentionDebugPolicy.allowsSelfMention(client);
		boolean mentionMatched = isMention(message);
		boolean mentioned = classified.channel() == ChatCanvasChannel.PLAYER_CHAT
				&& (!classified.selfMessage() || debugSelfMention)
				&& mentionMatched;
		ChatCanvas.LOGGER.debug(
				"Mention trace id={} ingress={} channel={} source={} sender={} local={} self={} matched={} debugSelf={}",
				id, context.ingress(), classified.channel(), classified.source(),
				classified.senderName() == null ? "<unknown>" : classified.senderName().getString(),
				client.player == null ? "<none>" : client.player.getGameProfile().name(),
				classified.selfMessage(), mentionMatched, debugSelfMention);
		ChatCanvasMessage result = new ChatCanvasMessage(
				id,
				classified.channel(),
				classified.source(),
				classified.senderUuid(),
				classified.senderName(),
				classified.content(),
				receivedAt,
				classified.selfMessage(),
				mentioned
		);
		if (!manager.add(result)) return false;
		if (result.channel() == ChatCanvasChannel.PLAYER_CHAT) {
			MentionNotificationController.instance().receive(result);
		}
		io.github.ikunkk02.chatcanvas.chat.history.LocalChatLogService.instance().record(result);
		return true;
	}

	private void mirrorToVanilla(Component message) {
		Minecraft client = Minecraft.getInstance();
		if (client.gui == null) return;
		try {
			fallbackBridge = true;
			client.gui.getChat().addClientSystemMessage(message);
		} finally {
			fallbackBridge = false;
		}
	}

	private static boolean isMention(Component message) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return false;
		return !MentionMatcher.findMentions(
				message.getString(),
				client.player.getGameProfile().name(),
				ChatCanvasConfig.instance().mention().requireAtSymbol()).isEmpty();
	}

	private static MessageContext context(
			MessageIngress ingress, MessageSignature signature,
			PlayerChatIdentity sender, Component senderName, boolean overlay) {
		Minecraft client = Minecraft.getInstance();
		UUID localUuid = client.player == null ? null : client.player.getUUID();
		String localName = client.player == null
				? "" : client.player.getGameProfile().name();
		List<PlayerChatIdentity> online = List.copyOf(PlayerRosterTracker.onlinePlayers());
		return new MessageContext(
				ingress, signature, sender, senderName, online,
				localUuid, localName, overlay);
	}
}
