package io.github.ikunkk02.chatcanvas.chat.history;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import io.github.ikunkk02.chatcanvas.config.ChatLogConfig;
import net.minecraftforge.fml.ModList;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ChatLogConfigStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get()
            .resolve("chatcanvas").resolve("chatlog.json");
    private ChatLogConfig config = ChatLogConfig.DEFAULT;

    public synchronized ChatLogConfig load() {
        if (Files.notExists(PATH)) {
            save(config);
            return config;
        }
        try (Reader reader = Files.newBufferedReader(PATH)) {
            ChatLogConfig parsed = GSON.fromJson(reader, ChatLogConfig.class);
            config = parsed == null ? ChatLogConfig.DEFAULT : parsed.sanitized();
        } catch (Exception e) {
            ChatCanvasForge.LOGGER.error("Failed to load chat log config; using defaults", e);
            config = ChatLogConfig.DEFAULT;
        }
        return config;
    }

    public synchronized boolean save(ChatLogConfig value) {
        config = value == null ? ChatLogConfig.DEFAULT : value;
        Path temporary = PATH.resolveSibling(PATH.getFileName() + ".tmp");
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(config, writer);
            }
            Files.move(temporary, PATH, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            ChatCanvasForge.LOGGER.error("Failed to save chat log config", e);
            try { Files.deleteIfExists(temporary); } catch (Exception ignored) { }
            return false;
        }
    }

    public synchronized ChatLogConfig config() {
        return config;
    }
}
