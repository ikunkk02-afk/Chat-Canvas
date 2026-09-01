package io.github.ikunkk02.chatcanvas.chat.message;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import net.minecraft.network.chat.Component;

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

	@Test
	void layoutInvalidationCanTargetOnlyPlayerHistory() {
		ChatCanvasMessageManager manager = new ChatCanvasMessageManager(5, 5);
		long playerBefore = manager.playerChat().layoutEpoch();
		long commandBefore = manager.commandSystem().layoutEpoch();
		manager.invalidateLayout(ChatCanvasChannel.PLAYER_CHAT);
		assertEquals(playerBefore + 1, manager.playerChat().layoutEpoch());
		assertEquals(commandBefore, manager.commandSystem().layoutEpoch());
	}

	private static ChatCanvasMessage message(ChatCanvasChannel channel) {
		return new ChatCanvasMessage(UUID.randomUUID(), channel,
				channel == ChatCanvasChannel.PLAYER_CHAT
						? ChatCanvasMessageSource.PLAYER : ChatCanvasMessageSource.SYSTEM,
				null, null, Component.literal(channel.name()), System.currentTimeMillis(),
				false, false);
	}
}
