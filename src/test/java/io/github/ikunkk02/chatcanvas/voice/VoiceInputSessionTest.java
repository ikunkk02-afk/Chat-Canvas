package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


class VoiceInputSessionTest {
	@Test
	void endpointStopsCaptureBeforeAsrFinishAndCommitsFinalResult() throws Exception {
		List<String> order = new CopyOnWriteArrayList<>();
		FakeCapture capture = new FakeCapture(order, 2);
		FakeAsr asr = new FakeAsr(order, 2, "你好，去挖矿吧");
		ResultListener listener = new ResultListener();
		ExecutorService recognition = Executors.newSingleThreadExecutor();
		ExecutorService audio = Executors.newSingleThreadExecutor();
		try {
			VoiceInputSession session = new VoiceInputSession(1L, capture, asr, null,
					recognition, VoiceSettings.DEFAULT, listener);
			audio.execute(session::startCapture);
			assertTrue(listener.completed.await(5, TimeUnit.SECONDS));
			assertEquals("你好，去挖矿吧", listener.result.get().text());
			assertEquals(VoiceFinishReason.ENDPOINT, listener.result.get().reason());
			assertTrue(order.indexOf("stop") >= 0);
			assertTrue(order.indexOf("finish") > order.indexOf("stop"), order.toString());
			assertEquals(1, asr.finishCalls.get());
		} finally {
			audio.shutdownNow();
			recognition.shutdownNow();
		}
	}

	@Test
	void manualFinalizeIsIdempotent() throws Exception {
		List<String> order = new CopyOnWriteArrayList<>();
		FakeCapture capture = new FakeCapture(order, 1);
		FakeAsr asr = new FakeAsr(order, Integer.MAX_VALUE, "手动结束");
		ResultListener listener = new ResultListener();
		ExecutorService recognition = Executors.newSingleThreadExecutor();
		ExecutorService audio = Executors.newSingleThreadExecutor();
		try {
			VoiceInputSession session = new VoiceInputSession(2L, capture, asr, null,
					recognition, VoiceSettings.DEFAULT, listener);
			audio.execute(session::startCapture);
			assertTrue(capture.started.await(2, TimeUnit.SECONDS));
			assertTrue(session.requestFinish(VoiceFinishReason.MANUAL));
			assertFalse(session.requestFinish(VoiceFinishReason.MANUAL));
			assertTrue(listener.completed.await(5, TimeUnit.SECONDS));
			assertEquals(1, asr.finishCalls.get());
			assertEquals(VoiceFinishReason.MANUAL, listener.result.get().reason());
		} finally {
			audio.shutdownNow();
			recognition.shutdownNow();
		}
	}

	@Test
	void noSpeechTimeoutStopsWithoutCallingAsrFinish() throws Exception {
		List<String> order = new CopyOnWriteArrayList<>();
		FakeCapture capture = new FakeCapture(order, 0);
		FakeAsr asr = new FakeAsr(order, Integer.MAX_VALUE, "");
		ResultListener listener = new ResultListener();
		ExecutorService recognition = Executors.newSingleThreadExecutor();
		ExecutorService audio = Executors.newSingleThreadExecutor();
		try {
			VoiceSettings settings = VoiceSettings.DEFAULT.withEndpointTiming(1_000, 800, 200);
			VoiceInputSession session = new VoiceInputSession(3L, capture, asr, null,
					recognition, settings, listener);
			audio.execute(session::startCapture);
			assertTrue(capture.started.await(2, TimeUnit.SECONDS));
			session.tick(System.currentTimeMillis() + 2_000L);
			assertTrue(listener.completed.await(5, TimeUnit.SECONDS));
			assertEquals(VoiceFinishReason.NO_SPEECH, listener.result.get().reason());
			assertEquals(0, asr.finishCalls.get());
		} finally {
			audio.shutdownNow();
			recognition.shutdownNow();
		}
	}

	@Test
	void tenSequentialSessionsReleaseRecognizerAndReturnFinalText() throws Exception {
		ExecutorService recognition = Executors.newSingleThreadExecutor();
		ExecutorService audio = Executors.newSingleThreadExecutor();
		try {
			for (int index = 0; index < 10; index++) {
				List<String> order = new CopyOnWriteArrayList<>();
				FakeCapture capture = new FakeCapture(order, 1);
				FakeAsr asr = new FakeAsr(order, 1, "session-" + index);
				ResultListener listener = new ResultListener();
				VoiceInputSession session = new VoiceInputSession(100L + index, capture, asr, null,
						recognition, VoiceSettings.DEFAULT, listener);

				audio.execute(session::startCapture);
				assertTrue(listener.completed.await(5, TimeUnit.SECONDS), "session " + index);
				assertTrue(session.awaitClosed(2, TimeUnit.SECONDS), "close " + index);
				assertEquals("session-" + index, listener.result.get().text());
				assertEquals(1, asr.finishCalls.get());
				assertEquals(1, asr.closeCalls.get());
				assertTrue(order.indexOf("finish") > order.indexOf("stop"), order.toString());
			}
		} finally {
			audio.shutdownNow();
			recognition.shutdownNow();
		}
	}

