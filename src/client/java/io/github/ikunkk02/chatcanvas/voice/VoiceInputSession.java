package io.github.ikunkk02.chatcanvas.voice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class VoiceInputSession {
	private static final Chunk END = new Chunk(new byte[0], 0, true);
	private final long token;
	private final MicrophoneManager.Lease microphone;
	private final RecognitionSession recognizer;
	private final ExecutorService recognitionExecutor;
	private final ArrayBlockingQueue<Chunk> queue = new ArrayBlockingQueue<>(8);
	private final AudioLevelMeter meter = new AudioLevelMeter();
	private final VoiceSettings settings;
	private final Consumer<String> partialConsumer;
	private final Consumer<VoiceRecognitionResult> completion;
	private final Consumer<Throwable> failure;
	private final Runnable automaticLimitReached;
	private final AtomicBoolean cancelled = new AtomicBoolean();
	private final AtomicBoolean finishing = new AtomicBoolean();
	private final AtomicBoolean endEnqueued = new AtomicBoolean();
	private volatile double level;
	private volatile Future<?> recognitionFuture;
	private volatile TargetDataLine captureLine;
	private final long startedAt = System.currentTimeMillis();

	public VoiceInputSession(long token, MicrophoneManager.Lease microphone,
							 RecognitionSession recognizer,
							 ExecutorService recognitionExecutor,
							 VoiceSettings settings,
							 Consumer<String> partialConsumer,
							 Consumer<VoiceRecognitionResult> completion,
							 Consumer<Throwable> failure,
							 Runnable automaticLimitReached) {
		this.token = token;
		this.microphone = microphone;
		this.recognizer = recognizer;
		this.recognitionExecutor = recognitionExecutor;
		this.settings = settings;
		this.partialConsumer = partialConsumer;
		this.completion = completion;
		this.failure = failure;
		this.automaticLimitReached = automaticLimitReached;
	}

	public void startCapture() {
		recognitionFuture = recognitionExecutor.submit(this::recognize);
		AudioDeviceManager.OpenedMicrophone opened = microphone.openedOrThrow();
		TargetDataLine line = opened.line();
		AudioFormat format = opened.format();
		captureLine = line;
		Pcm16MonoResampler resampler =
				new Pcm16MonoResampler(format.getSampleRate(), format.getChannels());
		byte[] source = new byte[Math.max(2048, format.getFrameSize() * 2048)];
		try {
			line.start();
			while (!cancelled.get() && !finishing.get()) {
				if (System.currentTimeMillis() - startedAt >= settings.maximumSeconds() * 1000L) {
					finishing.set(true);
					automaticLimitReached.run();
					break;
				}
				int read = line.read(source, 0, source.length);
				if (read <= 0) continue;
				byte[] converted = resampler.convert(source, read, format.isBigEndian());
				if (converted.length == 0) continue;
				long chunkMillis = converted.length * 1000L / 32_000L;
				level = meter.acceptPcm16Le(converted, converted.length,
						settings.noiseThreshold(), chunkMillis);
				if (!queue.offer(new Chunk(converted, converted.length, false),
						500L, TimeUnit.MILLISECONDS)) {
					throw new IllegalStateException("Voice audio queue is full");
				}
			}
		} catch (Throwable throwable) {
			if (!cancelled.get() && !finishing.get()) failure.accept(throwable);
			cancelled.set(true);
		} finally {
			captureLine = null;
			microphone.close();
			enqueueEndOnce();
		}
	}

	private void recognize() {
		String finalText = "";
		try (recognizer) {
			while (true) {
				Chunk chunk = queue.take();
				if (chunk.end()) break;
				if (cancelled.get()) continue;
				String partial = recognizer.accept(chunk.bytes(), chunk.length());
				if (!partial.isBlank()) partialConsumer.accept(partial);
			}
			if (!cancelled.get() && meter.hasClearSpeech()) finalText = recognizer.finish();
			if (!cancelled.get()) {
				completion.accept(new VoiceRecognitionResult(
						finalText, finalText.isBlank(),
						System.currentTimeMillis() - startedAt));
			}
		} catch (Throwable throwable) {
			if (!cancelled.get()) failure.accept(throwable);
		} finally {
			queue.clear();
		}
	}

	public void finish() {
		if (!finishing.compareAndSet(false, true)) {
			return;
		}
		stopCaptureLine();
	}

	public void cancel() {
		if (!cancelled.compareAndSet(false, true)) {
			return;
		}
		finishing.set(true);
		stopCaptureLine();
		queue.clear();
		queue.offer(END);
		Future<?> future = recognitionFuture;
		if (future != null) future.cancel(true);
	}

	private void stopCaptureLine() {
		TargetDataLine line = captureLine;
		if (line == null) return;
		try {
			if (line.isRunning()) {
				line.stop();
			}
		} catch (RuntimeException ignored) {
		}
		try {
			line.close();
		} catch (RuntimeException ignored) {
		}
	}

	public long token() { return token; }
	public double level() { return level; }
	public boolean finishing() { return finishing.get(); }

	private void enqueueEndOnce() {
		if (!endEnqueued.compareAndSet(false, true)) {
			return;
		}
		while (true) {
			try {
				if (queue.offer(END, 500L, TimeUnit.MILLISECONDS)) return;
				if (cancelled.get()) queue.clear();
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				queue.clear();
				queue.offer(END);
				return;
			}
		}
	}

	private record Chunk(byte[] bytes, int length, boolean end) {
		private Chunk {
			bytes = Arrays.copyOf(bytes, length);
		}
	}
}
