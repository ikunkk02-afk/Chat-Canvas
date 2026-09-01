package io.github.ikunkk02.chatcanvas.chat.emoji;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmojiRecentStorageTest {
	@TempDir
	Path directory;

	@Test
	void roundTripsVersionedEntries() {
		Path path = directory.resolve("emoji.json");
		EmojiRecentStorage storage = new EmojiRecentStorage(path);
		EmojiRecentData data = new EmojiRecentData(1,
				List.of(new RecentEmojiEntry("😀", 100L, 2)));

		assertTrue(storage.save(data));
		EmojiRecentStorage.LoadResult result = storage.load();

		assertEquals(EmojiRecentStorage.LoadStatus.OK, result.status());
		assertEquals(data, result.data());
	}

	@Test
	void partiallyRecoversInvalidAndDuplicateEntries() throws Exception {
		Path path = directory.resolve("emoji.json");
		Files.writeString(path, """
				{"version":1,"recent":[
				  {"unicode":"😀","lastUsedAt":100,"useCount":1},
				  {"unicode":"😀","lastUsedAt":200,"useCount":3},
				  {"unicode":"not-registered","lastUsedAt":300,"useCount":1},
				  {"unicode":"","lastUsedAt":0,"useCount":0}
				]}""");

		EmojiRecentStorage.LoadResult result =
				new EmojiRecentStorage(path).load();

		assertEquals(EmojiRecentStorage.LoadStatus.PARTIAL_RECOVERY, result.status());
		assertEquals(List.of(new RecentEmojiEntry("😀", 200L, 3)),
				result.data().recent());
	}

	@Test
	void backsUpCorruptFileAndRecoversEmpty() throws Exception {
		Path path = directory.resolve("emoji.json");
		Files.writeString(path, "{broken");
		EmojiRecentStorage storage = new EmojiRecentStorage(path,
				Clock.fixed(Instant.ofEpochMilli(555L), ZoneOffset.UTC));

		EmojiRecentStorage.LoadResult result = storage.load();

		assertEquals(EmojiRecentStorage.LoadStatus.RECOVERED_CORRUPT,
				result.status());
		assertNotNull(result.failure());
		assertTrue(Files.exists(directory.resolve("emoji.corrupt-555.json")));
		assertTrue(Files.exists(path));
		assertEquals(EmojiRecentData.EMPTY, result.data());
	}
}
