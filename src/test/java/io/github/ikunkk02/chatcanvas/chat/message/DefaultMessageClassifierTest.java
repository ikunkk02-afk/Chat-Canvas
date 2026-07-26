package io.github.ikunkk02.chatcanvas.chat.message;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMessageClassifierTest {
	private final DefaultMessageClassifier classifier = new DefaultMessageClassifier();
	private final UUID localUuid = UUID.fromString("11111111-2222-3333-4444-555555555555");
	private final UUID remoteUuid = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

	@Test
	void authoritativeChatAlwaysUsesPlayerChannelAndDetectsSelf() {
		PlayerChatIdentity remote = new PlayerChatIdentity(remoteUuid, "Alex", true);
		ClassifiedMessage other = classifier.classify(
				Text.literal("<Alex> hello"),
				context(MessageIngress.CHAT, remote, List.of(remote)));
		assertEquals(ChatCanvasChannel.PLAYER_CHAT, other.channel());
		assertEquals(ChatCanvasMessageSource.PLAYER, other.source());
		assertFalse(other.selfMessage());

		PlayerChatIdentity self = new PlayerChatIdentity(localUuid, "Steve", true);
		ClassifiedMessage own = classifier.classify(
				Text.literal("<Steve> hello"),
				context(MessageIngress.CHAT, self, List.of(self)));
		assertEquals(ChatCanvasMessageSource.SELF_PLAYER, own.source());
		assertTrue(own.selfMessage());
	}

	@Test
	void pluginChatRequiresUniqueOnlinePlayerAndHeaderDelimiter() {
		PlayerChatIdentity alex = new PlayerChatIdentity(remoteUuid, "Alex", true);
		MessageContext game = context(MessageIngress.GAME, null, List.of(alex));
		assertEquals(ChatCanvasChannel.PLAYER_CHAT,
				classifier.classify(Text.literal("[VIP] Alex: hello"), game).channel());
		assertEquals(ChatCanvasChannel.COMMAND_SYSTEM,
				classifier.classify(Text.literal("Server is saving Alex data"), game).channel());
		assertEquals(ChatCanvasChannel.COMMAND_SYSTEM,
				classifier.classify(Text.literal("Alex was slain by Zombie"), game).channel());
	}

	@Test
	void vanillaSystemKindsStayInCommandChannel() {
		MessageContext game = context(MessageIngress.GAME, null, List.of());
		assertEquals(ChatCanvasMessageSource.DEATH_MESSAGE, classifier.classify(
				Text.translatable("death.attack.generic", Text.literal("Alex")), game).source());
		assertEquals(ChatCanvasMessageSource.PLAYER_JOIN, classifier.classify(
				Text.translatable("multiplayer.player.joined", Text.literal("Alex")), game).source());
		assertEquals(ChatCanvasMessageSource.PLAYER_LEAVE, classifier.classify(
				Text.translatable("multiplayer.player.left", Text.literal("Alex")), game).source());
		assertEquals(ChatCanvasMessageSource.COMMAND_ERROR, classifier.classify(
				Text.translatable("commands.generic.unknown"), game).source());
	}

	@Test
	void unsignedVanillaPlayerChatTranslationStillUsesPlayerChannel() {
		PlayerChatIdentity alex = new PlayerChatIdentity(remoteUuid, "Alex", true);
		ClassifiedMessage classified = classifier.classify(
				Text.translatable("chat.type.text", Text.literal("Alex"),
						Text.literal("你好，@Steve！")),
				context(MessageIngress.DIRECT_HUD, null, List.of(alex)));
		assertEquals(ChatCanvasChannel.PLAYER_CHAT, classified.channel());
		assertEquals(ChatCanvasMessageSource.PLAYER, classified.source());
		assertFalse(classified.selfMessage());
	}

	@Test
	void translatedPlayerChatRequiresUniqueOnlineSender() {
		PlayerChatIdentity first = new PlayerChatIdentity(remoteUuid, "Alex", true);
		PlayerChatIdentity duplicate = new PlayerChatIdentity(UUID.randomUUID(), "alex", true);
		ClassifiedMessage classified = classifier.classify(
				Text.translatable("chat.type.text", Text.literal("Alex"), Text.literal("@Steve")),
				context(MessageIngress.DIRECT_HUD, null, List.of(first, duplicate)));
		assertEquals(ChatCanvasChannel.COMMAND_SYSTEM, classified.channel());
	}

	@Test
	void commandInputAndUnknownMessagesUseCommandChannel() {
		assertEquals(ChatCanvasMessageSource.COMMAND_INPUT, classifier.classify(
				Text.literal("/time set day"),
				context(MessageIngress.COMMAND_INPUT, null, List.of())).source());
		ClassifiedMessage unknown = classifier.classify(
				Text.literal("unclassified notice"),
				context(MessageIngress.DIRECT_HUD, null, List.of()));
		assertEquals(ChatCanvasChannel.COMMAND_SYSTEM, unknown.channel());
		assertEquals(ChatCanvasMessageSource.UNKNOWN, unknown.source());
	}

	private MessageContext context(
			MessageIngress ingress, PlayerChatIdentity sender,
			List<PlayerChatIdentity> online) {
		return new MessageContext(ingress, null, sender,
				sender == null ? null : Text.literal(sender.playerName()),
				online, localUuid, "Steve", false);
	}
}
