package io.github.ikunkk02.chatcanvas.chat.notification;

import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.chat.Component;

public final class MentionToastManager {
	private static final SystemToast.Type TYPE = new SystemToast.Type(5_000L);

	public void show(MentionNotificationEvent event, MentionConfig config) {
		Minecraft client = Minecraft.getInstance();
		if (!config.toastEnabled()) return;
		if (!config.toastWhenChatOpen() && client.screen instanceof ChatScreen) return;
		String sender = event.sender() == null || event.sender().playerName().isBlank()
				? Component.translatable("chat_canvas.notification.unknown_sender").getString()
				: event.sender().playerName();
		String preview = truncate(event.plainPreview(), config.toastMessageLength());
		SystemToast.add(
				client.getToastManager(),
				TYPE,
				Component.translatable("chat_canvas.notification.mentioned"),
				Component.translatable("chat_canvas.notification.preview", sender, preview));
	}

	static String truncate(String value, int maxCodePoints) {
		if (value == null) return "";
		String singleLine = value.replaceAll("[\\p{Cc}\\p{Cf}]+", " ")
				.replaceAll("\\s+", " ").trim();
		int count = singleLine.codePointCount(0, singleLine.length());
		if (count <= maxCodePoints) return singleLine;
		int end = singleLine.offsetByCodePoints(0, Math.max(0, maxCodePoints));
		return singleLine.substring(0, end).stripTrailing() + "…";
	}
}
