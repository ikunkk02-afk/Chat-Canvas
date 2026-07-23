package io.github.ikunkk02.chatcanvas.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
	private ChatCanvasSettings settings = ChatCanvasSettings.DEFAULT;

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
		return settings.layout();
	}

	public synchronized ChatTextConfig text() {
		return settings.text();
	}

	public synchronized ChatCanvasSettings settings() {
		return settings;
	}

	public synchronized void load() {
		if (Files.notExists(path)) {
			settings = ChatCanvasSettings.DEFAULT;
			if (!save(settings)) {
				ChatCanvas.LOGGER.warn("Could not create default Chat Canvas config at {}", path);
			}
			return;
		}

		try (Reader reader = Files.newBufferedReader(path)) {
			JsonElement root = JsonParser.parseReader(reader);
			if (!root.isJsonObject()) {
				throw new IllegalArgumentException("Chat Canvas config root must be an object");
			}
			settings = parseSettings(root.getAsJsonObject()).sanitized();
		} catch (IOException | RuntimeException exception) {
			ChatCanvas.LOGGER.warn("Failed to read Chat Canvas config at {}; using defaults", path, exception);
			settings = ChatCanvasSettings.DEFAULT;
		}
	}

	public synchronized boolean save(LayoutConfig value) {
		return save(new ChatCanvasSettings(value, settings.text()));
	}

	public synchronized boolean save(ChatCanvasSettings value) {
		ChatCanvasSettings sanitized = value.sanitized();
		Path parent = path.getParent();
		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			if (parent != null) {
				Files.createDirectories(parent);
			}
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(toJson(sanitized), writer);
			}
			try {
				Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException ignored) {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
			}
			settings = sanitized;
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

	private static ChatCanvasSettings parseSettings(JsonObject root) {
		LayoutConfig defaults = LayoutConfig.DEFAULT;
		JsonObject layout = objectOr(root, "layout", root);
		LayoutConfig parsedLayout = new LayoutConfig(
				doubleOr(layout, "chatXRatio", defaults.chatXRatio()),
				doubleOr(layout, "chatYRatio", defaults.chatYRatio()),
				doubleOr(layout, "chatWidthRatio", defaults.chatWidthRatio()),
				doubleOr(layout, "chatHeightRatio", defaults.chatHeightRatio())
		).sanitized();

		ChatTextConfig textDefaults = ChatTextConfig.DEFAULT;
		JsonObject text = objectOr(root, "text", null);
		if (text == null) {
			return new ChatCanvasSettings(parsedLayout, textDefaults);
		}
		ChatTextConfig parsedText = new ChatTextConfig(
				doubleOr(text, "fontScale", textDefaults.fontScale()),
				doubleOr(text, "lineSpacing", textDefaults.lineSpacing()),
				doubleOr(text, "textOpacity", textDefaults.textOpacity()),
				alignmentOr(text, "alignment", textDefaults.alignment()),
				booleanOr(text, "shadow", textDefaults.shadow())
		).sanitized();
		return new ChatCanvasSettings(parsedLayout, parsedText);
	}

	private static JsonObject toJson(ChatCanvasSettings value) {
		JsonObject root = new JsonObject();
		LayoutConfig layout = value.layout();
		root.addProperty("chatXRatio", layout.chatXRatio());
		root.addProperty("chatYRatio", layout.chatYRatio());
		root.addProperty("chatWidthRatio", layout.chatWidthRatio());
		root.addProperty("chatHeightRatio", layout.chatHeightRatio());

		ChatTextConfig text = value.text();
		JsonObject textObject = new JsonObject();
		textObject.addProperty("fontScale", text.fontScale());
		textObject.addProperty("lineSpacing", text.lineSpacing());
		textObject.addProperty("textOpacity", text.textOpacity());
		textObject.addProperty("alignment", text.alignment().name());
		textObject.addProperty("shadow", text.shadow());
		root.add("text", textObject);
		return root;
	}

	private static JsonObject objectOr(JsonObject root, String key, JsonObject fallback) {
		JsonElement element = root.get(key);
		return element != null && element.isJsonObject() ? element.getAsJsonObject() : fallback;
	}

	private static double doubleOr(JsonObject object, String key, double fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return element.getAsDouble();
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static boolean booleanOr(JsonObject object, String key, boolean fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return element.getAsBoolean();
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static ChatTextAlignment alignmentOr(JsonObject object, String key,
												 ChatTextAlignment fallback) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
			return fallback;
		}
		try {
			return ChatTextAlignment.valueOf(element.getAsString().toUpperCase(java.util.Locale.ROOT));
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}
}
