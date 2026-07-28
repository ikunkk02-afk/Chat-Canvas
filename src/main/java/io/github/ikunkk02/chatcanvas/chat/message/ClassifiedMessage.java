package io.github.ikunkk02.chatcanvas.chat.message;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ClassifiedMessage(
		ChatCanvasChannel channel,
		ChatCanvasMessageSource source,
		@Nullable UUID senderUuid,
		@Nullable Component senderName,
		Component content,
		boolean selfMessage
) {
	public ClassifiedMessage {
		if (channel == null) throw new IllegalArgumentException("channel");
		if (source == null) source = ChatCanvasMessageSource.UNKNOWN;
		if (content == null) throw new IllegalArgumentException("content");
	}
}
