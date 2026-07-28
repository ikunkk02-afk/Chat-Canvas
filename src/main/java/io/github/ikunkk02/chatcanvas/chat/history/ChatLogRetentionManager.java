package io.github.ikunkk02.chatcanvas.chat.history;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

public final class ChatLogRetentionManager {

    /** Matches "yyyy-MM-dd.jsonl" or "yyyy-MM-dd_2.jsonl" etc. */
    private static final Pattern LOG_FILE_PATTERN =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2}(_\\d+)?\\.jsonl");

    private ChatLogRetentionManager() {}

    /**
     * Deletes log files older than {@code retentionDays} under {@code root}.
     * Never deletes the currently-open file. Skips non-matching files.
     */
    public static void clean(Path root, int retentionDays, Path currentFile,
                              java.util.function.Consumer<String> logger) {
        if (root == null || !Files.isDirectory(root)) return;
        if (retentionDays <= 0) return; // keep forever

        long cutoff = System.currentTimeMillis()
                - retentionDays * 24L * 60 * 60 * 1000;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path entry : stream) {
                if (!Files.isRegularFile(entry)) continue;
                String name = entry.getFileName().toString();
                if (!LOG_FILE_PATTERN.matcher(name).matches()) continue;
                if (currentFile != null && entry.equals(currentFile)) continue;
                BasicFileAttributes attrs;
                try {
                    attrs = Files.readAttributes(entry, BasicFileAttributes.class);
                } catch (IOException e) {
                    continue;
                }
                if (attrs.lastModifiedTime().toInstant().isBefore(Instant.ofEpochMilli(cutoff))) {
                    try {
                        Files.delete(entry);
                        logger.accept("Deleted old chat log: " + name);
                    } catch (IOException e) {
                        logger.accept("Could not delete old chat log: " + name);
                    }
                }
            }
        } catch (IOException e) {
            logger.accept("Could not scan chat-logs directory: " + e.getMessage());
        }
    }
}
