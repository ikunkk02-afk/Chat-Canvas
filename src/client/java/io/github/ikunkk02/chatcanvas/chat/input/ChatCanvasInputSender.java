package io.github.ikunkk02.chatcanvas.chat.input;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

public final class ChatCanvasInputSender {
	private ChatCanvasInputSender() {
	}

	public static boolean sendPlayerChat(
			MinecraftClient client, ChatScreen screen, String input) {
		String message = screen.normalize(input);
		if (message.isEmpty() || message.startsWith("/") || client.player == null) {
			return false;
		}
		client.inGameHud.getChatHud().addToMessageHistory(message);
		client.player.networkHandler.sendChatMessage(message);
		return true;
	}

	public static boolean executeCommand(
			MinecraftClient client, ChatScreen screen, String input) {
		String normalized = screen.normalize(input);
		if (client.player == null) return false;
		String command = normalized.startsWith("/")
				? normalized.substring(1) : normalized;
		if (command.isBlank()) return false;
		String historyEntry = "/" + command;
		client.inGameHud.getChatHud().addToMessageHistory(historyEntry);
		client.player.networkHandler.sendChatCommand(command);
		return true;
	}
}
