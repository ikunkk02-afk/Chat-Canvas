package io.github.ikunkk02.chatcanvas.chat.emoji;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmojiRegistryTest {
	@Test
	void defaultRegistryContainsEightyOneUniqueCandidates() {
		EmojiRegistry registry = EmojiRegistry.instance();

		assertEquals(81, registry.entries().size());
		assertEquals(81, registry.entries().stream()
				.map(EmojiEntry::unicode).distinct().count());
	}

	@Test
	void searchesChineseEnglishCaseInsensitivelyAndUnicode() {
		EmojiRegistry registry = EmojiRegistry.instance();

		assertTrue(registry.search("笑").stream()
				.anyMatch(entry -> entry.unicode().equals("😀")));
		assertTrue(registry.search("HAPPY").stream()
				.anyMatch(entry -> entry.unicode().equals("😀")));
		assertTrue(registry.search("heart").stream()
				.anyMatch(entry -> entry.unicode().equals("❤️")));
		assertEquals(List.of("🔥"), registry.search("🔥").stream()
				.map(EmojiEntry::unicode).toList());
	}

	@Test
	void everyNonRecentCategoryHasEntries() {
		for (EmojiCategory category : EmojiCategory.values()) {
			if (category == EmojiCategory.RECENT) continue;
			assertFalse(EmojiRegistry.instance().category(category).isEmpty(),
					category.name());
		}
	}

	@Test
	void everyEmojiHasAUniqueStableTranslationKey() {
		EmojiRegistry registry = EmojiRegistry.instance();

		assertEquals(81, registry.entries().stream()
				.map(EmojiEntry::translationKey).distinct().count());
		assertEquals("chat_canvas.emoji.name.u2764_fe0f",
				registry.find("❤️").translationKey());
	}

	@Test
	void constructorDeduplicatesUnicode() {
		EmojiEntry first = new EmojiEntry(
				"😀", "笑脸", "grinning", List.of("笑"), EmojiCategory.SMILEYS);
		EmojiEntry duplicate = new EmojiEntry(
				"😀", "重复", "duplicate", List.of(), EmojiCategory.SYMBOLS);

		EmojiRegistry registry = new EmojiRegistry(List.of(first, duplicate));

		assertEquals(List.of(first), registry.entries());
		assertSame(first, registry.find("😀"));
	}
}
