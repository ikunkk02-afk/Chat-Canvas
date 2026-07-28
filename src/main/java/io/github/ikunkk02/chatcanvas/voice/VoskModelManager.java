package io.github.ikunkk02.chatcanvas.voice;

import net.minecraftforge.fml.ModList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class VoskModelManager {
	private static final List<String> REQUIRED = List.of(
			"am/final.mdl",
			"conf/mfcc.conf",
			"conf/model.conf",
			"graph/HCLr.fst",
			"graph/Gr.fst",
			"graph/phones/word_boundary.int",
			"ivector/final.ie",
			"ivector/final.mat",
			"ivector/final.dubm"
	);
	private final Path modelsDirectory = java.nio.file.Path.of("config")
			.resolve("chatcanvas").resolve("voice-models");

	public Path modelsDirectory() {
		return modelsDirectory;
	}

	public Path modelDirectory() {
		return modelsDirectory.resolve(
				VoiceModelMetadata.CHINESE_SMALL.requiredRootDirectory());
	}

	public boolean isInstalled() {
		return validate(modelDirectory());
	}

	public boolean validate(Path root) {
		if (root == null || !Files.isDirectory(root)) return false;
		for (String required : REQUIRED) {
			Path file = root.resolve(required).normalize();
			try {
				if (!file.startsWith(root.normalize()) || !Files.isRegularFile(file)
						|| Files.size(file) <= 0L) return false;
			} catch (IOException exception) {
				return false;
			}
		}
		return true;
	}
}
