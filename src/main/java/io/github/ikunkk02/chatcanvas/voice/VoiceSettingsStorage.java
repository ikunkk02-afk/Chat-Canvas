package io.github.ikunkk02.chatcanvas.voice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import net.minecraftforge.fml.ModList;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class VoiceSettingsStorage {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = java.nio.file.Path.of("config")
			.resolve("chatcanvas").resolve("voice.json");
	private VoiceSettings settings = VoiceSettings.DEFAULT;

	public synchronized VoiceSettings load() {
		if (Files.notExists(PATH)) {
			save(settings);
			return settings;
		}
		try (Reader reader = Files.newBufferedReader(PATH)) {
			VoiceSettings parsed = GSON.fromJson(reader, VoiceSettings.class);
			settings = parsed == null ? VoiceSettings.DEFAULT : new VoiceSettings(
					parsed.enabled(), parsed.microphoneId(), parsed.maximumSeconds(),
					parsed.showInputLevel(), parsed.noiseThreshold(),
					parsed.showPartialResults(), parsed.addFinalPunctuation());
		} catch (Exception exception) {
			ChatCanvasForge.LOGGER.error("Failed to load voice settings; using defaults", exception);
			settings = VoiceSettings.DEFAULT;
		}
		return settings;
	}

	public synchronized boolean save(VoiceSettings value) {
		settings = value == null ? VoiceSettings.DEFAULT : value;
		Path temporary = PATH.resolveSibling(PATH.getFileName() + ".tmp");
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(temporary)) {
				GSON.toJson(settings, writer);
			}
			Files.move(temporary, PATH, StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (Exception exception) {
			ChatCanvasForge.LOGGER.error("Failed to save voice settings", exception);
			try { Files.deleteIfExists(temporary); } catch (Exception ignored) { }
			return false;
		}
	}

	public synchronized VoiceSettings settings() {
		return settings;
	}
}
