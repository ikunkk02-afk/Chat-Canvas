package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AudioLevelMeterTest {
	@Test
	void requiresTwoHundredMillisecondsAboveThreshold() {
		AudioLevelMeter meter = new AudioLevelMeter();
		byte[] speech = pcm(8_000);
		meter.acceptPcm16Le(speech, speech.length, 0.01, 100);
		assertFalse(meter.hasClearSpeech());
		meter.acceptPcm16Le(speech, speech.length, 0.01, 100);
		assertTrue(meter.hasClearSpeech());
	}

	private static byte[] pcm(int value) {
		byte[] result = new byte[3_200];
		for (int i = 0; i < result.length; i += 2) {
			result[i] = (byte) value;
			result[i + 1] = (byte) (value >>> 8);
		}
		return result;
	}
}
