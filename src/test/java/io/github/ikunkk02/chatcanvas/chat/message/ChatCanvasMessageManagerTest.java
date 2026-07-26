package io.github.ikunkk02.chatcanvas.chat.message;

import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatCanvasMessageManagerTest {
	@Test
	void channelsNeverShareHistoryOrScrollState() {
		ChatCanvasMessageManager manager = new ChatCanvasMessageManager(5, 5);
		manager.add(message(ChatCanvasChannel.PLAYER_CHAT));
		manager.add(message(ChatCanvasChannel.COMMAND_SYSTEM));
		manager.playerChat().scrollBy(20, 100);

		assertEquals(1, manager.playerChat().messages().size());
		assertEquals(1, manager.commandSystem().messages().size());
		assertEquals(20.0, manager.playerChat().scrollOffsetPixels());
		assertEquals(0.0, manager.commandSystem().scrollOffsetPixels());

		manager.playerChat().clear();
		assertTrue(manager.playerChat().messages().isEmpty());
		assertEquals(1, manager.commandSystem().messages().size());
	}

	private static ChatCanvasMessage message(ChatCanvasChannel channel) {
		return new ChatCanvasMessage(UUID.randomUUID(), channel,
				channel == ChatCanvasChannel.PLAYER_CHAT
						? ChatCanvasMessageSource.PLAYER : ChatCanvasMessageSource.SYSTEM,
				null, null, Text.literal(channel.name()), System.currentTimeMillis(),
				false, false);
	}
}
