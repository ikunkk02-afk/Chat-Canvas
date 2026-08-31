package io.github.ikunkk02.chatcanvas.voice;

import io.github.ikunkk02.chatcanvas.ChatCanvas;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

final class FallbackAudioCaptureBackend implements AudioCaptureBackend {
	private final List<AudioCaptureBackend> candidates;
	private final Throwable terminalFailure;
	private final AtomicBoolean stopRequested = new AtomicBoolean();
	private volatile AudioCaptureBackend active;
	private volatile CaptureCapabilities capabilities;
	private volatile Throwable lastError;

	FallbackAudioCaptureBackend(List<AudioCaptureBackend> candidates) {
		this(candidates, null);
	}

	FallbackAudioCaptureBackend(List<AudioCaptureBackend> candidates, Throwable terminalFailure) {
		this.candidates = List.copyOf(candidates);
		this.terminalFailure = terminalFailure;
		this.capabilities = candidates.stream().map(AudioCaptureBackend::capabilities)
				.filter(CaptureCapabilities::available).findFirst()
				.orElse(CaptureCapabilities.unavailable("none", "Unavailable",
						"chat_canvas.voice.error.microphone"));
	}

	@Override public CaptureCapabilities capabilities() { return capabilities; }

	@Override
	public void start(String deviceId, AudioCallback callback) throws Exception {
		stopRequested.set(false);
		for (AudioCaptureBackend candidate : candidates) {
			if (stopRequested.get()) break;
			if (!candidate.isAvailable()) continue;
			active = candidate;
			capabilities = candidate.capabilities();
			try {
				ChatCanvas.LOGGER.info("Trying voice audio capture backend: {}", capabilities.displayName());
				candidate.start(deviceId, callback);
				return;
			} catch (Throwable throwable) {
				lastError = throwable;
				candidate.close();
				if (stopRequested.get()) return;
				ChatCanvas.LOGGER.warn("Voice capture backend {} failed; trying fallback",
						capabilities.displayName(), throwable);
			}
		}
		Throwable failure = terminalFailure != null ? terminalFailure : lastError == null
				? new IllegalStateException(capabilities.unavailableReason()) : lastError;
		if (failure instanceof Exception exception) throw exception;
		throw new IllegalStateException("No audio capture backend could be started", failure);
	}

	@Override
	public void stop() {
		stopRequested.set(true);
		AudioCaptureBackend current = active;
		if (current != null) current.stop();
	}

	@Override public Throwable getLastError() { return lastError; }

	@Override
	public void close() {
		stop();
		for (AudioCaptureBackend candidate : candidates) {
			try { candidate.close(); } catch (Throwable ignored) { }
		}
	}
}
