package io.github.ikunkk02.chatcanvas.voice;

import com.k2fsa.sherpa.onnx.EndpointConfig;
import com.k2fsa.sherpa.onnx.EndpointRule;
import com.k2fsa.sherpa.onnx.FeatureConfig;
import com.k2fsa.sherpa.onnx.OnlineModelConfig;
import com.k2fsa.sherpa.onnx.OnlineRecognizer;
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig;
import com.k2fsa.sherpa.onnx.OnlineStream;
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig;

import java.nio.file.Path;

public final class SherpaOnlineAsrProvider implements AsrProvider {
	private OnlineRecognizer recognizer;

	@Override public String id() { return "sherpa-online"; }

	@Override
	public synchronized void loadModel(VoiceModelDescriptor descriptor, Path root,
								   AsrRuntimeOptions options) {
		unloadModel();
		OnlineTransducerModelConfig transducer = OnlineTransducerModelConfig.builder()
				.setEncoder(root.resolve("encoder-epoch-99-avg-1.int8.onnx").toString())
				.setDecoder(root.resolve("decoder-epoch-99-avg-1.onnx").toString())
				.setJoiner(root.resolve("joiner-epoch-99-avg-1.int8.onnx").toString()).build();
		OnlineModelConfig model = OnlineModelConfig.builder().setTransducer(transducer)
				.setTokens(root.resolve("tokens.txt").toString())
				.setNumThreads(options.inferenceThreads()).setProvider("cpu").setDebug(options.debug()).build();
		EndpointRule endpoint = EndpointRule.builder().setMustContainNonSilence(true)
				.setMinTrailingSilence(0.8f).setMinUtteranceLength(0.0f).build();
		EndpointConfig endpoints = EndpointConfig.builder().setRule1(endpoint).setRule2(endpoint).setRule3(endpoint).build();
		OnlineRecognizerConfig config = OnlineRecognizerConfig.builder()
				.setFeatureConfig(FeatureConfig.builder().setSampleRate(16_000).setFeatureDim(80).build())
				.setOnlineModelConfig(model).setEndpointConfig(endpoints).setEnableEndpoint(true)
				.setDecodingMethod("greedy_search").build();
		recognizer = new OnlineRecognizer(config);
	}

	@Override public synchronized void unloadModel() { if (recognizer != null) { recognizer.release(); recognizer = null; } }
	@Override public synchronized boolean isLoaded() { return recognizer != null; }
	@Override public boolean supportsStreaming() { return true; }
	@Override public boolean suppliesEndpoint() { return true; }

	@Override
	public synchronized AsrSession createSession() {
		if (recognizer == null) throw new IllegalStateException("Sherpa online model is not loaded");
		return new Session(recognizer, recognizer.createStream());
	}

	private static final class Session implements AsrSession {
		private final OnlineRecognizer recognizer;
		private OnlineStream stream;
		private String latest = "";
		Session(OnlineRecognizer recognizer, OnlineStream stream) { this.recognizer = recognizer; this.stream = stream; }

		@Override public AsrAcceptResult acceptAudio(byte[] pcm16Le, int length) {
			if (stream == null) return AsrAcceptResult.EMPTY;
			stream.acceptWaveform(PcmFloatConverter.convert(pcm16Le, length), 16_000);
			while (recognizer.isReady(stream)) recognizer.decode(stream);
			latest = recognizer.getResult(stream).getText();
			return new AsrAcceptResult(latest, !latest.isBlank(), recognizer.isEndpoint(stream));
		}

		@Override public String finish() {
			if (stream == null) return latest;
			stream.inputFinished();
			while (recognizer.isReady(stream)) recognizer.decode(stream);
			latest = recognizer.getResult(stream).getText();
			return latest;
		}

		@Override public void close() { if (stream != null) { stream.release(); stream = null; } }
	}
}
