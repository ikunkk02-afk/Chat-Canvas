package io.github.ikunkk02.chatcanvas.voice;

import com.k2fsa.sherpa.onnx.FeatureConfig;
import com.k2fsa.sherpa.onnx.OfflineModelConfig;
import com.k2fsa.sherpa.onnx.OfflineRecognizer;
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig;
import com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig;
import com.k2fsa.sherpa.onnx.OfflineStream;
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

public final class SherpaOfflineAsrProvider implements AsrProvider {
	private final VoiceModelProvider type;
	private OfflineRecognizer recognizer;

	public SherpaOfflineAsrProvider(VoiceModelProvider type) {
		if (type != VoiceModelProvider.SHERPA_SENSE_VOICE && type != VoiceModelProvider.SHERPA_WHISPER) {
			throw new IllegalArgumentException("Unsupported offline sherpa model: " + type);
		}
		this.type = type;
	}

	@Override public String id() { return type == VoiceModelProvider.SHERPA_WHISPER ? "sherpa-whisper" : "sherpa-sense-voice"; }

	@Override
	public synchronized void loadModel(VoiceModelDescriptor descriptor, Path root,
								   AsrRuntimeOptions options) {
		unloadModel();
		OfflineModelConfig.Builder model = OfflineModelConfig.builder()
				.setNumThreads(options.inferenceThreads()).setProvider("cpu").setDebug(options.debug());
		if (type == VoiceModelProvider.SHERPA_SENSE_VOICE) {
			model.setSenseVoice(OfflineSenseVoiceModelConfig.builder()
					.setModel(root.resolve("model.int8.onnx").toString())
					.setLanguage("auto").setInverseTextNormalization(true).build())
					.setTokens(root.resolve("tokens.txt").toString());
		} else {
			model.setWhisper(OfflineWhisperModelConfig.builder()
					.setEncoder(root.resolve("tiny-encoder.int8.onnx").toString())
					.setDecoder(root.resolve("tiny-decoder.int8.onnx").toString())
					.setLanguage("").setTask("transcribe").setTailPaddings(-1).build())
					.setTokens(root.resolve("tiny-tokens.txt").toString());
		}
		OfflineRecognizerConfig config = OfflineRecognizerConfig.builder()
				.setFeatureConfig(FeatureConfig.builder().setSampleRate(16_000).setFeatureDim(80).build())
				.setOfflineModelConfig(model.build()).setDecodingMethod("greedy_search").build();
		recognizer = new OfflineRecognizer(config);
	}

	@Override public synchronized void unloadModel() { if (recognizer != null) { recognizer.release(); recognizer = null; } }
	@Override public synchronized boolean isLoaded() { return recognizer != null; }

	@Override
	public synchronized AsrSession createSession() {
		if (recognizer == null) throw new IllegalStateException("Sherpa offline model is not loaded");
		return new Session(recognizer);
	}

	private static final class Session implements AsrSession {
		private static final int MAX_PCM_BYTES = 2_048_000;
		private final OfflineRecognizer recognizer;
		private final ByteArrayOutputStream pcm = new ByteArrayOutputStream(256 * 1024);
		private boolean closed;
		Session(OfflineRecognizer recognizer) { this.recognizer = recognizer; }
		@Override public AsrAcceptResult acceptAudio(byte[] pcm16Le, int length) {
			int accepted = Math.max(0, Math.min(length, pcm16Le.length));
			if (!closed && pcm.size() + accepted > MAX_PCM_BYTES) {
				throw new IllegalStateException("Offline voice session PCM limit exceeded");
			}
			if (!closed) pcm.write(pcm16Le, 0, accepted);
			return AsrAcceptResult.EMPTY;
		}
		@Override public String finish() {
			if (closed) return "";
			OfflineStream stream = recognizer.createStream();
			try {
				byte[] audio = pcm.toByteArray();
				stream.acceptWaveform(PcmFloatConverter.convert(audio, audio.length), 16_000);
				recognizer.decode(stream);
				return recognizer.getResult(stream).getText();
			} finally { stream.release(); }
		}
		@Override public void close() { closed = true; pcm.reset(); }
	}
}
