package io.github.ikunkk02.chatcanvas.chat.notification;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import io.github.ikunkk02.chatcanvas.chat.mention.MentionMatcher;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

public final class MentionNotificationController {
	private static final MentionNotificationController INSTANCE =
			new MentionNotificationController();
	private final MentionNotificationDeduplicator deduplicator =
			new MentionNotificationDeduplicator();
	private final WeakHashMap<Text, UUID> unsignedIds = new WeakHashMap<>();
	private final MentionSoundPlayer soundPlayer = new MentionSoundPlayer();
	private final MentionToastManager toastManager = new MentionToastManager();
	private final MentionFlashOverlay flashOverlay = new MentionFlashOverlay();

	private MentionNotificationController() {
	}

	public static MentionNotificationController instance() {
		return INSTANCE;
	}

	public void register() {
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
		String fingerprint = fingerprint(message, identity, receivedAtMs);
		if (!deduplicator.accept(messageId, fingerprint, receivedAtMs)) return;
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
		return unsignedIds.computeIfAbsent(message, ignored -> UUID.randomUUID());
	}

	private static String fingerprint(Text message, PlayerChatIdentity sender, long receivedAtMs) {
		String senderKey = sender == null ? "?"
				: sender.uuid() != null ? sender.uuid().toString()
				: PlayerColorConfig.normalizeName(sender.playerName());
		String structure = message.getClass().getName() + ":" + message.hashCode();
		byte[] bytes = (senderKey + '\n' + structure + '\n' + message.getString())
				.getBytes(StandardCharsets.UTF_8);
		return UUID.nameUUIDFromBytes(bytes).toString();
	}

	private static boolean isOwn(PlayerChatIdentity sender, String localName, UUID localUuid) {
		if (sender == null) return false;
		if (sender.uuid() != null && sender.uuid().equals(localUuid)) return true;
		return PlayerColorConfig.normalizeName(sender.playerName())
				.equals(PlayerColorConfig.normalizeName(localName));
	}
}
