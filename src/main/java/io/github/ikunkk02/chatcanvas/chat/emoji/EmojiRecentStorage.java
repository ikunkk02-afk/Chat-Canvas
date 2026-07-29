package io.github.ikunkk02.chatcanvas.chat.emoji;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EmojiRecentStorage {
	public enum LoadStatus {
		OK,
		CREATED_EMPTY,
		PARTIAL_RECOVERY,
		RECOVERED_CORRUPT
	}

	public record LoadResult(
			EmojiRecentData data,
			LoadStatus status,
			Path backupPath,
			Throwable failure
	) {
	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final Path path;
	private final Clock clock;

	public EmojiRecentStorage(Path path) {
		this(path, Clock.systemUTC());
	}

	EmojiRecentStorage(Path path, Clock clock) {
		this.path = path;
		this.clock = clock;
	}

	public LoadResult load() {
		if (Files.notExists(path)) {
			return new LoadResult(
					EmojiRecentData.EMPTY, LoadStatus.CREATED_EMPTY, null, null);
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			JsonElement parsed = JsonParser.parseReader(reader);
			if (!parsed.isJsonObject()) throw new IllegalArgumentException("root");
			JsonObject root = parsed.getAsJsonObject();
			if (!root.has("version") || root.get("version").getAsInt() < 1) {
				throw new IllegalArgumentException("version");
			}
			if (!root.has("recent") || !root.get("recent").isJsonArray()) {
				throw new IllegalArgumentException("recent");
			}
			Map<String, RecentEmojiEntry> unique = new LinkedHashMap<>();
			int malformed = 0;
			for (JsonElement value : root.getAsJsonArray("recent")) {
				try {
					RecentEmojiEntry entry = GSON.fromJson(value, RecentEmojiEntry.class);
					if (entry == null || !entry.valid()
							|| !EmojiRegistry.instance().contains(entry.unicode())) {
						malformed++;
						continue;
					}
					RecentEmojiEntry existing = unique.get(entry.unicode());
					if (existing == null || entry.lastUsedAt() > existing.lastUsedAt()) {
						unique.put(entry.unicode(), entry);
					}
				} catch (RuntimeException ignored) {
					malformed++;
				}
			}
			List<RecentEmojiEntry> recent = new ArrayList<>(unique.values());
			recent.sort(Comparator.comparingLong(
					RecentEmojiEntry::lastUsedAt).reversed());
			if (recent.size() > EmojiRecentManager.MAX_RECENT) {
				recent = new ArrayList<>(recent.subList(0, EmojiRecentManager.MAX_RECENT));
				malformed++;
			}
			EmojiRecentData data = new EmojiRecentData(
					EmojiRecentData.CURRENT_VERSION, recent);
			if (malformed > 0) save(data);
			return new LoadResult(data, malformed > 0
					? LoadStatus.PARTIAL_RECOVERY : LoadStatus.OK, null, null);
		} catch (IOException | RuntimeException failure) {
			Path backup = backupCorruptFile();
			save(EmojiRecentData.EMPTY);
			return new LoadResult(
					EmojiRecentData.EMPTY, LoadStatus.RECOVERED_CORRUPT,
					backup, failure);
		}
	}

	public synchronized boolean save(EmojiRecentData data) {
		Path parent = path.getParent();
		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			if (parent != null) Files.createDirectories(parent);
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(data, writer);
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
		Path backup = path.resolveSibling(
				"emoji.corrupt-" + clock.millis() + ".json");
		try {
			Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
			return backup;
		} catch (IOException ignored) {
			return null;
		}
	}
}
