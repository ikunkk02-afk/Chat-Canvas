package io.github.ikunkk02.chatcanvas.chat.notification;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record MentionNotificationEvent(
		UUID messageId,
		@Nullable PlayerChatIdentity sender,
		Text originalMessage,
		String plainPreview,
		long receivedAtMs
) {
	public MentionNotificationEvent {
		if (messageId == null) throw new IllegalArgumentException("messageId");
		if (originalMessage == null) throw new IllegalArgumentException("originalMessage");
		plainPreview = plainPreview == null ? "" : plainPreview;
	}
}
