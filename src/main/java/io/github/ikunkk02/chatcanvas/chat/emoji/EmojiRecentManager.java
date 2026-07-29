package io.github.ikunkk02.chatcanvas.chat.emoji;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class EmojiRecentManager {
	public static final int MAX_RECENT = 24;

	private final EmojiRecentStorage storage;
	private final Clock clock;
	private final List<RecentEmojiEntry> recent = new ArrayList<>();
	private EmojiRecentStorage.LoadResult loadResult;

	public EmojiRecentManager(EmojiRecentStorage storage) {
		this(storage, Clock.systemUTC());
	}

	EmojiRecentManager(EmojiRecentStorage storage, Clock clock) {
		this.storage = storage;
		this.clock = clock;
		loadResult = storage.load();
		recent.addAll(loadResult.data().recent());
	}

	public synchronized boolean recordSelected(String unicode) {
		if (!EmojiRegistry.instance().contains(unicode)) return false;
		long newest = recent.stream().mapToLong(
				RecentEmojiEntry::lastUsedAt).max().orElse(0L);
		long now = Math.max(Math.max(1L, clock.millis()), newest + 1L);
		int count = 1;
		for (int index = 0; index < recent.size(); index++) {
			RecentEmojiEntry entry = recent.get(index);
			if (!entry.unicode().equals(unicode)) continue;
			count = entry.useCount() + 1;
			recent.remove(index);
			break;
		}
		recent.add(new RecentEmojiEntry(unicode, now, count));
		recent.sort(Comparator.comparingLong(
				RecentEmojiEntry::lastUsedAt).reversed());
		while (recent.size() > MAX_RECENT) recent.remove(recent.size() - 1);
		return storage.save(data());
	}

	public synchronized List<RecentEmojiEntry> recent() {
		return List.copyOf(recent);
	}

	public synchronized List<EmojiEntry> entries() {
		return recent.stream()
				.map(entry -> EmojiRegistry.instance().find(entry.unicode()))
				.filter(java.util.Objects::nonNull)
				.toList();
	}

	public synchronized EmojiRecentData data() {
		return new EmojiRecentData(EmojiRecentData.CURRENT_VERSION, recent);
	}

	public EmojiRecentStorage.LoadResult loadResult() {
		return loadResult;
	}

	public synchronized boolean flush() {
		return storage.save(data());
	}
}
