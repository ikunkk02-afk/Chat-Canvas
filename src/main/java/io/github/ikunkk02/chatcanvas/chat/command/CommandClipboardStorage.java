package io.github.ikunkk02.chatcanvas.chat.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class CommandClipboardStorage {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final Path path;
	private final Clock clock;

	public CommandClipboardStorage(Path path) {
		this(path, Clock.systemUTC());
	}

	CommandClipboardStorage(Path path, Clock clock) {
		this.path = path;
		this.clock = clock;
	}

	public CommandClipboardData load() {
		if (Files.notExists(path)) {
			save(CommandClipboardData.EMPTY);
			return CommandClipboardData.EMPTY;
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			JsonElement parsed = JsonParser.parseReader(reader);
			if (!parsed.isJsonObject()) throw new IllegalArgumentException("root");
			JsonObject root = parsed.getAsJsonObject();
			int version = integer(root, "version", 1);
			JsonArray entries = root.has("commands") && root.get("commands").isJsonArray()
					? root.getAsJsonArray("commands") : new JsonArray();
			List<SavedCommand> commands = new ArrayList<>();
			for (JsonElement entry : entries) {
				try {
					SavedCommand command = parseEntry(entry);
					if (command.valid()) commands.add(command);
				} catch (RuntimeException ignored) {
					// A malformed command is isolated from the rest of the file.
				}
			}
			commands.sort(Comparator.comparingInt(SavedCommand::sortOrder));
			CommandClipboardData data = migrate(new CommandClipboardData(version, commands));
			if (version != CommandClipboardData.CURRENT_VERSION
					|| commands.size() != entries.size()) save(data);
			return data;
		} catch (IOException | RuntimeException failure) {
			backupCorruptFile();
			save(CommandClipboardData.EMPTY);
			return CommandClipboardData.EMPTY;
		}
	}

	public synchronized boolean save(CommandClipboardData data) {
		Path parent = path.getParent();
		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			if (parent != null) Files.createDirectories(parent);
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(toJson(data), writer);
			}
			try {
				Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ignored) {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
			}
			return true;
		} catch (IOException failure) {
			try {
				Files.deleteIfExists(temporary);
			} catch (IOException ignored) {
			}
			return false;
		}
	}

	public Path path() {
		return path;
	}

	private CommandClipboardData migrate(CommandClipboardData data) {
		return new CommandClipboardData(CommandClipboardData.CURRENT_VERSION, data.commands());
	}

	private void backupCorruptFile() {
		if (Files.notExists(path)) return;
		Path backup = path.resolveSibling("command_clipboard.corrupt-"
				+ clock.millis() + ".json");
		try {
			Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ignored) {
		}
	}

	private static SavedCommand parseEntry(JsonElement value) {
		if (!value.isJsonObject()) throw new IllegalArgumentException("entry");
		JsonObject entry = value.getAsJsonObject();
		SavedCommand command = new SavedCommand(
				UUID.fromString(requiredString(entry, "id")),
				requiredString(entry, "title"),
				requiredString(entry, "command"),
				string(entry, "category", ""),
				bool(entry, "favorite", false),
				longValue(entry, "createdAt", 0L),
				longValue(entry, "updatedAt", 0L),
				longValue(entry, "lastUsedAt", 0L),
				integer(entry, "useCount", 0),
				integer(entry, "sortOrder", 0));
		if (!command.valid()) throw new IllegalArgumentException("invalid command");
		return command;
	}

	private static JsonObject toJson(CommandClipboardData data) {
		JsonObject root = new JsonObject();
		root.addProperty("version", CommandClipboardData.CURRENT_VERSION);
		JsonArray commands = new JsonArray();
		for (SavedCommand command : data.commands()) commands.add(GSON.toJsonTree(command));
		root.add("commands", commands);
		return root;
	}

	private static String requiredString(JsonObject object, String key) {
		if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
			throw new IllegalArgumentException(key);
		}
		return object.get(key).getAsString();
	}

	private static String string(JsonObject object, String key, String fallback) {
		try {
			return object.has(key) ? object.get(key).getAsString() : fallback;
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static int integer(JsonObject object, String key, int fallback) {
		try {
			return object.has(key) ? object.get(key).getAsInt() : fallback;
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static long longValue(JsonObject object, String key, long fallback) {
		try {
			return object.has(key) ? object.get(key).getAsLong() : fallback;
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static boolean bool(JsonObject object, String key, boolean fallback) {
		try {
			return object.has(key) ? object.get(key).getAsBoolean() : fallback;
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}
}
