package io.github.ikunkk02.chatcanvas.chat.notification;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MentionNotificationDeduplicatorTest {
	@Test
	void rejectsOnlyTheSameMessageId() {
		MentionNotificationDeduplicator deduplicator =
				new MentionNotificationDeduplicator(4, 1_000);
		UUID first = UUID.randomUUID();
		assertTrue(deduplicator.accept(first, 1_000));
		assertFalse(deduplicator.accept(first, 1_001));
		assertTrue(deduplicator.accept(UUID.randomUUID(), 1_001));
		assertTrue(deduplicator.accept(UUID.randomUUID(), 1_001));
	}

	@Test
	void expiresOldEntries() {
		MentionNotificationDeduplicator deduplicator =
				new MentionNotificationDeduplicator(4, 100);
		UUID id = UUID.randomUUID();
		assertTrue(deduplicator.accept(id, 10));
		assertTrue(deduplicator.accept(id, 111));
	}

	@Test
	void evictsOldestIdsAtCapacity() {
		MentionNotificationDeduplicator deduplicator =
				new MentionNotificationDeduplicator(2, 10_000);
		UUID first = UUID.randomUUID();
		assertTrue(deduplicator.accept(first, 1));
		assertTrue(deduplicator.accept(UUID.randomUUID(), 2));
		assertTrue(deduplicator.accept(UUID.randomUUID(), 3));
		assertTrue(deduplicator.accept(first, 4));
	}
}
