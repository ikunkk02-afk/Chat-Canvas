package io.github.ikunkk02.chatcanvas.chat.notification;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class MentionNotificationDeduplicator {
	public static final int DEFAULT_CAPACITY = 2_048;
	public static final long DEFAULT_TTL_MS = 10 * 60 * 1_000L;
	private final int capacity;
	private final long ttlMs;
	private final LinkedHashMap<UUID, Long> ids = new LinkedHashMap<>();
	private final LinkedHashMap<String, Long> fingerprints = new LinkedHashMap<>();

	public MentionNotificationDeduplicator() {
		this(DEFAULT_CAPACITY, DEFAULT_TTL_MS);
	}

	public MentionNotificationDeduplicator(int capacity, long ttlMs) {
		this.capacity = Math.max(1, capacity);
		this.ttlMs = Math.max(1L, ttlMs);
	}

	public synchronized boolean accept(UUID id, String fingerprint, long nowMs) {
		prune(nowMs);
		if (ids.containsKey(id)) return false;
		if (fingerprint != null && !fingerprint.isBlank()) {
			Long previous = fingerprints.get(fingerprint);
			if (previous != null && nowMs - previous <= 250L) return false;
		}
		ids.put(id, nowMs);
		if (fingerprint != null && !fingerprint.isBlank()) fingerprints.put(fingerprint, nowMs);
		trim(ids);
		trim(fingerprints);
		return true;
	}

	public synchronized void clear() {
		ids.clear();
		fingerprints.clear();
	}

	private void prune(long nowMs) {
		ids.entrySet().removeIf(entry -> nowMs - entry.getValue() > ttlMs);
		fingerprints.entrySet().removeIf(entry -> nowMs - entry.getValue() > ttlMs);
	}

	private <K> void trim(LinkedHashMap<K, Long> map) {
		Iterator<Map.Entry<K, Long>> iterator = map.entrySet().iterator();
		while (map.size() > capacity && iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}
}
