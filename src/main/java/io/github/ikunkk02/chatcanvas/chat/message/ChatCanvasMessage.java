package io.github.ikunkk02.chatcanvas.chat.message;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ChatCanvasMessage(
		UUID messageId,
		ChatCanvasChannel channel,
		ChatCanvasMessageSource source,
		@Nullable UUID senderUuid,
		@Nullable Text senderName,
		Text content,
		long receivedAt,
		boolean selfMessage,
		boolean mentionedCurrentPlayer
) {
	public ChatCanvasMessage {
		if (messageId == null) throw new IllegalArgumentException("messageId");
		if (channel == null) throw new IllegalArgumentException("channel");
		if (source == null) source = ChatCanvasMessageSource.UNKNOWN;
		if (content == null) throw new IllegalArgumentException("content");
	}
}
