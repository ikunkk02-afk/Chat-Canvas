package io.github.ikunkk02.chatcanvas.chat.input;

import io.github.ikunkk02.chatcanvas.chat.text.UnicodeTextNavigator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

public final class ChatCanvasInputSender {
	private ChatCanvasInputSender() {
	}

	public static boolean sendPlayerChat(
			Minecraft client, ChatScreen screen, String input) {
		String message = screen.normalizeChatMessage(
				UnicodeTextNavigator.truncateAtGraphemeBoundary(input, 256));
		if (message.isEmpty() || message.startsWith("/") || client.player == null) {
			return false;
		}
		client.gui.getChat().addRecentChat(message);
		client.player.connection.sendChat(message);
		return true;
	}

	public static boolean executeCommand(
			Minecraft client, ChatScreen screen, String input) {
		String normalized = screen.normalizeChatMessage(
				UnicodeTextNavigator.truncateAtGraphemeBoundary(input, 256));
		if (client.player == null) return false;
		String command = normalized.startsWith("/")
				? normalized.substring(1) : normalized;
		if (command.isBlank()) return false;
		String historyEntry = "/" + command;
		client.gui.getChat().addRecentChat(historyEntry);
		ChatCanvasInputController.instance().recordExecutedCommand(command);
		io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress.instance()
				.acceptCommand(command);
		io.github.ikunkk02.chatcanvas.chat.command.CommandToolRuntime.recordExecuted(command);
		client.player.connection.sendCommand(command);
		return true;
	}
}
