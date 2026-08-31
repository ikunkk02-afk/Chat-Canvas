package io.github.ikunkk02.chatcanvas.voice;

import com.k2fsa.sherpa.onnx.SileroVadModelConfig;
import com.k2fsa.sherpa.onnx.Vad;
import com.k2fsa.sherpa.onnx.VadModelConfig;

import java.nio.file.Path;

public final class SileroVadProcessor implements VadProcessor {
	private static final int WINDOW_SAMPLES = 512;
	private static final float MIN_SILENCE_DURATION_SECONDS = 0.25f;
	private static final float MIN_SPEECH_DURATION_SECONDS = 0.25f;
	private final Vad vad;
	private final EndpointTracker tracker;
	private final float[] window = new float[WINDOW_SAMPLES];
	private int windowLength;

	public SileroVadProcessor(Path model, VoiceSettings settings, int threads) {
		vad = new Vad(createModelConfig(model, settings.maximumSeconds(), threads));
		tracker = new EndpointTracker(250L, settings.endpointSilenceMillis());
	}

	static VadModelConfig createModelConfig(Path model, int maximumSeconds, int threads) {
		SileroVadModelConfig silero = SileroVadModelConfig.builder()
				.setModel(model.toAbsolutePath().toString())
				.setThreshold(0.5f)
				.setMinSilenceDuration(MIN_SILENCE_DURATION_SECONDS)
				.setMinSpeechDuration(MIN_SPEECH_DURATION_SECONDS)
				.setWindowSize(WINDOW_SAMPLES)
				.setMaxSpeechDuration(maximumSeconds)
				.build();
		return VadModelConfig.builder()
				.setSileroVadModelConfig(silero)
				.setSampleRate(16_000)
				.setNumThreads(Math.max(1, threads))
				.setProvider("cpu")
				.setDebug(false)
				.build();
	}

	@Override
	public VadDecision accept(byte[] pcm16Le, int length) {
		float[] samples = PcmFloatConverter.convert(pcm16Le, length);
		VadDecision combined = VadDecision.SILENCE;
		for (float sample : samples) {
			window[windowLength++] = sample;
			if (windowLength == window.length) {
				float probability = vad.compute(window.clone());
				VadDecision decision = tracker.accept(probability >= 0.5f, 32L);
				combined = merge(combined, decision);
				windowLength = 0;
			}
		}
		return combined;
	}

	private static VadDecision merge(VadDecision left, VadDecision right) {
		boolean endpoint = right.speechActive() || right.speechStarted()
				? false : left.endpoint() || right.endpoint();
		return new VadDecision(left.speechStarted() || right.speechStarted(),
				right.speechActive(), right.waitingForEndpoint(), endpoint);
	}

	@Override public void reset() { vad.reset(); tracker.reset(); windowLength = 0; }
	@Override public void close() { vad.release(); }
}
