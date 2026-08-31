package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VoiceArtifactInstallerTest {
	@TempDir Path temporary;

	@Test
	void sha256VerificationRejectsCorruptArtifact() throws Exception {
		Path artifact = temporary.resolve("model.onnx");
		Files.writeString(artifact, "chat-canvas");
		assertDoesNotThrow(() -> VoiceArtifactInstaller.verifySha256(artifact,
				"D6DC07BE1C88FBD8F10C1D5FDA27701440E96542729BAF43BC2817E9F71F4C1A"));
		assertThrows(IOException.class, () -> VoiceArtifactInstaller.verifySha256(artifact,
				"0000000000000000000000000000000000000000000000000000000000000000"));
	}

	@Test
	void recursiveDeleteCannotEscapeVoiceDataRoot() throws Exception {
		Path allowed = Files.createDirectory(temporary.resolve("voice-models"));
		Path model = Files.createDirectories(allowed.resolve("model").resolve("nested"));
		Files.writeString(model.resolve("file"), "data");
		VoiceArtifactInstaller.deleteTree(allowed.resolve("model"), allowed);
		assertThrows(IOException.class, () -> VoiceArtifactInstaller.deleteTree(allowed, allowed));
	}

	@Test
	void acceptsCurrentHuggingFaceCdnRedirects() {
		assertDoesNotThrow(() -> VoiceArtifactInstaller.validateUri(
				URI.create("https://us.aws.cdn.hf.co/xet-bridge-us/model")));
		assertDoesNotThrow(() -> VoiceArtifactInstaller.validateUri(
				URI.create("https://cdn-lfs.hf.co/repository/model")));
	}

	@Test
	void rejectsLookalikeHuggingFaceCdnHosts() {
		assertThrows(IOException.class, () -> VoiceArtifactInstaller.validateUri(
				URI.create("https://us.aws.cdn.hf.co.attacker.example/model")));
		assertThrows(IOException.class, () -> VoiceArtifactInstaller.validateUri(
				URI.create("http://us.aws.cdn.hf.co/model")));
	}
}
