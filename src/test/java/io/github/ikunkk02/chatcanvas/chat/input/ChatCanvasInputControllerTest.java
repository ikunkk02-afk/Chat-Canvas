package io.github.ikunkk02.chatcanvas.chat.input;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class ChatCanvasInputControllerTest {
	private ChatCanvasInputController controller;

	@BeforeEach
	void reset() {
		controller = ChatCanvasInputController.instance();
		controller.clearSession();
	}

	@Test
	void playerAndCommandDraftsRemainIndependent() {
		controller.capture(ChatCanvasInputMode.PLAYER_CHAT,
				ChatInputSnapshot.atEnd("等我一下"));
		controller.open(ChatCanvasInputMode.COMMAND, "/");
		controller.capture(ChatCanvasInputMode.COMMAND,
				ChatInputSnapshot.atEnd("/time set day"));

		assertEquals("等我一下",
				controller.snapshot(ChatCanvasInputMode.PLAYER_CHAT).text());
		assertEquals("/time set day",
				controller.snapshot(ChatCanvasInputMode.COMMAND).text());
	}

	@Test
	void slashSwitchTransfersCommandWithoutPollutingPlayerDraft() {
		controller.capture(ChatCanvasInputMode.PLAYER_CHAT,
				ChatInputSnapshot.atEnd("保留的草稿"));
		controller.switchPlayerTextToCommand(
				ChatInputSnapshot.atEnd("/gamemode creative"));

		assertEquals(ChatCanvasInputMode.COMMAND, controller.currentMode());
		assertEquals("保留的草稿",
				controller.snapshot(ChatCanvasInputMode.PLAYER_CHAT).text());
		assertEquals("/gamemode creative",
				controller.snapshot(ChatCanvasInputMode.COMMAND).text());
	}

	@Test
	void historiesAreSeparatedAndConsecutiveDuplicatesMatchVanilla() {
		controller.recordSentPlayerChat("hello");
		controller.recordSentPlayerChat("hello");
		controller.recordExecutedCommand("/time set day");
		controller.recordExecutedCommand("time set day");

		assertEquals(java.util.List.of("hello"), controller.playerHistory());
		assertEquals(java.util.List.of("/time set day"), controller.commandHistory());
	}

	@Test
	void eachHistoryRestoresItsOwnBrowsingDraft() {
		controller.recordSentPlayerChat("one");
		controller.recordExecutedCommand("/help");

		ChatInputSnapshot player = controller.navigateHistory(
				ChatCanvasInputMode.PLAYER_CHAT, -1,
				ChatInputSnapshot.atEnd("player draft"));
		ChatInputSnapshot command = controller.navigateHistory(
				ChatCanvasInputMode.COMMAND, -1,
				ChatInputSnapshot.atEnd("/command draft"));

		assertEquals("one", player.text());
		assertEquals("/help", command.text());
		assertEquals("player draft", controller.navigateHistory(
				ChatCanvasInputMode.PLAYER_CHAT, 1, player).text());
		assertEquals("/command draft", controller.navigateHistory(
				ChatCanvasInputMode.COMMAND, 1, command).text());
	}

	@Test
	void deletingCommandDoesNotSwitchMode() {
		controller.open(ChatCanvasInputMode.COMMAND, "/");
		controller.capture(ChatCanvasInputMode.COMMAND, ChatInputSnapshot.EMPTY);

		assertEquals(ChatCanvasInputMode.COMMAND, controller.currentMode());
	}

	@Test
	void clearingSessionDropsBothDraftsAndHistories() {
		controller.capture(ChatCanvasInputMode.PLAYER_CHAT, ChatInputSnapshot.atEnd("draft"));
		controller.capture(ChatCanvasInputMode.COMMAND, ChatInputSnapshot.atEnd("/draft"));
		controller.recordSentPlayerChat("sent");
		controller.recordExecutedCommand("/sent");

		controller.clearSession();

		assertEquals("", controller.snapshot(ChatCanvasInputMode.PLAYER_CHAT).text());
		assertEquals("/", controller.snapshot(ChatCanvasInputMode.COMMAND).text());
		assertEquals(java.util.List.of(), controller.playerHistory());
		assertEquals(java.util.List.of(), controller.commandHistory());
	}

	@Test
	void emojiInsertionUsesTheFormalPlayerDraftAndSelection() {
		controller.capture(ChatCanvasInputMode.PLAYER_CHAT,
				new ChatInputSnapshot("大家好", 2, 2));

		var inserted = controller.insertPlayerText("😀", 256);

		assertFalse(inserted.limitExceeded());
		assertEquals("大家😀好",
				controller.snapshot(ChatCanvasInputMode.PLAYER_CHAT).text());
		assertEquals(4,
				controller.snapshot(ChatCanvasInputMode.PLAYER_CHAT).cursor());
	}

	@Test
	void emojiInsertionReplacesSelectionWithoutTouchingCommandDraft() {
		controller.capture(ChatCanvasInputMode.PLAYER_CHAT,
				new ChatInputSnapshot("hello world", 5, 11));
		controller.capture(ChatCanvasInputMode.COMMAND,
				ChatInputSnapshot.atEnd("/say keep"));

		controller.insertPlayerText("❤️", 256);

		assertEquals("hello❤️",
				controller.snapshot(ChatCanvasInputMode.PLAYER_CHAT).text());
		assertEquals("/say keep",
				controller.snapshot(ChatCanvasInputMode.COMMAND).text());
	}
}