	@Test
	void neuralVadKeepsOnlyConfiguredTailBeforeEndpoint() throws Exception {
		List<String> order = new CopyOnWriteArrayList<>();
		FakeCapture capture = new FakeCapture(order, 9);
		FakeAsr asr = new FakeAsr(order, Integer.MAX_VALUE, "尾音完整");
		FakeVad vad = new FakeVad();
		ResultListener listener = new ResultListener();
		ExecutorService recognition = Executors.newSingleThreadExecutor();
		ExecutorService audio = Executors.newSingleThreadExecutor();
		try {
			VoiceSettings settings = VoiceSettings.DEFAULT.withEndpointTiming(5_000, 800, 200);
			VoiceInputSession session = new VoiceInputSession(4L, capture, asr, vad,
					recognition, settings, listener);
			audio.execute(session::startCapture);

			assertTrue(listener.completed.await(5, TimeUnit.SECONDS));
			assertEquals(VoiceFinishReason.ENDPOINT, listener.result.get().reason());
			assertEquals(9, vad.accepts.get(), "VAD must observe the full 800ms endpoint silence");
			assertEquals(3, asr.accepts.get(), "ASR receives speech plus exactly 200ms of tail audio");
			assertEquals(1, vad.closeCalls.get());
		} finally {
			audio.shutdownNow();
			recognition.shutdownNow();
		}
	}

	@Test
	void maximumDurationFinalizesAnActiveUtterance() throws Exception {
		List<String> order = new CopyOnWriteArrayList<>();
		FakeCapture capture = new FakeCapture(order, 1);
		FakeAsr asr = new FakeAsr(order, Integer.MAX_VALUE, "达到上限");
		ResultListener listener = new ResultListener();
		ExecutorService recognition = Executors.newSingleThreadExecutor();
		ExecutorService audio = Executors.newSingleThreadExecutor();
		try {
			VoiceSettings settings = VoiceSettings.DEFAULT.withMaximumSeconds(5);
			VoiceInputSession session = new VoiceInputSession(5L, capture, asr, null,
					recognition, settings, listener);
			audio.execute(session::startCapture);
			assertTrue(capture.started.await(2, TimeUnit.SECONDS));
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
			while (!session.speechDetected() && System.nanoTime() < deadline) Thread.onSpinWait();
			assertTrue(session.speechDetected());
			session.tick(System.currentTimeMillis() + 6_000L);

			assertTrue(listener.completed.await(5, TimeUnit.SECONDS));
			assertEquals(VoiceFinishReason.MAXIMUM_DURATION, listener.result.get().reason());
			assertEquals(1, asr.finishCalls.get());
		} finally {
			audio.shutdownNow();
			recognition.shutdownNow();
		}
	}

	private static final class FakeCapture implements AudioCaptureBackend {
		private final List<String> order;
		private final int chunks;
		private final AtomicBoolean running = new AtomicBoolean();
		private final CountDownLatch started = new CountDownLatch(1);
		FakeCapture(List<String> order, int chunks) { this.order = order; this.chunks = chunks; }
		@Override public CaptureCapabilities capabilities() {
			return new CaptureCapabilities(true, "fake", "Fake", 16_000, 1, true, "", List.of());
		}
		@Override public void start(String deviceId, AudioCallback callback) throws Exception {
			running.set(true);
			started.countDown();
			for (int i = 0; i < chunks && running.get(); i++) callback.onPcm16Mono16Khz(new byte[3_200]);
			while (running.get()) Thread.onSpinWait();
		}
		@Override public void stop() { if (running.compareAndSet(true, false)) order.add("stop"); }
		@Override public Throwable getLastError() { return null; }
		@Override public void close() { stop(); }
	}

	private static final class FakeAsr implements AsrSession {
		private final List<String> order;
		private final int endpointAt;
		private final String result;
		private final AtomicInteger accepts = new AtomicInteger();
		private final AtomicInteger finishCalls = new AtomicInteger();
		private final AtomicInteger closeCalls = new AtomicInteger();
		FakeAsr(List<String> order, int endpointAt, String result) {
			this.order = order; this.endpointAt = endpointAt; this.result = result;
		}
		@Override public AsrAcceptResult acceptAudio(byte[] pcm16Le, int length) {
			int count = accepts.incrementAndGet();
			return new AsrAcceptResult(count >= 1 ? "partial" : "", count >= 1, count >= endpointAt);
		}
		@Override public String finish() { finishCalls.incrementAndGet(); order.add("finish"); return result; }
		@Override public void close() { closeCalls.incrementAndGet(); order.add("asr-close"); }
	}

	private static final class FakeVad implements VadProcessor {
		private final AtomicInteger accepts = new AtomicInteger();
		private final AtomicInteger closeCalls = new AtomicInteger();
		@Override public VadDecision accept(byte[] pcm16Le, int length) {
			int count = accepts.incrementAndGet();
			if (count == 1) return new VadDecision(true, true, false, false);
			if (count == 9) return new VadDecision(false, false, true, true);
			return new VadDecision(false, false, true, false);
		}
		@Override public void reset() { }
		@Override public void close() { closeCalls.incrementAndGet(); }
	}

	private static final class ResultListener implements VoiceInputSession.Listener {
		private final CountDownLatch completed = new CountDownLatch(1);
		private final AtomicReference<VoiceRecognitionResult> result = new AtomicReference<>();
		@Override public void state(VoiceInputState state) { }
		@Override public void partial(String text) { }
		@Override public void completed(VoiceRecognitionResult result) {
			this.result.set(result);
			completed.countDown();
		}
		@Override public void failure(Throwable throwable) { fail(throwable); }
	}
}
