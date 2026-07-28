package io.github.ikunkk02.chatcanvas.voice;

import com.google.gson.Gson;
import io.github.ikunkk02.chatcanvas.ChatCanvasForge;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.nio.file.Path;

public final class VoskSpeechRecognitionBackend implements SpeechRecognitionBackend {
	private static final VoskResultParser RESULTS = new VoskResultParser(new Gson());
	private Model model;

	@Override
	public synchronized void initialize(Path modelPath) throws Exception {
		VoskEncodingBootstrap.verifyBeforeVoskInitialization();
		close();
		model = new Model(modelPath.toAbsolutePath().toString());
	}

	@Override
	public synchronized RecognitionSession createSession(float sampleRate) throws Exception {
		VoskEncodingBootstrap.verifyBeforeVoskInitialization();
		if (model == null) throw new IllegalStateException("Vosk model is not loaded");
		return new Session(new Recognizer(model, sampleRate));
	}

	@Override
	public synchronized boolean isReady() {
		return model != null;
	}

	@Override
	public synchronized void close() {
		if (model != null) {
			model.close();
			model = null;
		}
	}

	private static final class Session implements RecognitionSession {
		private Recognizer recognizer;

		private Session(Recognizer recognizer) {
			this.recognizer = recognizer;
		}

		@Override
		public String accept(byte[] pcm, int length) {
			if (recognizer == null) return "";
			boolean complete = recognizer.acceptWaveForm(pcm, length);
			String rawJson = complete
					? recognizer.getResult()
					: recognizer.getPartialResult();
			String text = complete
					? RESULTS.parseResult(rawJson)
					: RESULTS.parsePartial(rawJson);
			logResult(complete ? "result" : "partial", rawJson, text);
			return text;
		}

		@Override
		public String finish() {
			if (recognizer == null) return "";
			String rawJson = recognizer.getFinalResult();
			String text = RESULTS.parseFinal(rawJson);
			logResult("final", rawJson, text);
			return text;
		}

		@Override
		public void close() {
			if (recognizer != null) {
				recognizer.close();
				recognizer = null;
			}
		}

		private static void logResult(String stage, String rawJson, String text) {
			if (!VoiceEncodingDiagnostics.enabled()) return;
			ChatCanvasForge.LOGGER.debug("Vosk raw {} JSON: {}", stage, rawJson);
			ChatCanvasForge.LOGGER.debug("Parsed {} text: {}", stage, text);
			ChatCanvasForge.LOGGER.debug("Parsed {} code points: {}", stage,
					VoiceEncodingDiagnostics.describeCodePoints(text));
		}
	}
}
