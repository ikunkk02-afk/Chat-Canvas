package io.github.ikunkk02.chatcanvas.voice;

import com.k2fsa.sherpa.onnx.VadModelConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SileroVadProcessorTest {
	@Test
	void createsNativeValidSileroDurations() {
		VadModelConfig config = SileroVadProcessor.createModelConfig(
				Path.of("silero_vad.int8.onnx"), 25, 0);

		assertEquals(16_000, config.getSampleRate());
		assertEquals(1, config.getNumThreads());
		assertEquals(512, config.getSileroVadModelConfig().getWindowSize());
		assertEquals(25.0f, config.getSileroVadModelConfig().getMaxSpeechDuration());
		assertTrue(config.getSileroVadModelConfig().getMinSilenceDuration() > 0.0f);
		assertTrue(config.getSileroVadModelConfig().getMinSpeechDuration() > 0.0f);
	}
}
