package io.github.ikunkk02.chatcanvas.chat.message;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import net.minecraft.network.chat.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ChatChannelHistoryTest {
	@Test
	void capacityAndDuplicateIdsAreIndependentFromText() {
		ChatChannelHistory history = new ChatChannelHistory(2);
		UUID first = UUID.randomUUID();
		assertTrue(history.add(message(first, "same", 1)));
		assertFalse(history.add(message(first, "changed", 2)));
		assertTrue(history.add(message(UUID.randomUUID(), "same", 3)));
		assertTrue(history.add(message(UUID.randomUUID(), "same", 4)));
		assertEquals(2, history.messages().size());
		assertEquals("same", history.messages().getFirst().content().getString());
	}

	@Test
	void scrollingAndClearingAreLocalState() {
		ChatChannelHistory history = new ChatChannelHistory(5);
		history.scrollBy(30, 100);
		assertEquals(30.0, history.scrollOffsetPixels());
		assertFalse(history.lockedToBottom());
		history.add(message(UUID.randomUUID(), "new", 1));
		assertTrue(history.unread());
		history.setScrollOffset(0, 100);
		assertTrue(history.lockedToBottom());
		assertFalse(history.unread());
		history.clear();
		assertTrue(history.messages().isEmpty());
		assertEquals(0.0, history.scrollOffsetPixels());
	}

	private static ChatCanvasMessage message(UUID id, String text, long time) {
		return new ChatCanvasMessage(id, ChatCanvasChannel.PLAYER_CHAT,
				ChatCanvasMessageSource.PLAYER, null, null,
				Component.literal(text), time, false, false);
	}
}
