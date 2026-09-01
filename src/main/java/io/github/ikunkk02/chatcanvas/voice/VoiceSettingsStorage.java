package io.github.ikunkk02.chatcanvas.voice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.ikunkk02.chatcanvas.ChatCanvas;
import net.neoforged.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class VoiceSettingsStorage {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private VoiceSettings settings = VoiceSettings.DEFAULT;

	public synchronized VoiceSettings load() {
		Path path = path();
		if (Files.notExists(path)) {
			save(settings);
			return settings;
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
			settings = decode(json);
		} catch (Exception exception) {
			ChatCanvas.LOGGER.error("Failed to load voice settings; using defaults", exception);
			settings = VoiceSettings.DEFAULT;
		}
		return settings;
	}

	static VoiceSettings decode(JsonObject json) {
		VoiceSettings parsed = GSON.fromJson(json, VoiceSettings.class);
		if (parsed == null) return VoiceSettings.DEFAULT;
		VoiceSettings defaults = VoiceSettings.DEFAULT;
		return new VoiceSettings(
				parsed.enabled(), parsed.microphoneId(), parsed.maximumSeconds(),
				parsed.showInputLevel(), parsed.noiseThreshold(),
				parsed.showPartialResults(), parsed.addFinalPunctuation(),
				json.has("selectedModelId") ? parsed.selectedModelId() : defaults.selectedModelId(),
				json.has("noSpeechTimeoutMillis") ? parsed.noSpeechTimeoutMillis() : defaults.noSpeechTimeoutMillis(),
				json.has("endpointSilenceMillis") ? parsed.endpointSilenceMillis() : defaults.endpointSilenceMillis(),
				json.has("tailPaddingMillis") ? parsed.tailPaddingMillis() : defaults.tailPaddingMillis(),
				json.has("inferenceThreads") ? parsed.inferenceThreads() : defaults.inferenceThreads());
	}

	public synchronized boolean save(VoiceSettings value) {
		settings = value == null ? VoiceSettings.DEFAULT : value;
		Path path = path();
		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(settings, writer);
			}
			Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (Exception exception) {
			ChatCanvas.LOGGER.error("Failed to save voice settings", exception);
			try { Files.deleteIfExists(temporary); } catch (Exception ignored) { }
			return false;
		}
	}

	public synchronized VoiceSettings settings() {
		return settings;
	}

	private static Path path() {
		return FMLPaths.CONFIGDIR.get().resolve("chatcanvas").resolve("voice.json");
	}
}
