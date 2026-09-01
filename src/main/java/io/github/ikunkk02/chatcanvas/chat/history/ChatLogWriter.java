package io.github.ikunkk02.chatcanvas.chat.history;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ChatLogWriter {
    private final ZoneId zone;
    private final long maxFileSizeBytes;
    private Path contextDir;
    private BufferedWriter writer;
    private Path currentFile;
    private long currentBytes;
    private LocalDate currentDate;

    public ChatLogWriter(ZoneId zone, long maxFileSizeBytes) {
        this.zone = zone;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public void open(Path directory) throws IOException {
        close();
        contextDir = directory;
        Files.createDirectories(contextDir);
        currentDate = LocalDate.now(zone);
        openCurrentFile();
    }

    public void write(StoredChatMessage message) throws IOException {
        if (writer == null) throw new IOException("Writer not open");
        LocalDate today = LocalDate.now(zone);
        if (!today.equals(currentDate)) {
            rotateToDate(today);
        } else if (currentBytes > 0 && currentBytes >= maxFileSizeBytes) {
            rotateWithinDate();
        }

        String json = ChatLogJson.GSON.toJson(message);
        writer.write(json);
        writer.newLine();
        // approximate size for rotation tracking
        currentBytes += json.length() + 1;
    }

    public void flush() throws IOException {
        if (writer != null) writer.flush();
    }

    public void close() {
        silentClose(writer);
        writer = null;
        currentFile = null;
        currentBytes = 0;
        currentDate = null;
    }

    public Path currentFileForTest() {
        return currentFile;
    }

    private void openCurrentFile() throws IOException {
        List<Path> existing = listFiles(currentDate);
        int nextIndex;
        if (existing.isEmpty()) {
            nextIndex = 1;
        } else {
            Path last = existing.get(existing.size() - 1);
            long size = Files.size(last);
            nextIndex = existing.size();
            if (size >= maxFileSizeBytes) nextIndex++;
        }
        currentFile = fileName(currentDate, nextIndex);
        currentBytes = Files.exists(currentFile) ? Files.size(currentFile) : 0;
        writer = new BufferedWriter(Files.newBufferedWriter(
                currentFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND));
    }

    private void rotateToDate(LocalDate date) throws IOException {
        closeWriter();
        currentDate = date;
        openCurrentFile();
    }

    private void rotateWithinDate() throws IOException {
        closeWriter();
        openCurrentFile();
    }

    private void closeWriter() {
        silentClose(writer);
        writer = null;
        currentFile = null;
        currentBytes = 0;
    }

    private Path fileName(LocalDate date, int index) {
        String name = index <= 1
                ? date.toString() + ".jsonl"
                : date.toString() + "_" + index + ".jsonl";
        return contextDir.resolve(name);
    }

    private List<Path> listFiles(LocalDate date) throws IOException {
        String prefix = date.toString();
        List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(contextDir,
                prefix + "*.jsonl")) {
            for (Path path : stream) {
                result.add(path);
            }
        }
        result.sort(Comparator.naturalOrder());
        return result;
    }

    private static void silentClose(AutoCloseable closeable) {
        if (closeable == null) return;
        try { closeable.close(); } catch (Exception ignored) { }
    }
}
