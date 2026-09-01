package io.github.ikunkk02.chatcanvas.chat.command;

import io.github.ikunkk02.chatcanvas.config.CommandClipboardConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandToolManagerTest {
	@TempDir
	Path directory;

	@Test
	void recordsOnlyNonSensitiveExecutedCommandsAndMovesDuplicatesToTop() {
		CommandToolManager manager = manager(CommandClipboardConfig.DEFAULT);

		manager.recordExecuted("time set day", "one", 100L);
		manager.recordExecuted("/weather clear", "one", 200L);
		manager.recordExecuted("/time set day", "one", 300L);
		manager.recordExecuted("/login secret", "one", 400L);
		manager.recordExecuted("/token abc", "one", 500L);

		assertEquals(List.of("/time set day", "/weather clear"),
				manager.recent().stream().map(CommandHistoryEntry::command).toList());
		assertEquals(300L, manager.recent().getFirst().executedAt());
		assertTrue(manager.flush());
	}

	@Test
	void respectsCustomNoRecordRuleAndCapacity() {
		CommandClipboardConfig options = new CommandClipboardConfig(
				true, true, null, false, true, 200, Set.of(),
				true, 10, false, Set.of("warp"));
		CommandToolManager manager = manager(options);

		manager.recordExecuted("/warp home", "one", 1L);
		for (int i = 0; i < 12; i++) {
			manager.recordExecuted("/say " + i, "one", i + 2L);
		}

		assertEquals(10, manager.recent().size());
		assertFalse(manager.recent().stream()
				.anyMatch(entry -> entry.command().startsWith("/warp")));
	}

	@Test
	void migratesLegacySavedCommandsOnceAsFavorites() {
		Path legacyPath = directory.resolve("command_clipboard.json");
		CommandClipboardStorage legacy = new CommandClipboardStorage(legacyPath);
		SavedCommand old = SavedCommand.create(
				"Home", "/home", "Server", 0, 10L);
		assertTrue(legacy.save(new CommandClipboardData(1, List.of(old))));
		Path newPath = directory.resolve("commands.json");

		CommandToolManager first = new CommandToolManager(
				new CommandToolStorage(newPath), legacy, legacyPath,
				() -> CommandClipboardConfig.DEFAULT, (summary, error) -> { });
		CommandToolManager second = new CommandToolManager(
				new CommandToolStorage(newPath), legacy, legacyPath,
				() -> CommandClipboardConfig.DEFAULT, (summary, error) -> { });

		assertEquals(1, first.favorites().size());
		assertEquals(1, second.favorites().size());
		assertEquals("/home", second.favorites().getFirst().command());
	}

	@Test
	void favoriteEditsPreserveUnicodeQuotesAndBackslashes() {
		CommandToolManager manager = manager(CommandClipboardConfig.DEFAULT);

		manager.addFavorite("传送 \"家\"", "/say \"C:\\\\家 🌍\"", 100L);
		assertTrue(manager.flush());

		CommandToolManager reloaded = manager(CommandClipboardConfig.DEFAULT);
		assertEquals("传送 \"家\"", reloaded.favorites().getFirst().name());
		assertEquals("/say \"C:\\\\家 🌍\"",
				reloaded.favorites().getFirst().command());
	}

	private CommandToolManager manager(CommandClipboardConfig options) {
		Path newPath = directory.resolve("commands.json");
		Path legacyPath = directory.resolve("command_clipboard.json");
		return new CommandToolManager(
				new CommandToolStorage(newPath),
				new CommandClipboardStorage(legacyPath),
				legacyPath, () -> options, (summary, error) -> { });
	}
}
