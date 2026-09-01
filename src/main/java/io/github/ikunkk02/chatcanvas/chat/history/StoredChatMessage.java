package io.github.ikunkk02.chatcanvas.chat.history;

import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessage;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record StoredChatMessage(
        int schemaVersion,
        String messageId,
        long timestamp,
        String localTime,
        String channel,
        String source,
        String senderUuid,
        String senderName,
        String plainText,
        boolean selfMessage,
        boolean mentionedCurrentPlayer
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public static StoredChatMessage from(ChatCanvasMessage original, ZoneId zone) {
        OffsetDateTime time = OffsetDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(original.receivedAt()), zone);
        return new StoredChatMessage(
                CURRENT_SCHEMA_VERSION,
                original.messageId().toString(),
                original.receivedAt(),
                time.toString(),
                original.channel().name(),
                original.source().name(),
                original.senderUuid() == null ? null : original.senderUuid().toString(),
                original.senderName() == null ? null : original.senderName().getString(),
                original.content().getString(),
                original.selfMessage(),
                original.mentionedCurrentPlayer());
    }
}
