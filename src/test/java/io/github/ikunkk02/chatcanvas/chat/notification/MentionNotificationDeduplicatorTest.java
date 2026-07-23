package io.github.ikunkk02.chatcanvas.chat.notification;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MentionNotificationDeduplicatorTest {
	@Test
	void rejectsRepeatedIdsAndShortWindowFingerprints() {
		MentionNotificationDeduplicator deduplicator =
				new MentionNotificationDeduplicator(4, 1_000);
		UUID first = UUID.randomUUID();
		assertTrue(deduplicator.accept(first, "same-event", 1_000));
		assertFalse(deduplicator.accept(first, "different", 1_001));
		assertFalse(deduplicator.accept(UUID.randomUUID(), "same-event", 1_200));
		assertTrue(deduplicator.accept(UUID.randomUUID(), "same-event", 1_251));
	}

	@Test
	void expiresOldEntries() {
		MentionNotificationDeduplicator deduplicator =
				new MentionNotificationDeduplicator(4, 100);
		UUID id = UUID.randomUUID();
		assertTrue(deduplicator.accept(id, "value", 10));
		assertTrue(deduplicator.accept(id, "value", 111));
	}
}
