package io.github.ikunkk02.chatcanvas.voice;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoiceSettingsTest {
	@Test
	void newDefaultsMatchEndpointAndPerformancePolicy() {
		VoiceSettings settings = VoiceSettings.DEFAULT;
		assertEquals(5_000, settings.noSpeechTimeoutMillis());
		assertEquals(800, settings.endpointSilenceMillis());
		assertEquals(200, settings.tailPaddingMillis());
		assertEquals(25, settings.maximumSeconds());
		assertEquals(0, settings.inferenceThreads());
	}

	@Test
	void withersPreserveModelAndEndpointConfiguration() {
		VoiceSettings configured = VoiceSettings.DEFAULT.withSelectedModel(VoiceModelRegistry.SENSE_VOICE)
				.withEndpointTiming(4_000, 900, 250).withInferenceThreads(2);
		VoiceSettings toggled = configured.withEnabled(false).withShowPartialResults(false);
		assertEquals(VoiceModelRegistry.SENSE_VOICE, toggled.selectedModelId());
		assertEquals(4_000, toggled.noSpeechTimeoutMillis());
		assertEquals(900, toggled.endpointSilenceMillis());
		assertEquals(250, toggled.tailPaddingMillis());
		assertEquals(2, toggled.inferenceThreads());
	}

	@Test
	void legacyJsonUsesNewEndpointDefaultsButKeepsExistingSettings() {
		var json = JsonParser.parseString("""
				{"enabled":true,"microphoneId":"old-device","maximumSeconds":15,
				 "showInputLevel":true,"noiseThreshold":0.02,
				 "showPartialResults":true,"addFinalPunctuation":false}
				""").getAsJsonObject();
		VoiceSettings migrated = VoiceSettingsStorage.decode(json);
		assertEquals("old-device", migrated.microphoneId());
		assertEquals(15, migrated.maximumSeconds());
		assertEquals(5_000, migrated.noSpeechTimeoutMillis());
		assertEquals(800, migrated.endpointSilenceMillis());
		assertEquals(200, migrated.tailPaddingMillis());
	}

	@Test
	void explicitZeroTailPaddingRemainsAllowed() {
		var json = JsonParser.parseString("""
				{"enabled":true,"maximumSeconds":25,"tailPaddingMillis":0,
				 "noSpeechTimeoutMillis":5000,"endpointSilenceMillis":800}
				""").getAsJsonObject();
		assertEquals(0, VoiceSettingsStorage.decode(json).tailPaddingMillis());
	}
}
