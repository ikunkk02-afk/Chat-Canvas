package io.github.ikunkk02.chatcanvas.chat.command;

import java.util.List;

public record CommandToolData(
		int version,
		boolean migrationCompleted,
		List<CommandHistoryEntry> recent,
		List<FavoriteCommandEntry> favorites
) {
	public static final int CURRENT_VERSION = 1;
	public static final CommandToolData EMPTY =
			new CommandToolData(CURRENT_VERSION, false, List.of(), List.of());

	public CommandToolData {
		version = Math.max(1, version);
		recent = recent == null ? List.of() : List.copyOf(recent);
		favorites = favorites == null ? List.of() : List.copyOf(favorites);
	}
}
