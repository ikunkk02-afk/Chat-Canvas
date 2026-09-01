package io.github.ikunkk02.chatcanvas.chat.history;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import io.github.ikunkk02.chatcanvas.chat.command.SensitiveCommandDetector;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasChannel;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessage;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageSource;
import io.github.ikunkk02.chatcanvas.config.ChatLogConfig;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.network.chat.Component;

/**
 * Singleton background service that converts validated ChatCanvasMessages
 * into StoredChatMessage records and writes them to UTF-8 JSONL files.
 */
public final class LocalChatLogService implements ChatLogService {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final Path CHAT_LOGS_ROOT =
			net.neoforged.fml.loading.FMLPaths.GAMEDIR.get()
					.resolve("chatcanvas").resolve("chat-logs");
    private static final int QUEUE_CAPACITY = 8192;
    private static final long ERROR_COOLDOWN_MS = 10_000L;
    static final int FLUSH_INTERVAL_SECONDS = 1;

    private static final class Holder {
        static final LocalChatLogService INSTANCE = new LocalChatLogService();
    }

    public static LocalChatLogService instance() { return Holder.INSTANCE; }

    private final ScheduledExecutorService executor;
    private final BlockingQueue<QueuedRecord> queue;
    private final AtomicBoolean running;
    private final AtomicLong lastErrorMs;
    private final AtomicInteger droppedSinceLastNotice;
    private final AtomicLong queuedSinceDrain;

    private volatile ChatLogConfig config = ChatLogConfig.DEFAULT;
    private volatile ChatLogContext context;
    private volatile ChatLogContext writerContext;
    private volatile boolean writerOpen;
    private final ChatLogWriter writer;

    private record QueuedRecord(StoredChatMessage message, ChatLogContext context) {}

