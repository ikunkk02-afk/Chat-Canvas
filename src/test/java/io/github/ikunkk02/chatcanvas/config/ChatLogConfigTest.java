package io.github.ikunkk02.chatcanvas.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatLogConfigTest {

    @Test
    void defaultValues_areCorrect() {
        ChatLogConfig config = ChatLogConfig.DEFAULT;
        assertTrue(config.enabled());
        assertTrue(config.saveSelfMessages());
        assertTrue(config.saveOtherPlayersMessages());
        assertFalse(config.saveCommandSystemMessages());
        assertEquals(30, config.retentionDays());
        assertEquals(10L * 1024 * 1024, config.maxFileSizeBytes());
    }

    @Test
    void sanitized_doesNotChangeValid() {
        assertEquals(ChatLogConfig.DEFAULT, ChatLogConfig.DEFAULT.sanitized());
    }

    @Test
    void retentionDays_clamped() {
        ChatLogConfig config = ChatLogConfig.DEFAULT;
        ChatLogConfig clamped = config.withRetentionDays(99999);
        assertEquals(ChatLogConfig.MAX_RETENTION_DAYS, clamped.retentionDays());
        assertEquals(ChatLogConfig.MIN_RETENTION_DAYS, config.withRetentionDays(-1).retentionDays());
    }

    @Test
    void maxFileSizeBytes_clamped() {
        ChatLogConfig config = ChatLogConfig.DEFAULT;
        assertEquals(ChatLogConfig.MAX_FILE_SIZE_BYTES,
                config.withMaxFileSizeBytes(Long.MAX_VALUE).maxFileSizeBytes());
        assertEquals(ChatLogConfig.MIN_FILE_SIZE_BYTES,
                config.withMaxFileSizeBytes(0).maxFileSizeBytes());
    }

    @Test
    void withEnabled_toggles() {
        ChatLogConfig config = ChatLogConfig.DEFAULT;
        assertFalse(config.withEnabled(false).enabled());
        assertTrue(config.withEnabled(false).withEnabled(true).enabled());
    }

    @Test
    void withSaveSelfMessages_toggles() {
        assertFalse(ChatLogConfig.DEFAULT.withSaveSelfMessages(false).saveSelfMessages());
    }

    @Test
    void withSaveOtherPlayersMessages_toggles() {
        assertFalse(ChatLogConfig.DEFAULT.withSaveOtherPlayersMessages(false).saveOtherPlayersMessages());
    }

    @Test
    void withSaveCommandSystemMessages_toggles() {
        assertTrue(ChatLogConfig.DEFAULT.withSaveCommandSystemMessages(true).saveCommandSystemMessages());
    }

    @Test
    void retentionDays_zero_meansForever() {
        assertEquals(0, ChatLogConfig.DEFAULT.withRetentionDays(0).retentionDays());
    }
}
