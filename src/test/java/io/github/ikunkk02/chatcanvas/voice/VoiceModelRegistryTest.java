package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class VoiceModelRegistryTest {
	@Test
	void registryContainsExistingVoskAndThreeSherpaModels() {
		assertEquals(4, VoiceModelRegistry.all().size());
		assertNotNull(VoiceModelRegistry.get(VoiceModelRegistry.VOSK_CN));
		assertNotNull(VoiceModelRegistry.get(VoiceModelRegistry.ZIPFORMER_ZH));
		assertNotNull(VoiceModelRegistry.get(VoiceModelRegistry.SENSE_VOICE));
		assertNotNull(VoiceModelRegistry.get(VoiceModelRegistry.WHISPER_TINY));
	}

	@Test
	void directModelDownloadSizesEqualFileManifests() {
		for (VoiceModelDescriptor model : VoiceModelRegistry.all()) {
			long sum = model.files().stream().mapToLong(VoiceModelFile::size).sum();
			assertEquals(model.downloadSize(), sum, model.id());
			assertEquals(ReloadPolicy.HOT_SWAP, model.reloadPolicy(), model.id());
		}
	}

	@Test
	void idsAndFileHashesAreValidAndUnique() {
		HashSet<String> ids = new HashSet<>();
		for (VoiceModelDescriptor model : VoiceModelRegistry.all()) {
			assertTrue(ids.add(model.id()));
			assertFalse(model.languageKeys().isEmpty());
			for (VoiceModelFile file : model.files()) {
				assertTrue(file.downloadUrl().startsWith("https://"));
				assertTrue(file.sha256().matches("(?i)[0-9a-f]{64}"));
			}
		}
	}
}
