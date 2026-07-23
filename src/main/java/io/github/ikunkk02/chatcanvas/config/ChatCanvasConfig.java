package io.github.ikunkk02.chatcanvas.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.ikunkk02.chatcanvas.ChatCanvas;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ChatCanvasConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ChatCanvasConfig instance;

	private final Path path;
	private LayoutConfig layout = LayoutConfig.DEFAULT;

	public ChatCanvasConfig(Path path) {
		this.path = path;
	}

	public static synchronized ChatCanvasConfig initialize() {
		if (instance == null) {
			instance = new ChatCanvasConfig(FabricLoader.getInstance().getConfigDir().resolve("chat_canvas.json"));
			instance.load();
		}
		return instance;
	}

	public static ChatCanvasConfig instance() {
		if (instance == null) {
			return initialize();
		}
		return instance;
	}

	public synchronized LayoutConfig layout() {
		return layout;
	}

	public synchronized void load() {
		if (Files.notExists(path)) {
			layout = LayoutConfig.DEFAULT;
			if (!save(layout)) {
				ChatCanvas.LOGGER.warn("Could not create default Chat Canvas config at {}", path);
			}
			return;
		}

		try (Reader reader = Files.newBufferedReader(path)) {
			LayoutConfig parsed = GSON.fromJson(reader, LayoutConfig.class);
			if (parsed == null || parsed.sanitized() == LayoutConfig.DEFAULT && !LayoutConfig.DEFAULT.equals(parsed)) {
				ChatCanvas.LOGGER.warn("Invalid Chat Canvas config at {}; using defaults", path);
				layout = LayoutConfig.DEFAULT;
			} else {
				layout = parsed.sanitized();
			}
		} catch (IOException | RuntimeException exception) {
			ChatCanvas.LOGGER.warn("Failed to read Chat Canvas config at {}; using defaults", path, exception);
			layout = LayoutConfig.DEFAULT;
		}
	}

	public synchronized boolean save(LayoutConfig value) {
		LayoutConfig sanitized = value.sanitized();
		Path parent = path.getParent();
		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			if (parent != null) {
				Files.createDirectories(parent);
			}
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(sanitized, writer);
			}
			try {
				Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ignored) {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
			}
			layout = sanitized;
			return true;
		} catch (IOException exception) {
			ChatCanvas.LOGGER.error("Failed to save Chat Canvas config to {}", path, exception);
			try {
				Files.deleteIfExists(temporary);
			} catch (IOException cleanupException) {
				ChatCanvas.LOGGER.debug("Failed to remove temporary config {}", temporary, cleanupException);
			}
			return false;
		}
	}

	public Path path() {
		return path;
	}
}
