package io.github.ikunkk02.chatcanvas.config;

public record ChatLogConfig(
        boolean enabled,
        boolean saveSelfMessages,
        boolean saveOtherPlayersMessages,
        boolean saveCommandSystemMessages,
        int retentionDays,
        long maxFileSizeBytes
) {
    public static final int MIN_RETENTION_DAYS = 0;
    public static final int MAX_RETENTION_DAYS = 3650;
    public static final int DEFAULT_RETENTION_DAYS = 30;
    public static final long MIN_FILE_SIZE_BYTES = 1024L * 1024L;        // 1 MB
    public static final long MAX_FILE_SIZE_BYTES = 200L * 1024L * 1024L; // 200 MB
    public static final long DEFAULT_MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L; // 10 MB

    public static final ChatLogConfig DEFAULT = new ChatLogConfig(
            true, true, true, false,
            DEFAULT_RETENTION_DAYS, DEFAULT_MAX_FILE_SIZE_BYTES);

    public ChatLogConfig {
        retentionDays = Math.max(MIN_RETENTION_DAYS,
                Math.min(MAX_RETENTION_DAYS, retentionDays));
        maxFileSizeBytes = Math.max(MIN_FILE_SIZE_BYTES,
                Math.min(MAX_FILE_SIZE_BYTES, maxFileSizeBytes));
    }

    public ChatLogConfig sanitized() {
        return this;
    }

    public ChatLogConfig withEnabled(boolean value) {
        return new ChatLogConfig(value, saveSelfMessages, saveOtherPlayersMessages,
                saveCommandSystemMessages, retentionDays, maxFileSizeBytes);
    }

    public ChatLogConfig withSaveSelfMessages(boolean value) {
        return new ChatLogConfig(enabled, value, saveOtherPlayersMessages,
                saveCommandSystemMessages, retentionDays, maxFileSizeBytes);
    }

    public ChatLogConfig withSaveOtherPlayersMessages(boolean value) {
        return new ChatLogConfig(enabled, saveSelfMessages, value,
                saveCommandSystemMessages, retentionDays, maxFileSizeBytes);
    }

    public ChatLogConfig withSaveCommandSystemMessages(boolean value) {
        return new ChatLogConfig(enabled, saveSelfMessages, saveOtherPlayersMessages,
                value, retentionDays, maxFileSizeBytes);
    }

    public ChatLogConfig withRetentionDays(int value) {
        return new ChatLogConfig(enabled, saveSelfMessages, saveOtherPlayersMessages,
                saveCommandSystemMessages, value, maxFileSizeBytes);
    }

    public ChatLogConfig withMaxFileSizeBytes(long value) {
        return new ChatLogConfig(enabled, saveSelfMessages, saveOtherPlayersMessages,
                saveCommandSystemMessages, retentionDays, value);
    }
}