    private LocalChatLogService() {
        this.executor = Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "ChatCanvas-ChatLogWriter"));
        this.queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        this.running = new AtomicBoolean(true);
        this.lastErrorMs = new AtomicLong();
        this.droppedSinceLastNotice = new AtomicInteger();
        this.queuedSinceDrain = new AtomicLong();
        this.writer = new ChatLogWriter(ZONE, config.maxFileSizeBytes());
        executor.scheduleWithFixedDelay(this::drainAndFlush,
                FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    // --- configuration (called from settings or on init) ----------------------

    public void updateConfig(ChatLogConfig value) {
        config = value == null ? ChatLogConfig.DEFAULT : value;
    }

    public ChatLogConfig config() { return config; }

    public Path logsRoot() { return CHAT_LOGS_ROOT; }

    // --- ChatLogService -------------------------------------------------------

    @Override
    public void record(ChatCanvasMessage message) {
        if (message == null || !config.enabled()) return;
        if (!shouldSave(message)) return;
        StoredChatMessage stored = StoredChatMessage.from(message, ZONE);
        boolean offered = queue.offer(new QueuedRecord(stored, context));
        if (!offered) {
            long dropped = queuedSinceDrain.incrementAndGet();
            if (dropped == 1) {
                reportThrottled("chat_canvas.chat_log.error.write_slow", null);
            }
        }
    }

    @Override
    public void switchContext(ChatLogContext newContext) {
        context = newContext;
        if (!running.get()) return;
        // Enqueue a sentinel that forces drain and context switch on writer thread
        try {
            executor.execute(() -> {
                drainQueue();
                flushWriter();
                closeWriterFile();
                writerContext = null;
                writerOpen = false;
                if (context != null) {
                    tryOpenWriter();
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException ignored) {
            // A NeoForge disconnect event may follow GameShuttingDownEvent.
            // close() already drained and closed the writer in that case.
        }
    }

    @Override
    public void flush() {
        try { executor.submit(this::flushWriter).get(5, TimeUnit.SECONDS); }
        catch (Exception ignored) { }
    }

    @Override
    public void close() {
        if (!running.compareAndSet(true, false)) return;
        executor.execute(() -> {
            drainQueue();
            flushWriter();
            closeWriterFile();
        });
        executor.shutdown();
        try { executor.awaitTermination(10, TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // --- public helpers -------------------------------------------------------

    public void openLogsDirectory() {
        try {
            Files.createDirectories(CHAT_LOGS_ROOT);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(CHAT_LOGS_ROOT.toFile());
            }
        } catch (IOException e) {
            ChatCanvas.LOGGER.warn("Cannot open chat-logs directory", e);
            reportThrottled("chat_canvas.chat_log.error.open_dir", null);
        }
    }

    public void runRetentionCleanup() {
        executor.execute(() -> {
            if (config.retentionDays() <= 0) return;
            try {
                Files.createDirectories(CHAT_LOGS_ROOT);
                try (var stream = Files.newDirectoryStream(CHAT_LOGS_ROOT)) {
                    for (Path dir : stream) {
                        if (Files.isDirectory(dir)) {
                            ChatLogRetentionManager.clean(
                                    dir, config.retentionDays(),
                                    writer.currentFileForTest(),
                                    msg -> ChatCanvas.LOGGER.info("[ChatCanvas] " + msg));
                        }
                    }
                }
            } catch (IOException e) {
                ChatCanvas.LOGGER.warn("Retention cleanup failed", e);
            }
        });
    }

    // --- internal -------------------------------------------------------------

    private void drainAndFlush() {
        drainQueue();
        flushWriter();
    }

    private void drainQueue() {
        QueuedRecord item;
        int count = 0;
        while ((item = queue.poll()) != null && count < QUEUE_CAPACITY) {
            count++;
            if (item.context() != context) continue; // stale context
            tryWrite(item.message());
        }
        queuedSinceDrain.set(0);
    }

    private void tryWrite(StoredChatMessage message) {
        if (!tryOpenWriter()) return;
        try {
            writer.write(message);
        } catch (Exception e) {
            closeWriterFile();
            writerOpen = false;
            reportThrottled("chat_canvas.chat_log.error.write_failed", e);
        }
    }

    private boolean tryOpenWriter() {
        if (writerOpen) return true;
        if (context == null) return false;
        try {
            Path dir = CHAT_LOGS_ROOT.resolve(context.directoryName());
            writer.open(dir);
            writerOpen = true;
            writerContext = context;
            return true;
        } catch (Exception e) {
            ChatCanvas.LOGGER.warn("Cannot open chat log writer for {}", context, e);
            reportThrottled("chat_canvas.chat_log.error.write_failed", e);
            return false;
        }
    }

    private void flushWriter() {
        try { writer.flush(); } catch (Exception ignored) { }
    }

    private void closeWriterFile() {
        writer.close();
        writerOpen = false;
    }

    // --- filtering ------------------------------------------------------------

    private boolean shouldSave(ChatCanvasMessage message) {
        if (message.channel() == ChatCanvasChannel.PLAYER_CHAT) {
            if (message.selfMessage() && !config.saveSelfMessages()) return false;
            if (!message.selfMessage() && !config.saveOtherPlayersMessages()) return false;
            return true;
        }
        // COMMAND_SYSTEM
        if (!config.saveCommandSystemMessages()) return false;
        // Exclude command input/results/errors and mod errors
        ChatCanvasMessageSource source = message.source();
        if (source == ChatCanvasMessageSource.COMMAND_INPUT
                || source == ChatCanvasMessageSource.COMMAND_RESULT
                || source == ChatCanvasMessageSource.COMMAND_ERROR
                || source == ChatCanvasMessageSource.CHAT_CANVAS_ERROR
                || source == ChatCanvasMessageSource.MOD_ERROR) {
            return false;
        }
        // Block sensitive commands
        String text = message.content().getString();
        if (!text.isBlank() && SensitiveCommandDetector.isSensitive(text)) {
            return false;
        }
        return true;
    }

    private void reportThrottled(String key, Throwable throwable) {
        long now = System.currentTimeMillis();
        long last = lastErrorMs.get();
        if (now - last >= ERROR_COOLDOWN_MS && lastErrorMs.compareAndSet(last, now)) {
            try {
                // route error to chat
                io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress
                        .instance().reportError(
                        Component.translatable(key), throwable);
            } catch (Exception ignored) { }
        }
    }
}
