package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FallbackAudioCaptureBackendTest {
	@Test
	void triesAndroidFallbackWhenOpenAlStartFails() throws Exception {
		FakeBackend openAl = new FakeBackend("openal", true);
		FakeBackend android = new FakeBackend("android_audio_record", false);
		FallbackAudioCaptureBackend backend = new FallbackAudioCaptureBackend(List.of(openAl, android));
		backend.start("", pcm -> { });
		assertEquals(1, openAl.starts.get());
		assertEquals(1, android.starts.get());
		assertEquals("android_audio_record", backend.capabilities().backendId());
	}

	private static final class FakeBackend implements AudioCaptureBackend {
		private final String id;
		private final boolean fail;
		private final AtomicInteger starts = new AtomicInteger();
		FakeBackend(String id, boolean fail) { this.id = id; this.fail = fail; }
		@Override public CaptureCapabilities capabilities() {
			return new CaptureCapabilities(true, id, id, 16_000, 1, true, "", List.of());
		}
		@Override public void start(String deviceId, AudioCallback callback) {
			starts.incrementAndGet();
			if (fail) throw new IllegalStateException(id + " unavailable");
		}
		@Override public void stop() { }
		@Override public Throwable getLastError() { return null; }
		@Override public void close() { }
	}
}
