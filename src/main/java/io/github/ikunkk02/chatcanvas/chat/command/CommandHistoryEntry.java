package io.github.ikunkk02.chatcanvas.chat.command;

import java.util.UUID;

public record CommandHistoryEntry(
		UUID entryId,
		String command,
		long executedAt,
		String serverIdentifier
) {
	public CommandHistoryEntry {
		entryId = entryId == null ? UUID.randomUUID() : entryId;
		command = CommandTextSanitizer.normalizeCommand(command);
		executedAt = Math.max(0L, executedAt);
		serverIdentifier = cleanIdentifier(serverIdentifier);
	}

	public static CommandHistoryEntry create(
			String command, long executedAt, String serverIdentifier) {
		return new CommandHistoryEntry(
				UUID.randomUUID(), command, executedAt, serverIdentifier);
	}

	public boolean valid() {
		return command.length() > 1;
	}

	private static String cleanIdentifier(String value) {
		if (value == null || value.isBlank()) return "unknown";
		StringBuilder result = new StringBuilder();
		for (int offset = 0; offset < value.length() && result.length() < 80;) {
			int codePoint = value.codePointAt(offset);
			offset += Character.charCount(codePoint);
			if (!Character.isISOControl(codePoint)) result.appendCodePoint(codePoint);
		}
		return result.toString().strip();
	}
}
