package io.github.ikunkk02.chatcanvas.chat.history;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasChannel;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessage;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageSource;
import net.minecraft.network.chat.Component;

import static org.junit.jupiter.api.Assertions.*;

class StoredChatMessageTest {

    @Test
    void from_convertsMessageCorrectly() {
        UUID msgId = UUID.randomUUID();
        long now = 1753644800000L;
        ChatCanvasMessage original = new ChatCanvasMessage(
                msgId,
                ChatCanvasChannel.PLAYER_CHAT,
                ChatCanvasMessageSource.PLAYER,
                UUID.randomUUID(),
                Component.literal("Steve"),
                Component.literal("Hello 😀"),
                now,
                false,
                true);

        StoredChatMessage stored = StoredChatMessage.from(original, ZoneId.of("Asia/Shanghai"));
        assertEquals(1, stored.schemaVersion());
        assertEquals(msgId.toString(), stored.messageId());
        assertEquals(now, stored.timestamp());
        assertNotNull(stored.localTime());
        assertEquals("PLAYER_CHAT", stored.channel());
        assertEquals("PLAYER", stored.source());
        assertNotNull(stored.senderUuid());
        assertEquals("Steve", stored.senderName());
        assertEquals("Hello 😀", stored.plainText());
        assertFalse(stored.selfMessage());
        assertTrue(stored.mentionedCurrentPlayer());
    }

    @Test
    void senderUuid_nullForUnknown() {
        ChatCanvasMessage original = new ChatCanvasMessage(
                UUID.randomUUID(), ChatCanvasChannel.PLAYER_CHAT,
                ChatCanvasMessageSource.PLAYER,
                null, null, Component.literal("test"),
                System.currentTimeMillis(), false, false);
        StoredChatMessage stored = StoredChatMessage.from(original, ZoneId.systemDefault());
        assertNull(stored.senderUuid());
        assertNull(stored.senderName());
    }

    @Test
    void emoji_preserved() {
        ChatCanvasMessage original = new ChatCanvasMessage(
                UUID.randomUUID(), ChatCanvasChannel.PLAYER_CHAT,
                ChatCanvasMessageSource.PLAYER,
                null, Component.literal("Tester"), Component.literal("红石⚙️测试 中文 English 123 😀"),
                System.currentTimeMillis(), false, false);
        StoredChatMessage stored = StoredChatMessage.from(original, ZoneId.systemDefault());
        assertEquals("红石⚙️测试 中文 English 123 😀", stored.plainText());
    }

    @Test
    void json_serialization(@TempDir Path tempDir) throws Exception {
        UUID msgId = UUID.randomUUID();
        ChatCanvasMessage original = new ChatCanvasMessage(
                msgId, ChatCanvasChannel.PLAYER_CHAT,
                ChatCanvasMessageSource.PLAYER,
                null, Component.literal("Steve"), Component.literal("你好\n\"test\""),
                System.currentTimeMillis(), false, false);
        StoredChatMessage stored = StoredChatMessage.from(original, ZoneId.systemDefault());

        // Serialize to JSONL line and back
        Path file = tempDir.resolve("test.jsonl");
        String json = ChatLogJson.GSON.toJson(stored);
        Files.writeString(file, json + "\n", StandardCharsets.UTF_8);

        // Read back
        StoredChatMessage parsed = ChatLogJson.GSON.fromJson(json, StoredChatMessage.class);
        assertEquals(stored.messageId(), parsed.messageId());
        assertEquals(stored.plainText(), parsed.plainText());
        assertEquals(stored.timestamp(), parsed.timestamp());
    }
}
