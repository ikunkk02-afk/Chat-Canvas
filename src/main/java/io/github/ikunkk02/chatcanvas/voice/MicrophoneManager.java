package io.github.ikunkk02.chatcanvas.voice;

import javax.sound.sampled.TargetDataLine;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class MicrophoneManager {
	private final AudioDeviceManager devices = new AudioDeviceManager();
	private final AtomicBoolean occupied = new AtomicBoolean();

	public Lease acquire(String deviceId) throws Exception {
		if (!occupied.compareAndSet(false, true)) {
			throw new IllegalStateException("Microphone is already in use by Chat Canvas");
		}
		try {
			return new Lease(devices.open(deviceId), occupied);
		} catch (Exception exception) {
			occupied.set(false);
			throw exception;
		}
	}

	public AudioDeviceManager devices() {
		return devices;
	}

	public static final class Lease implements AutoCloseable {
		private final AtomicReference<AudioDeviceManager.OpenedMicrophone> openedRef;
		private final AtomicBoolean occupied;

		Lease(AudioDeviceManager.OpenedMicrophone opened,
					  AtomicBoolean occupied) {
			this.openedRef = new AtomicReference<>(
					Objects.requireNonNull(opened, "OpenedMicrophone must not be null"));
			this.occupied = occupied;
		}

		public AudioDeviceManager.OpenedMicrophone openedOrThrow() {
			AudioDeviceManager.OpenedMicrophone current = openedRef.get();
			if (current == null) {
				throw new IllegalStateException("Microphone lease is already closed");
			}
			return current;
		}

		@Override
		public void close() {
			AudioDeviceManager.OpenedMicrophone current = openedRef.getAndSet(null);
			if (current == null) {
				return;
			}
			closeLine(current);
			occupied.set(false);
		}

		private static void closeLine(AudioDeviceManager.OpenedMicrophone opened) {
			TargetDataLine line = opened.line();
			if (line == null) return;
			try {
				if (line.isRunning()) {
					line.stop();
				}
			} catch (RuntimeException ignored) {
			}
			try {
				line.flush();
			} catch (RuntimeException ignored) {
			}
			try {
				line.close();
			} catch (RuntimeException ignored) {
			}
		}
	}
}
