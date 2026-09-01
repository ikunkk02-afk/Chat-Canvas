package io.github.ikunkk02.chatcanvas.chat.emoji;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;


class EmojiRecentManagerTest {
	@TempDir
	Path directory;

	@Test
	void selectingAgainMovesToFrontAndIncrementsCount() {
		EmojiRecentManager first = manager(100L);
		assertTrue(first.recordSelected("😀"));

		EmojiRecentManager second = manager(200L);
		assertTrue(second.recordSelected("😀"));
		assertTrue(second.recordSelected("❤️"));

		assertEquals("❤️", second.recent().getFirst().unicode());
		RecentEmojiEntry grin = second.recent().stream()
				.filter(entry -> entry.unicode().equals("😀")).findFirst().orElseThrow();
		assertEquals(2, grin.useCount());
	}

	@Test
	void capsRecentListAtTwentyFourWithoutDuplicates() {
		EmojiRecentManager manager = manager(1000L);
		int index = 0;
		for (EmojiEntry entry : EmojiRegistry.instance().entries()) {
			assertTrue(manager.recordSelected(entry.unicode()));
			if (++index == 30) break;
		}

		assertEquals(EmojiRecentManager.MAX_RECENT, manager.recent().size());
		assertEquals(EmojiRecentManager.MAX_RECENT, manager.recent().stream()
				.map(RecentEmojiEntry::unicode).distinct().count());
	}

	@Test
	void ignoresUnknownEmoji() {
		EmojiRecentManager manager = manager(100L);

		assertFalse(manager.recordSelected("custom:emoji"));
		assertTrue(manager.recent().isEmpty());
	}

	private EmojiRecentManager manager(long millis) {
		return new EmojiRecentManager(
				new EmojiRecentStorage(directory.resolve("emoji.json")),
				Clock.fixed(Instant.ofEpochMilli(millis), ZoneOffset.UTC));
	}
}
