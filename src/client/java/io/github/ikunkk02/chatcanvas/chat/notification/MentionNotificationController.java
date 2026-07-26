package io.github.ikunkk02.chatcanvas.chat.notification;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import io.github.ikunkk02.chatcanvas.chat.mention.MentionMatcher;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

public final class MentionNotificationController {
	private static final MentionNotificationController INSTANCE =
			new MentionNotificationController();
	private final MentionNotificationDeduplicator deduplicator =
			new MentionNotificationDeduplicator();
	private final MentionMessageIdRegistry<Text> unsignedIds =
			new MentionMessageIdRegistry<>();
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

	public void receive(Text message, MessageSignatureData signature,
						Optional<PlayerChatIdentity> sender, long receivedAtMs) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || message == null) return;
		MentionConfig config = ChatCanvasConfig.instance().mention().sanitized();
		String localName = client.player.getGameProfile().getName();
		String plain = message.getString();
		if (MentionMatcher.findMentions(plain, localName, config.requireAtSymbol()).isEmpty()) return;
		PlayerChatIdentity identity = sender == null ? null : sender.orElse(null);
		if (config.ignoreOwnMessages() && isOwn(identity, localName, client.player.getUuid())) return;
		UUID messageId = messageId(message, signature);
		if (!deduplicator.accept(messageId, receivedAtMs)) return;
		MentionNotificationEvent event = new MentionNotificationEvent(
				messageId, identity, message, plain, receivedAtMs);
		soundPlayer.playConfigured(config);
		toastManager.show(event, config);
		flashOverlay.trigger(config);
	}

	public void testSound(MentionConfig config) {
		soundPlayer.test(config);
	}

	public void clearSession() {
		deduplicator.clear();
		unsignedIds.clear();
		flashOverlay.clear();
	}

	private synchronized UUID messageId(Text message, MessageSignatureData signature) {
		if (signature != null) return UUID.nameUUIDFromBytes(signature.data());
		return unsignedIds.idFor(message);
	}

	private static boolean isOwn(PlayerChatIdentity sender, String localName, UUID localUuid) {
		if (sender == null) return false;
		if (sender.uuid() != null && sender.uuid().equals(localUuid)) return true;
		return PlayerColorConfig.normalizeName(sender.playerName())
				.equals(PlayerColorConfig.normalizeName(localName));
	}
}
