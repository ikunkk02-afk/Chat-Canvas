package io.github.ikunkk02.chatcanvas.chat.emoji;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmojiRegistryTest {
	@Test
	void defaultRegistryContainsOneHundredThirtySixUniqueCandidates() {
		EmojiRegistry registry = EmojiRegistry.instance();

		assertEquals(136, registry.entries().size());
		assertEquals(136, registry.entries().stream()
				.map(EmojiEntry::unicode).distinct().count());
		assertEquals(96, registry.category(EmojiCategory.SMILEYS).size());
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

		assertEquals(136, registry.entries().stream()
				.map(EmojiEntry::translationKey).distinct().count());
		assertEquals("chat_canvas.emoji.name.u2764_fe0f",
				registry.find("❤️").translationKey());
	}

	@Test
	void keepsEveryLegacyEmojiIdentifierStable() {
		List<String> legacy = List.of(
				"😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
				"🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😋", "😎", "🤓",
				"🤔", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "😔",
				"😢", "😭", "😡", "🤬", "😱", "😨", "😴", "🤢", "🤮", "💀",
				"👻", "👍", "👎", "👏", "🙌", "🤝", "🙏", "💪", "👀", "❤️",
				"💔", "💕", "💖", "🎉", "🎊", "🔥", "✨", "⭐", "✅", "❌",
				"⚠️", "❓", "❗", "🐶", "🐱", "🐷", "🐔", "🍎", "🍞", "🍖",
				"🍗", "🚗", "🚲", "✈️", "🚀", "🏠", "🌍", "⚔️", "🛡️", "🏹", "⛏️");

		assertEquals(81, legacy.size());
		legacy.forEach(unicode -> assertNotNull(
				EmojiRegistry.instance().find(unicode), unicode));
	}

	@Test
	void everyEmojiNameIsTranslatedInAllSupportedLanguages() throws Exception {
		for (String locale : List.of("en_us", "zh_cn", "zh_tw")) {
			String resource = "assets/chatcanvas/lang/" + locale + ".json";
			InputStream resourceStream = getClass().getClassLoader()
					.getResourceAsStream(resource);
			assertNotNull(resourceStream, resource);
			try (InputStream stream = resourceStream;
				 InputStreamReader reader = new InputStreamReader(
						 stream, StandardCharsets.UTF_8)) {
				JsonObject translations = JsonParser.parseReader(reader).getAsJsonObject();
				for (EmojiEntry entry : EmojiRegistry.instance().entries()) {
					assertTrue(translations.has(entry.translationKey()),
							locale + ": " + entry.translationKey());
				}
			}
		}
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
