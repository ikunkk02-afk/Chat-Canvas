package io.github.ikunkk02.chatcanvas.voice;

import io.github.ikunkk02.chatcanvas.ChatCanvas;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class VoiceInputSession {
	private static final Chunk END = new Chunk(new byte[0], true);
	private final long token;
	private final AudioCaptureBackend capture;
	private final AsrSession recognizer;
	private final VadProcessor vad;
	private final ExecutorService recognitionExecutor;
	private final VoiceSettings settings;
	private final Listener listener;
	private final ArrayBlockingQueue<Chunk> queue = new ArrayBlockingQueue<>(32);
	private final AudioLevelMeter meter = new AudioLevelMeter();
	private final AtomicBoolean cancelled = new AtomicBoolean();
	private final AtomicBoolean endEnqueued = new AtomicBoolean();
	private final AtomicBoolean failureReported = new AtomicBoolean();
	private final AtomicBoolean resourcesClosed = new AtomicBoolean();
	private final AtomicReference<VoiceFinishReason> finishReason = new AtomicReference<>();
	private final CountDownLatch recognitionClosed = new CountDownLatch(1);
	private final long startedAt = System.currentTimeMillis();
	private volatile long finishRequestedAt;
	private volatile boolean speechDetected;
	private volatile long trailingSilenceMillis;
	private volatile VoiceInputState recognitionState = VoiceInputState.WAITING_FOR_SPEECH;
	private volatile double level;
	private volatile Future<?> recognitionFuture;

	public VoiceInputSession(long token, AudioCaptureBackend capture, AsrSession recognizer,
							 VadProcessor vad, ExecutorService recognitionExecutor,
							 VoiceSettings settings, Listener listener) {
		this.token = token;
		this.capture = capture;
		this.recognizer = recognizer;
		this.vad = vad;
		this.recognitionExecutor = recognitionExecutor;
		this.settings = settings;
		this.listener = listener;
	}

	/** Runs on the dedicated capture worker. */
	public void startCapture() {
		if (cancelled.get()) {
			closeUnstartedResources();
			return;
		}
		try {
			recognitionFuture = recognitionExecutor.submit(this::recognize);
			listener.state(VoiceInputState.WAITING_FOR_SPEECH);
			capture.start(settings.microphoneId(), pcm -> {
				if (cancelled.get() || finishReason.get() != null || pcm.length == 0) return;
				long millis = pcm.length * 1_000L / 32_000L;
				level = meter.acceptPcm16Le(pcm, pcm.length, settings.noiseThreshold(), millis);
				if (!queue.offer(new Chunk(pcm, false), 300L, TimeUnit.MILLISECONDS)) {
					throw new IllegalStateException("Voice audio queue is full");
				}
			});
			if (!cancelled.get() && finishReason.get() == null) {
				fail(new IllegalStateException("Microphone capture stopped unexpectedly"));
			}
		} catch (Throwable throwable) {
			if (!cancelled.get() && finishReason.get() == null) fail(throwable);
		} finally {
			try { capture.close(); } catch (Throwable ignored) { }
			enqueueEndOnce();
			if (recognitionFuture == null) closeUnstartedResources();
		}
	}

	private void recognize() {
		VoiceRecognitionResult completedResult = null;
		Throwable problem = null;
		try (recognizer; vad) {
			while (true) {
				Chunk chunk = queue.take();
				if (chunk.end()) break;
				if (cancelled.get()) continue;
				VadDecision decision = vad == null ? VadDecision.SILENCE : vad.accept(chunk.bytes(), chunk.bytes().length);
				boolean feedAsr = shouldFeedAsr(decision, chunk.bytes().length * 1_000L / 32_000L);
				AsrAcceptResult asr = feedAsr
						? recognizer.acceptAudio(chunk.bytes(), chunk.bytes().length)
						: AsrAcceptResult.EMPTY;
				if (vad != null) applyVadDecision(decision);
				else applyDecoderDecision(asr);
				if (!asr.partial().isBlank()) listener.partial(asr.partial());
				if (vad == null && asr.endpoint() && asr.speechDetected()) requestFinish(VoiceFinishReason.ENDPOINT);
			}
			if (cancelled.get()) return;
			VoiceFinishReason reason = finishReason.get();
			if (reason == null) throw new IllegalStateException("ASR input ended without a finish reason");
			String text = reason == VoiceFinishReason.NO_SPEECH ? "" : recognizer.finish();
			completedResult = new VoiceRecognitionResult(text, text.isBlank(),
					System.currentTimeMillis() - startedAt, reason);
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			if (!cancelled.get()) problem = interrupted;
		} catch (Throwable throwable) {
			if (!cancelled.get()) problem = throwable;
		} finally {
			queue.clear();
			resourcesClosed.set(true);
			recognitionClosed.countDown();
		}
		// Never publish completion while the recognizer still owns native model state.
		// Model hot-swap may begin as soon as the client receives this callback.
		if (problem != null) fail(problem);
		else if (completedResult != null && !cancelled.get()) listener.completed(completedResult);
	}

	private boolean shouldFeedAsr(VadDecision decision, long chunkMillis) {
		if (vad == null || !speechDetected) return true;
		if (decision.speechActive() || decision.speechStarted()) {
			trailingSilenceMillis = 0L;
			return true;
		}
		if (decision.waitingForEndpoint() || decision.endpoint()) {
			long before = trailingSilenceMillis;
			trailingSilenceMillis += Math.max(0L, chunkMillis);
			return before < settings.tailPaddingMillis();
		}
		return true;
	}

	private void applyVadDecision(VadDecision decision) {
		if (decision.speechStarted()) {
			speechDetected = true;
			setRecognitionState(VoiceInputState.SPEAKING);
			ChatCanvas.LOGGER.info("Voice session {}: speech detected", token);
		} else if (decision.speechActive() && speechDetected) {
			setRecognitionState(VoiceInputState.SPEAKING);
		} else if (decision.waitingForEndpoint() && speechDetected) {
			setRecognitionState(VoiceInputState.WAITING_FOR_ENDPOINT);
		}
		if (decision.endpoint()) {
			ChatCanvas.LOGGER.info("Voice session {}: endpoint detected", token);
			requestFinish(VoiceFinishReason.ENDPOINT);
		}
	}

	private void applyDecoderDecision(AsrAcceptResult result) {
		if (result.speechDetected() && !speechDetected) {
			speechDetected = true;
			setRecognitionState(VoiceInputState.SPEAKING);
			ChatCanvas.LOGGER.info("Voice session {}: decoder speech detected", token);
		}
	}

	private void setRecognitionState(VoiceInputState next) {
		if (recognitionState == next || finishReason.get() != null) return;
		recognitionState = next;
		listener.state(next);
	}

	public void tick(long now) {
		if (cancelled.get()) return;
		VoiceFinishReason reason = finishReason.get();
		if (reason == null) {
			long elapsed = now - startedAt;
			if (!speechDetected && elapsed >= settings.noSpeechTimeoutMillis()) {
				requestFinish(VoiceFinishReason.NO_SPEECH);
			} else if (elapsed >= settings.maximumSeconds() * 1_000L) {
				requestFinish(VoiceFinishReason.MAXIMUM_DURATION);
			}
			return;
		}
		long finishingFor = now - finishRequestedAt;
		if (finishingFor >= 2_500L) enqueueEndOnce();
		if (finishingFor >= 8_000L && failureReported.compareAndSet(false, true)) {
			cancelled.set(true);
			listener.failure(new IllegalStateException("Voice finalization timed out"));
		}
	}

	public boolean requestFinish(VoiceFinishReason reason) {
		if (cancelled.get() || !finishReason.compareAndSet(null, reason)) return false;
		finishRequestedAt = System.currentTimeMillis();
		listener.state(VoiceInputState.FINALIZING);
		ChatCanvas.LOGGER.info("Voice session {}: finalize ({})", token, reason);
		capture.stop();
		return true;
	}

	public void cancel() {
		if (!cancelled.compareAndSet(false, true)) return;
		finishReason.compareAndSet(null, VoiceFinishReason.CANCELLED);
		capture.stop();
		queue.clear();
		enqueueEndOnce();
		Future<?> future = recognitionFuture;
		if (future == null) {
			closeUnstartedResources();
		} else if (future.isDone()) {
			future.cancel(false);
		}
	}

	private void fail(Throwable throwable) {
		if (!failureReported.compareAndSet(false, true)) return;
		cancelled.set(true);
		finishReason.compareAndSet(null, VoiceFinishReason.ERROR);
		try { capture.stop(); } catch (Throwable ignored) { }
		queue.clear();
		enqueueEndOnce();
		listener.failure(throwable);
	}

	private void enqueueEndOnce() {
		if (!endEnqueued.compareAndSet(false, true)) return;
		while (!queue.offer(END)) queue.poll();
	}

	public long token() { return token; }
	public double level() { return level; }
	public boolean finishing() { return finishReason.get() != null; }
	public boolean speechDetected() { return speechDetected; }
	public boolean awaitClosed(long timeout, TimeUnit unit) {
		try { return recognitionClosed.await(timeout, unit); }
		catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	private void closeUnstartedResources() {
		if (!resourcesClosed.compareAndSet(false, true)) return;
		try { recognizer.close(); } catch (Throwable ignored) { }
		if (vad != null) try { vad.close(); } catch (Throwable ignored) { }
		try { capture.close(); } catch (Throwable ignored) { }
		recognitionClosed.countDown();
	}

	private record Chunk(byte[] bytes, boolean end) {
		private Chunk { bytes = Arrays.copyOf(bytes, bytes.length); }
	}

	public interface Listener {
		void state(VoiceInputState state);
		void partial(String text);
		void completed(VoiceRecognitionResult result);
		void failure(Throwable throwable);
	}
}
