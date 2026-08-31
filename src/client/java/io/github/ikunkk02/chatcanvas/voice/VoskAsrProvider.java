package io.github.ikunkk02.chatcanvas.voice;

import com.google.gson.Gson;
import org.vosk.Model;
import org.vosk.Recognizer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class VoskAsrProvider implements AsrProvider {
	private static final VoskResultParser RESULTS = new VoskResultParser(new Gson());
	private Model model;

	@Override public String id() { return "vosk"; }

	@Override
	public synchronized void loadModel(VoiceModelDescriptor descriptor, Path modelPath,
								   AsrRuntimeOptions options) throws Exception {
		VoskEncodingBootstrap.verifyBeforeVoskInitialization();
		unloadModel();
		model = new Model(modelPath.toAbsolutePath().toString());
	}

	@Override public synchronized void unloadModel() { if (model != null) { model.close(); model = null; } }
	@Override public synchronized boolean isLoaded() { return model != null; }
	@Override public boolean supportsStreaming() { return true; }
	@Override public boolean suppliesEndpoint() { return true; }

	@Override
	public synchronized AsrSession createSession() throws Exception {
		if (model == null) throw new IllegalStateException("Vosk model is not loaded");
		return new Session(new Recognizer(model, 16_000.0f));
	}

	private static final class Session implements AsrSession {
		private Recognizer recognizer;
		private final List<String> completed = new ArrayList<>();

		private Session(Recognizer recognizer) { this.recognizer = recognizer; }

		@Override
		public AsrAcceptResult acceptAudio(byte[] pcm16Le, int length) {
			if (recognizer == null) return AsrAcceptResult.EMPTY;
			boolean endpoint = recognizer.acceptWaveForm(pcm16Le, length);
			String text;
			if (endpoint) {
				text = RESULTS.parseResult(recognizer.getResult()).strip();
				if (!text.isBlank()) completed.add(text);
			} else {
				text = RESULTS.parsePartial(recognizer.getPartialResult()).strip();
			}
			String aggregate = join(text, endpoint);
			return new AsrAcceptResult(aggregate, !aggregate.isBlank(), endpoint && !aggregate.isBlank());
		}

		private String join(String current, boolean alreadyCompleted) {
			List<String> values = new ArrayList<>(completed);
			if (!alreadyCompleted && current != null && !current.isBlank()) values.add(current);
			return String.join(" ", values).strip();
		}

		@Override
		public String finish() {
			if (recognizer == null) return String.join(" ", completed).strip();
			String tail = RESULTS.parseFinal(recognizer.getFinalResult()).strip();
			if (!tail.isBlank()) completed.add(tail);
			return String.join(" ", completed).strip();
		}

		@Override public void close() { if (recognizer != null) { recognizer.close(); recognizer = null; } }
	}
}
