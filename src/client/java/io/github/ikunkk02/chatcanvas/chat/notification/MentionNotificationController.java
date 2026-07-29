package io.github.ikunkk02.chatcanvas.chat.notification;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasChannel;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessage;
import io.github.ikunkk02.chatcanvas.chat.mention.MentionMatcher;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import io.github.ikunkk02.chatcanvas.ChatCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public final class MentionNotificationController {
	private static final MentionNotificationController INSTANCE =
			new MentionNotificationController();
	private final MentionNotificationDeduplicator deduplicator =
			new MentionNotificationDeduplicator();
	private final MentionSoundPlayer soundPlayer = new MentionSoundPlayer();
	private final MentionToastManager toastManager = new MentionToastManager();
	private final MentionFlashOverlay flashOverlay = new MentionFlashOverlay();
	private boolean registered;

	private MentionNotificationController() {
	}

	public static MentionNotificationController instance() {
		return INSTANCE;
	}

	public synchronized void register() {
		if (registered) return;
		registered = true;
		flashOverlay.register();
	}

	public void receive(ChatCanvasMessage message) {
		Minecraft client = Minecraft.getInstance();
		boolean debugSelfMention = MentionDebugPolicy.allowsSelfMention(client);
		if (client.player == null || message == null
				|| message.channel() != ChatCanvasChannel.PLAYER_CHAT
				|| (message.selfMessage() && !debugSelfMention)
				|| !message.mentionedCurrentPlayer()) return;
		MentionConfig config = ChatCanvasConfig.instance().mention().sanitized();
		PlayerChatIdentity identity = message.senderName() == null ? null
				: new PlayerChatIdentity(
						message.senderUuid(), message.senderName().getString(), true);
		if (!debugSelfMention && config.ignoreOwnMessages() && isOwn(
				identity, client.player.getGameProfile().name(), client.player.uuid())) return;
		if (!deduplicator.accept(message.messageId(), message.receivedAt())) {
			ChatCanvas.LOGGER.debug("Mention notification suppressed as duplicate: id={}",
					message.messageId());
			return;
		}
		MentionNotificationEvent event = new MentionNotificationEvent(
				message.messageId(), identity, message.content(),
				message.content().getString(), message.receivedAt());
		soundPlayer.playConfigured(config);
		toastManager.show(event, config);
		flashOverlay.trigger(config);
		ChatCanvas.LOGGER.debug(
				"Mention notification delivered: id={} sender={} sound={} toast={} flash={} debugSelf={}",
				message.messageId(),
				identity == null ? "<unknown>" : identity.playerName(),
				config.soundEnabled(), config.toastEnabled(), config.flashEnabled(),
				debugSelfMention);
	}

	public void testSound(MentionConfig config) {
		soundPlayer.test(config);
	}

	public void clearSession() {
		deduplicator.clear();
		flashOverlay.clear();
	}

	private static boolean isOwn(PlayerChatIdentity sender, String localName, UUID localUuid) {
		if (sender == null) return false;
		if (sender.uuid() != null && sender.uuid().equals(localUuid)) return true;
		return PlayerColorConfig.normalizeName(sender.playerName())
				.equals(PlayerColorConfig.normalizeName(localName));
	}
}
