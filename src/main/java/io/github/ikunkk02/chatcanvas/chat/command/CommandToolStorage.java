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

public final class CommandToolStorage {
	public enum LoadStatus {
		OK,
		CREATED_EMPTY,
		PARTIAL_RECOVERY,
		RECOVERED_CORRUPT
	}

	public record LoadResult(
			CommandToolData data, LoadStatus status, Path backupPath) {
	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final Path path;
	private final Clock clock;

	public CommandToolStorage(Path path) {
		this(path, Clock.systemUTC());
	}

	CommandToolStorage(Path path, Clock clock) {
		this.path = path;
		this.clock = clock;
	}

	public LoadResult load() {
		if (Files.notExists(path)) {
			return new LoadResult(CommandToolData.EMPTY, LoadStatus.CREATED_EMPTY, null);
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			JsonElement parsed = JsonParser.parseReader(reader);
			if (!parsed.isJsonObject()) throw new IllegalArgumentException("root");
			JsonObject root = parsed.getAsJsonObject();
			JsonArray recentJson = array(root, "recent");
			JsonArray favoritesJson = array(root, "favorites");
			List<CommandHistoryEntry> recent = new ArrayList<>();
			List<FavoriteCommandEntry> favorites = new ArrayList<>();
			int malformed = 0;
			for (JsonElement entry : recentJson) {
				try {
					CommandHistoryEntry parsedEntry = parseRecent(entry);
					if (parsedEntry.valid()) recent.add(parsedEntry);
					else malformed++;
				} catch (RuntimeException ignored) {
					malformed++;
				}
			}
			for (JsonElement entry : favoritesJson) {
				try {
					FavoriteCommandEntry parsedEntry = parseFavorite(entry);
					if (parsedEntry.valid()) favorites.add(parsedEntry);
					else malformed++;
				} catch (RuntimeException ignored) {
					malformed++;
				}
			}
			recent.sort(Comparator.comparingLong(
					CommandHistoryEntry::executedAt).reversed());
			favorites.sort(Comparator.comparingInt(FavoriteCommandEntry::sortOrder));
			CommandToolData data = new CommandToolData(
					CommandToolData.CURRENT_VERSION,
					bool(root, "migrationCompleted", false),
					recent, favorites);
			if (malformed > 0) save(data);
			return new LoadResult(data,
					malformed > 0 ? LoadStatus.PARTIAL_RECOVERY : LoadStatus.OK,
					null);
		} catch (IOException | RuntimeException failure) {
			Path backup = backupCorruptFile();
			save(CommandToolData.EMPTY);
			return new LoadResult(
					CommandToolData.EMPTY, LoadStatus.RECOVERED_CORRUPT, backup);
		}
	}

	public synchronized boolean save(CommandToolData data) {
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

	private Path backupCorruptFile() {
		if (Files.notExists(path)) return null;
		Path backup = path.resolveSibling("commands.corrupt-"
				+ clock.millis() + ".json");
		try {
			Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
			return backup;
		} catch (IOException ignored) {
			return null;
		}
	}

	private static CommandHistoryEntry parseRecent(JsonElement value) {
		JsonObject entry = object(value);
		return new CommandHistoryEntry(
				UUID.fromString(requiredString(entry, "entryId")),
				requiredString(entry, "command"),
				longValue(entry, "executedAt", 0L),
				string(entry, "serverIdentifier", "unknown"));
	}

	private static FavoriteCommandEntry parseFavorite(JsonElement value) {
		JsonObject entry = object(value);
		return new FavoriteCommandEntry(
				UUID.fromString(requiredString(entry, "entryId")),
				requiredString(entry, "name"),
				requiredString(entry, "command"),
				longValue(entry, "createdAt", 0L),
				longValue(entry, "updatedAt", 0L),
				integer(entry, "sortOrder", 0),
				string(entry, "serverScope", ""));
	}

	private static JsonObject toJson(CommandToolData data) {
		JsonObject root = new JsonObject();
		root.addProperty("version", CommandToolData.CURRENT_VERSION);
		root.addProperty("migrationCompleted", data.migrationCompleted());
		root.add("recent", GSON.toJsonTree(data.recent()));
		root.add("favorites", GSON.toJsonTree(data.favorites()));
		return root;
	}

	private static JsonObject object(JsonElement value) {
		if (value == null || !value.isJsonObject()) {
			throw new IllegalArgumentException("entry");
		}
		return value.getAsJsonObject();
	}

	private static JsonArray array(JsonObject root, String key) {
		return root.has(key) && root.get(key).isJsonArray()
				? root.getAsJsonArray(key) : new JsonArray();
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
