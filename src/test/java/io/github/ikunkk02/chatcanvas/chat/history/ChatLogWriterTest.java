package io.github.ikunkk02.chatcanvas.chat.history;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasChannel;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessage;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageSource;

import static org.junit.jupiter.api.Assertions.*;


class ChatLogWriterTest {

    @Test
    void writesAndRotatesByDate(@TempDir Path tempDir) throws Exception {
        ZoneId utc = ZoneId.of("UTC");
        ChatLogWriter writer = new ChatLogWriter(utc, 1024);

        // Use override to control "today" – but we only test basics
        writer.open(tempDir);
        StoredChatMessage msg = buildMsg("Hello");
        writer.write(msg);
        writer.flush();

        Path file = writer.currentFileForTest();
        assertNotNull(file);
        assertTrue(Files.exists(file));
        assertTrue(file.getFileName().toString().endsWith(".jsonl"));

        String content = Files.readString(file, StandardCharsets.UTF_8).trim();
        assertTrue(content.contains("\"Hello\""));

        writer.close();
    }

    @Test
    void rotatesOnSizeLimit(@TempDir Path tempDir) throws Exception {
        ZoneId utc = ZoneId.of("UTC");
        // Small max so each message triggers rotation
        ChatLogWriter writer = new ChatLogWriter(utc, 50);
        writer.open(tempDir);

        StoredChatMessage msg1 = buildMsg("1234567890");
        writer.write(msg1);
        Path file1 = writer.currentFileForTest();

        StoredChatMessage msg2 = buildMsg("abcdefghij");
        writer.write(msg2);
        Path file2 = writer.currentFileForTest();

        assertNotNull(file1);
        assertNotNull(file2);
        // After rotation, different files
        assertNotEquals(file1.getFileName().toString(), file2.getFileName().toString());
        writer.close();
    }

    @Test
    void utf8_preservesChineseAndEmoji(@TempDir Path tempDir) throws Exception {
        ZoneId utc = ZoneId.of("UTC");
        ChatLogWriter writer = new ChatLogWriter(utc, 10 * 1024 * 1024);
        writer.open(tempDir);

        StoredChatMessage msg = buildMsg("你好世界😀");
        writer.write(msg);
        writer.flush();

        Path file = writer.currentFileForTest();
        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.contains("你好世界😀"));
        writer.close();
    }

    @Test
    void close_andReopen_works(@TempDir Path tempDir) throws Exception {
        ZoneId utc = ZoneId.of("UTC");
        ChatLogWriter writer = new ChatLogWriter(utc, 10 * 1024 * 1024);
        writer.open(tempDir);
        writer.write(buildMsg("first"));
        writer.close();

        ChatLogWriter writer2 = new ChatLogWriter(utc, 10 * 1024 * 1024);
        writer2.open(tempDir);
        writer2.write(buildMsg("second"));
        writer2.flush();

        Path file = writer2.currentFileForTest();
        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.contains("first"));
        assertTrue(content.contains("second"));
        writer2.close();
    }

    private static StoredChatMessage buildMsg(String text) {
        ChatCanvasMessage msg = new ChatCanvasMessage(
                UUID.randomUUID(), ChatCanvasChannel.PLAYER_CHAT,
                ChatCanvasMessageSource.PLAYER,
                null, Component.literal("Tester"), Component.literal(text),
                System.currentTimeMillis(), false, false);
        return StoredChatMessage.from(msg, ZoneId.of("UTC"));
    }
}
