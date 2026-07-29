package io.github.ikunkk02.chatcanvas.chat.input;

import java.util.List;

import io.github.ikunkk02.chatcanvas.chat.text.UnicodeTextNavigator;

public final class ChatCanvasInputController {
	private static final ChatCanvasInputController INSTANCE = new ChatCanvasInputController();

	private final PlayerChatInputState playerChatState = new PlayerChatInputState();
	private final CommandInputState commandState = new CommandInputState();
	private final ChatInputHistory playerHistory = new ChatInputHistory();
	private final ChatInputHistory commandHistory = new ChatInputHistory();
	private ChatCanvasInputMode currentMode = ChatCanvasInputMode.PLAYER_CHAT;

	private ChatCanvasInputController() {
		syncHistoryIndexes();
	}

	public static ChatCanvasInputController instance() {
		return INSTANCE;
	}

	public synchronized ChatCanvasInputMode open(
			ChatCanvasInputMode mode, String initialText) {
		currentMode = mode == null ? ChatCanvasInputMode.PLAYER_CHAT : mode;
		String initial = initialText == null ? "" : initialText;
		if (currentMode == ChatCanvasInputMode.COMMAND) {
			if (!initial.isBlank() && !initial.equals("/")) {
				commandState.restore(ChatInputSnapshot.atEnd(ensureSlash(initial)));
			} else if (commandState.command().isEmpty()) {
				commandState.restore(ChatInputSnapshot.atEnd("/"));
			}
		} else if (!initial.isEmpty()) {
			playerChatState.restore(ChatInputSnapshot.atEnd(initial));
		}
		syncHistoryIndexes();
		return currentMode;
	}

	public synchronized void capture(
			ChatCanvasInputMode mode, ChatInputSnapshot snapshot) {
		if (mode == ChatCanvasInputMode.COMMAND) {
			commandState.restore(snapshot);
		} else {
			playerChatState.restore(snapshot);
		}
	}

	public synchronized void switchPlayerTextToCommand(ChatInputSnapshot commandInput) {
		commandState.restore(new ChatInputSnapshot(
				ensureSlash(commandInput.text()),
				commandInput.cursor(),
				commandInput.selectionEnd()));
		currentMode = ChatCanvasInputMode.COMMAND;
		commandHistory.resetNavigation();
		syncHistoryIndexes();
	}

	public synchronized void switchToPlayerChat() {
		currentMode = ChatCanvasInputMode.PLAYER_CHAT;
		playerHistory.resetNavigation();
		syncHistoryIndexes();
	}

	public synchronized ChatInputSnapshot navigateHistory(
			ChatCanvasInputMode mode, int offset, ChatInputSnapshot current) {
		ChatInputSnapshot result;
		if (mode == ChatCanvasInputMode.COMMAND) {
			result = commandHistory.navigate(offset, current);
			commandState.restore(result);
		} else {
			result = playerHistory.navigate(offset, current);
			playerChatState.restore(result);
		}
		syncHistoryIndexes();
		return result;
	}

	public synchronized void recordSentPlayerChat(String message) {
		if (message == null || message.isBlank() || message.startsWith("/")) return;
		playerHistory.add(message);
		playerChatState.restore(ChatInputSnapshot.EMPTY);
		syncHistoryIndexes();
	}

	public synchronized void recordExecutedCommand(String command) {
		if (command == null || command.isBlank()) return;
		String normalized = ensureSlash(command);
		if (normalized.length() <= 1) return;
		commandHistory.add(normalized);
		commandState.restore(ChatInputSnapshot.atEnd("/"));
		syncHistoryIndexes();
	}

	public synchronized ChatCanvasInputMode currentMode() {
		return currentMode;
	}

	public synchronized ChatInputSnapshot snapshot(ChatCanvasInputMode mode) {
		return mode == ChatCanvasInputMode.COMMAND
				? commandState.snapshot() : playerChatState.snapshot();
	}

	public synchronized PlayerChatInputState playerChatState() {
		return playerChatState;
	}

	public synchronized UnicodeTextNavigator.EditResult insertPlayerText(
			String value, int maxUtf16Length) {
		return playerChatState.replaceSelection(value, maxUtf16Length);
	}

	public synchronized CommandInputState commandState() {
		return commandState;
	}

	public synchronized List<String> playerHistory() {
		return playerHistory.entries();
	}

	public synchronized List<String> commandHistory() {
		return commandHistory.entries();
	}

	public synchronized void clearSession() {
		currentMode = ChatCanvasInputMode.PLAYER_CHAT;
		playerChatState.restore(ChatInputSnapshot.EMPTY);
		commandState.restore(ChatInputSnapshot.atEnd("/"));
		playerHistory.clear();
		commandHistory.clear();
		syncHistoryIndexes();
	}

	private void syncHistoryIndexes() {
		playerChatState.historyIndex(playerHistory.index());
		commandState.historyIndex(commandHistory.index());
	}

	private static String ensureSlash(String command) {
		String safe = command == null ? "" : command;
		return safe.startsWith("/") ? safe : "/" + safe;
	}
}
