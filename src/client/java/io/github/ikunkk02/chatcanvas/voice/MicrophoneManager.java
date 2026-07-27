package io.github.ikunkk02.chatcanvas.voice;

import java.util.concurrent.atomic.AtomicBoolean;

public final class MicrophoneManager {
	private final AudioDeviceManager devices = new AudioDeviceManager();
	private final AtomicBoolean occupied = new AtomicBoolean();

	public Lease acquire(String deviceId) throws Exception {
		if (!occupied.compareAndSet(false, true)) {
			throw new IllegalStateException("Microphone is already in use by Chat Canvas");
		}
		try {
			return new Lease(devices.open(deviceId));
		} catch (Exception exception) {
			occupied.set(false);
			throw exception;
		}
	}

	public AudioDeviceManager devices() {
		return devices;
	}

	public final class Lease implements AutoCloseable {
		private AudioDeviceManager.OpenedMicrophone opened;

		private Lease(AudioDeviceManager.OpenedMicrophone opened) {
			this.opened = opened;
		}

		public AudioDeviceManager.OpenedMicrophone opened() {
			return opened;
		}

		@Override
		public void close() {
			if (opened == null) return;
			try {
				opened.line().stop();
				opened.line().flush();
				opened.line().close();
			} finally {
				opened = null;
				occupied.set(false);
			}
		}
	}
}
