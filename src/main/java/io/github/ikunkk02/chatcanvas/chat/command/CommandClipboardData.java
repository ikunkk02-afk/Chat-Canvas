package io.github.ikunkk02.chatcanvas.chat.command;

import java.util.List;

public record CommandClipboardData(int version, List<SavedCommand> commands) {
	public static final int CURRENT_VERSION = 1;
	public static final CommandClipboardData EMPTY =
			new CommandClipboardData(CURRENT_VERSION, List.of());

	public CommandClipboardData {
		version = Math.max(1, version);
		commands = commands == null ? List.of() : List.copyOf(commands);
	}
}
