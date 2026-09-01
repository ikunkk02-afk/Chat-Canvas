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
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandClipboardStorageTest {
	@TempDir
	Path directory;

	@Test
	void roundTripsCommands() {
		Path path = directory.resolve("command_clipboard.json");
		CommandClipboardStorage storage = new CommandClipboardStorage(path);
		SavedCommand command = SavedCommand.create(
				"Creative", "/gamemode creative @s", "Utility", 0, 100L);

		assertTrue(storage.save(new CommandClipboardData(1, List.of(command))));
		CommandClipboardData loaded = storage.load();

		assertEquals(1, loaded.version());
		assertEquals(List.of(command), loaded.commands());
	}

	@Test
	void skipsMalformedEntriesWithoutDiscardingValidEntries() throws Exception {
		Path path = directory.resolve("command_clipboard.json");
		Files.writeString(path, """
				{"version":1,"commands":[
				  {"id":"not-a-uuid","title":"bad","command":"/bad"},
				  {"id":"11111111-1111-1111-1111-111111111111","title":"Day",
				   "command":"/time set day","category":"","favorite":false,
				   "createdAt":1,"updatedAt":1,"lastUsedAt":0,"useCount":0,"sortOrder":0}
				]}
				""");

		CommandClipboardData loaded = new CommandClipboardStorage(path).load();

		assertEquals(1, loaded.commands().size());
		assertEquals("/time set day", loaded.commands().getFirst().command());
	}

	@Test
	void backsUpCorruptRootAndCreatesEmptyData() throws Exception {
		Path path = directory.resolve("command_clipboard.json");
		Files.writeString(path, "not json");
		Clock clock = Clock.fixed(Instant.ofEpochMilli(1234L), ZoneOffset.UTC);

		CommandClipboardData loaded = new CommandClipboardStorage(path, clock).load();

		assertTrue(loaded.commands().isEmpty());
		assertTrue(Files.exists(directory.resolve("command_clipboard.corrupt-1234.json")));
		assertTrue(Files.exists(path));
	}
}
