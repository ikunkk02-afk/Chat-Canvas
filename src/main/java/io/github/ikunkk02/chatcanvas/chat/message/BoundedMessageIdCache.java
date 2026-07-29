package io.github.ikunkk02.chatcanvas.chat.message;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class BoundedMessageIdCache {
	public static final int DEFAULT_CAPACITY = 512;
	public static final long DEFAULT_TTL_MS = 10 * 60 * 1_000L;
	private final int capacity;
	private final long ttlMs;
	private final LinkedHashMap<UUID, Long> ids = new LinkedHashMap<>();

	public BoundedMessageIdCache() {
		this(DEFAULT_CAPACITY, DEFAULT_TTL_MS);
	}

	public BoundedMessageIdCache(int capacity, long ttlMs) {
		this.capacity = Math.max(1, capacity);
		this.ttlMs = Math.max(1L, ttlMs);
	}

	public synchronized boolean accept(UUID id, long nowMs) {
		if (id == null) return false;
		ids.entrySet().removeIf(entry -> nowMs - entry.getValue() > ttlMs);
		if (ids.containsKey(id)) return false;
		ids.put(id, nowMs);
		Iterator<Map.Entry<UUID, Long>> iterator = ids.entrySet().iterator();
		while (ids.size() > capacity && iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
		return true;
	}

	public synchronized void clear() {
		ids.clear();
	}
}
