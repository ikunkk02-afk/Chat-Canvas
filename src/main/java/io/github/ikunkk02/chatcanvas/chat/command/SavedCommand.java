package io.github.ikunkk02.chatcanvas.chat.command;

import java.util.UUID;

public record SavedCommand(
		UUID id,
		String title,
		String command,
		String category,
		boolean favorite,
		long createdAt,
		long updatedAt,
		long lastUsedAt,
		int useCount,
		int sortOrder
) {
	public static final String UNCATEGORIZED = "";

	public SavedCommand {
		id = id == null ? UUID.randomUUID() : id;
		title = clean(title);
		command = command == null ? "" : command.strip();
		category = clean(category);
		createdAt = Math.max(0L, createdAt);
		updatedAt = Math.max(createdAt, updatedAt);
		lastUsedAt = Math.max(0L, lastUsedAt);
		useCount = Math.max(0, useCount);
	}

	public static SavedCommand create(String title, String command, String category,
									  int sortOrder, long now) {
		return new SavedCommand(UUID.randomUUID(), title, command, category, false,
				now, now, 0L, 0, sortOrder);
	}

	public boolean valid() {
		return !title.isBlank() && command.startsWith("/");
	}

	public SavedCommand edited(String newTitle, String newCommand, String newCategory, long now) {
		return new SavedCommand(id, newTitle, newCommand, newCategory, favorite, createdAt,
				now, lastUsedAt, useCount, sortOrder);
	}

	public SavedCommand withFavorite(boolean value, long now) {
		return new SavedCommand(id, title, command, category, value, createdAt,
				now, lastUsedAt, useCount, sortOrder);
	}

	public SavedCommand used(long now) {
		return new SavedCommand(id, title, command, category, favorite, createdAt,
				updatedAt, now, useCount + 1, sortOrder);
	}

	public SavedCommand withSortOrder(int value) {
		return new SavedCommand(id, title, command, category, favorite, createdAt,
				updatedAt, lastUsedAt, useCount, value);
	}

	private static String clean(String value) {
		return value == null ? "" : value.replace('\r', ' ').replace('\n', ' ').strip();
	}
}
