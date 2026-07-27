package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.TargetDataLine;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MicrophoneManagerLeaseConcurrencyTest {

	@Test
	void repeatedCloseIsIdempotent() {
		CountingLine countingLine = new CountingLine();
		AudioDeviceManager.OpenedMicrophone opened =
				new AudioDeviceManager.OpenedMicrophone(
						countingLine, AudioDeviceManager.TARGET_FORMAT, "test");
		AtomicBoolean occupied = new AtomicBoolean(true);
		MicrophoneManager.Lease lease = new MicrophoneManager.Lease(opened, occupied);

		lease.close();
		lease.close();
		lease.close();

		assertEquals(1, countingLine.stopCount(), "stop should only be called once");
		assertEquals(1, countingLine.flushCount(), "flush should only be called once");
		assertEquals(1, countingLine.closeCount(), "close should only be called once");
		assertFalse(occupied.get(), "occupied must be released");
	}

	@Test
	void concurrentCloseOneHundredRounds() throws InterruptedException {
		for (int round = 0; round < 100; round++) {
			CountingLine countingLine = new CountingLine();
			AudioDeviceManager.OpenedMicrophone opened =
					new AudioDeviceManager.OpenedMicrophone(
							countingLine, AudioDeviceManager.TARGET_FORMAT, "test");
			AtomicBoolean occupied = new AtomicBoolean(true);
			MicrophoneManager.Lease lease = new MicrophoneManager.Lease(opened, occupied);

			ExecutorService executor = Executors.newFixedThreadPool(4);
			CountDownLatch latch = new CountDownLatch(4);

			for (int t = 0; t < 4; t++) {
				executor.execute(() -> {
					try {
						lease.close();
					} finally {
						latch.countDown();
					}
				});
			}

			assertTrue(latch.await(5, TimeUnit.SECONDS), "threads should complete");
			executor.shutdown();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

			assertEquals(1, countingLine.closeCount(),
					"Round " + round + ": close should only be called once");
			assertFalse(occupied.get(),
					"Round " + round + ": occupied must be released");
		}
	}

	@Test
	void openedOrThrowWorksAndThrowsAfterClose() {
		CountingLine countingLine = new CountingLine();
		AudioDeviceManager.OpenedMicrophone opened =
				new AudioDeviceManager.OpenedMicrophone(
						countingLine, AudioDeviceManager.TARGET_FORMAT, "test");
		AtomicBoolean occupied = new AtomicBoolean(true);
		MicrophoneManager.Lease lease = new MicrophoneManager.Lease(opened, occupied);

		var result = lease.openedOrThrow();
		assertNotNull(result);
		assertEquals("test", result.displayName());

		lease.close();

		assertThrows(IllegalStateException.class, lease::openedOrThrow,
				"openedOrThrow after close should throw");
	}

	@Test
	void closeLineSurvivesIndividualStepFailures() {
		FailingLine failingLine = new FailingLine();
		AudioDeviceManager.OpenedMicrophone opened =
				new AudioDeviceManager.OpenedMicrophone(
						failingLine, AudioDeviceManager.TARGET_FORMAT, "test");
		AtomicBoolean occupied = new AtomicBoolean(true);
		MicrophoneManager.Lease lease = new MicrophoneManager.Lease(opened, occupied);

		// Should not throw despite failures on stop/flush/close
		assertDoesNotThrow(lease::close);
		assertFalse(occupied.get(), "occupied must be released even on failure");
	}

	/**
	 * A TargetDataLine that counts how many times stop/flush/close are called.
	 */
	private static final class CountingLine extends StubTargetDataLine {
		private final AtomicInteger stopCount = new AtomicInteger();
		private final AtomicInteger flushCount = new AtomicInteger();
		private final AtomicInteger closeCount = new AtomicInteger();

		@Override
		public void stop() {
			stopCount.incrementAndGet();
		}

		@Override
		public void flush() {
			flushCount.incrementAndGet();
		}

		@Override
		public void close() {
			closeCount.incrementAndGet();
		}

		int stopCount() { return stopCount.get(); }
		int flushCount() { return flushCount.get(); }
		int closeCount() { return closeCount.get(); }
	}

	/**
	 * A TargetDataLine that throws on every operation.
	 */
	private static final class FailingLine extends StubTargetDataLine {
		@Override
		public void stop() { throw new RuntimeException("stop failed"); }

		@Override
		public void flush() { throw new RuntimeException("flush failed"); }

		@Override
		public void close() { throw new RuntimeException("close failed"); }
	}

	/**
	 * Minimal stub so we don't have to implement the entire TargetDataLine interface.
	 */
	private static class StubTargetDataLine implements TargetDataLine {
		@Override public void open(AudioFormat format, int bufferSize) {}
		@Override public void open(AudioFormat format) {}
		@Override public int read(byte[] b, int off, int len) { return 0; }
		@Override public void drain() {}
		@Override public void start() {}
		@Override public void stop() {}
		@Override public void flush() {}
		@Override public void close() {}
		@Override public boolean isOpen() { return true; }
		@Override public boolean isRunning() { return true; }
		@Override public boolean isActive() { return true; }
		@Override public AudioFormat getFormat() { return AudioDeviceManager.TARGET_FORMAT; }
		@Override public int getBufferSize() { return 4096; }
		@Override public int available() { return 0; }
		@Override public long getMicrosecondPosition() { return 0; }
		@Override public int getFramePosition() { return 0; }
		@Override public float getLevel() { return 0; }
		@Override public long getLongFramePosition() { return 0; }
		@Override public Line.Info getLineInfo() { return null; }
		@Override public void open() {}
		@Override public void addLineListener(LineListener listener) {}
		@Override public void removeLineListener(LineListener listener) {}
		@Override public Control[] getControls() { return new Control[0]; }
		@Override public Control getControl(Control.Type control) { return null; }
		@Override public boolean isControlSupported(Control.Type control) { return false; }
	}
}
