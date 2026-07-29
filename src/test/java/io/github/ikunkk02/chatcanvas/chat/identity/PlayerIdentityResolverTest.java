package io.github.ikunkk02.chatcanvas.chat.identity;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerIdentityResolverTest {
	private static final UUID STEVE_UUID =
			UUID.fromString("11111111-2222-3333-4444-555555555555");

	@Test
	void standardResolverUsesAuthoritativeSenderComponentAndExcludesPrefix() {
		Text sender = Text.literal("[VIP] ")
				.append(Text.literal("Steve").formatted(Formatting.BOLD));
		Text message = Text.literal("<").append(sender).append(Text.literal("> hello Steve"));
		ChatMessageMetadata metadata = PlayerIdentityResolver.resolveStandard(
				message, sender, STEVE_UUID, "Steve").orElseThrow();
		assertEquals(message.getString().indexOf("Steve"), metadata.nameStart());
		assertEquals("Steve", message.getString().substring(
				metadata.nameStart(), metadata.nameEnd()));
		assertTrue(metadata.sender().reliable());
	}

	@Test
	void pluginResolverUsesLongestBoundedNameAndDelimiter() {
		PlayerChatIdentity steve = new PlayerChatIdentity(STEVE_UUID, "Steve", true);
		PlayerChatIdentity steve123 = new PlayerChatIdentity(UUID.randomUUID(), "Steve123", true);
		ChatMessageMetadata metadata = PluginChatFallbackResolver.resolve(
				Text.literal("[VIP] Steve123: hello"), List.of(steve, steve123)).orElseThrow();
		assertEquals("Steve123", metadata.sender().playerName());
	}

	@Test
	void pluginResolverRejectsDeathsAnnouncementsAndAmbiguity() {
		PlayerChatIdentity steve = new PlayerChatIdentity(STEVE_UUID, "Steve", true);
		assertTrue(PluginChatFallbackResolver.resolve(
				Text.translatable("death.attack.generic", Text.literal("Steve")),
				List.of(steve)).isEmpty());
		assertTrue(PluginChatFallbackResolver.resolve(
				Text.literal("Steve was slain by Alex"), List.of(steve)).isEmpty());
		assertTrue(PluginChatFallbackResolver.resolve(
				Text.literal("Advertisement for Steve"), List.of(steve)).isEmpty());
	}

	@Test
	void boundaryDoesNotMatchSteveInsideSteve123() {
		PlayerChatIdentity steve = new PlayerChatIdentity(STEVE_UUID, "Steve", true);
		assertTrue(PluginChatFallbackResolver.resolve(
				Text.literal("Steve123: hello"), List.of(steve)).isEmpty());
	}
}
