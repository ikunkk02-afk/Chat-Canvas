package io.github.ikunkk02.chatcanvas.chat.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandToolStorageTest {
	@TempDir
	Path directory;

	@Test
	void roundTripsVersionedRecentAndFavorites() {
		Path path = directory.resolve("commands.json");
		CommandHistoryEntry recent = CommandHistoryEntry.create(
				"/time set day", 200L, "singleplayer");
		FavoriteCommandEntry favorite = FavoriteCommandEntry.create(
				"中文 \"天气\"", "/weather clear", 0, 100L);
		CommandToolStorage storage = new CommandToolStorage(path);

		assertTrue(storage.save(new CommandToolData(
				1, true, List.of(recent), List.of(favorite))));
		CommandToolStorage.LoadResult result = storage.load();

		assertEquals(CommandToolStorage.LoadStatus.OK, result.status());
		assertEquals(List.of(recent), result.getValue().recent());
		assertEquals(List.of(favorite), result.getValue().favorites());
	}

	@Test
	void backsUpCorruptDataAndRecoversEmpty() throws Exception {
		Path path = directory.resolve("commands.json");
		Files.writeString(path, "{broken");
		CommandToolStorage storage = new CommandToolStorage(path,
				Clock.fixed(Instant.ofEpochMilli(555L), ZoneOffset.UTC));

		CommandToolStorage.LoadResult result = storage.load();

		assertEquals(CommandToolStorage.LoadStatus.RECOVERED_CORRUPT,
				result.status());
		assertNotNull(result.backupPath());
		assertTrue(Files.exists(directory.resolve("commands.corrupt-555.json")));
		assertTrue(Files.exists(path));
	}
}
