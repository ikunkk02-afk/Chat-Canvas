package io.github.ikunkk02.chatcanvas.chat.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MentionMessageIdRegistryTest {
	@Test
	void sameInstanceKeepsItsIdButEqualInstancesRemainDistinct() {
		MentionMessageIdRegistry<String> registry = new MentionMessageIdRegistry<>(4);
		String first = new String("@Steve");
		String identicalMessage = new String("@Steve");

		assertEquals(registry.idFor(first), registry.idFor(first));
		assertNotEquals(registry.idFor(first), registry.idFor(identicalMessage));
	}

	@Test
	void capacityAndClearReleaseOldInstances() {
		MentionMessageIdRegistry<Object> registry = new MentionMessageIdRegistry<>(2);
		Object first = new Object();
		registry.idFor(first);
		registry.idFor(new Object());
		registry.idFor(new Object());
		assertEquals(2, registry.size());

		registry.clear();
		assertEquals(0, registry.size());
	}

	@Test
	void tenIdenticalMessagesAreIndependentButDuplicateDispatchIsNot() {
		MentionMessageIdRegistry<String> registry = new MentionMessageIdRegistry<>(32);
		MentionNotificationDeduplicator deduplicator =
				new MentionNotificationDeduplicator(32, 10_000);

		for (int index = 0; index < 10; index++) {
			String receivedMessage = new String("Alex: @Steve");
			var messageId = registry.idFor(receivedMessage);
			assertTrue(deduplicator.accept(messageId, 1_000 + index));
			assertFalse(deduplicator.accept(registry.idFor(receivedMessage), 1_000 + index));
		}
	}
}
