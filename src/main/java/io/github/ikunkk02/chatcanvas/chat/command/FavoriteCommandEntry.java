package io.github.ikunkk02.chatcanvas.chat.command;

import java.util.UUID;

public record FavoriteCommandEntry(
		UUID entryId,
		String name,
		String command,
		long createdAt,
		long updatedAt,
		int sortOrder,
		String serverScope
) {
	public FavoriteCommandEntry {
		entryId = entryId == null ? UUID.randomUUID() : entryId;
		name = cleanName(name);
		command = CommandTextSanitizer.normalizeCommand(command);
		createdAt = Math.max(0L, createdAt);
		updatedAt = Math.max(createdAt, updatedAt);
		sortOrder = Math.max(0, sortOrder);
		serverScope = serverScope == null ? "" : serverScope.strip();
	}

	public static FavoriteCommandEntry create(
			String name, String command, int sortOrder, long now) {
		String normalized = CommandTextSanitizer.normalizeCommand(command);
		return new FavoriteCommandEntry(
				UUID.randomUUID(), defaultName(name, normalized), normalized,
				now, now, sortOrder, "");
	}

	public boolean valid() {
		return !name.isBlank() && command.length() > 1;
	}

	public FavoriteCommandEntry edited(String newName, String newCommand, long now) {
		String normalized = CommandTextSanitizer.normalizeCommand(newCommand);
		return new FavoriteCommandEntry(
				entryId, defaultName(newName, normalized), normalized,
				createdAt, now, sortOrder, serverScope);
	}

	public FavoriteCommandEntry withSortOrder(int value) {
		return new FavoriteCommandEntry(
				entryId, name, command, createdAt, updatedAt, value, serverScope);
	}

	private static String defaultName(String value, String command) {
		String cleaned = cleanName(value);
		return cleaned.isBlank() ? CommandTextSanitizer.commandName(command) : cleaned;
	}

	private static String cleanName(String value) {
		return CommandTextSanitizer.normalize(value);
	}
}
