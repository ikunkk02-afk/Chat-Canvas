package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class VoskAsrProviderIntegrationTest {
	@Test
	void installedWorkspaceModelLoadsCreatesSessionAndFinalizes() throws Exception {
		Path modelRoot = Path.of("run", "config", "chatcanvas", "voice-models",
				VoiceModelRegistry.VOSK_CN).toAbsolutePath();
		Assumptions.assumeTrue(Files.isRegularFile(modelRoot.resolve("am/final.mdl")),
				"workspace Vosk model is not installed");
		VoskEncodingBootstrap.initialize();
		try (VoskAsrProvider provider = new VoskAsrProvider()) {
			provider.loadModel(VoiceModelRegistry.get(VoiceModelRegistry.VOSK_CN),
					modelRoot, new AsrRuntimeOptions(2, false));
			try (AsrSession session = provider.createSession()) {
				session.acceptAudio(new byte[3_200], 3_200);
				assertNotNull(session.finish());
			}
		}
	}
}
