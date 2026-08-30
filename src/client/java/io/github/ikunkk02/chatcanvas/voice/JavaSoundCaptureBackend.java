package io.github.ikunkk02.chatcanvas.voice;

import javax.sound.sampled.TargetDataLine;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class JavaSoundCaptureBackend implements AudioCaptureBackend {
	private final AudioDeviceManager devices = new AudioDeviceManager();
	private final AtomicBoolean running = new AtomicBoolean();
	private volatile TargetDataLine line;
	private volatile Throwable lastError;

	@Override
	public CaptureCapabilities capabilities() {
		try {
			List<AudioCaptureDevice> available = devices.devices().stream()
					.map(device -> new AudioCaptureDevice(device.id(), device.displayName(), device.exactFormat()))
					.toList();
			return new CaptureCapabilities(true, "java_sound", "Java Sound", 16_000, 1,
					true, "", available);
		} catch (Throwable throwable) {
			lastError = throwable;
			return CaptureCapabilities.unavailable("java_sound", "Java Sound",
					"chat_canvas.voice.error.java_sound_unavailable");
		}
	}

	@Override
	public void start(String deviceId, AudioCallback callback) throws Exception {
		AudioDeviceManager.OpenedMicrophone opened = devices.open(deviceId);
		TargetDataLine openedLine = opened.line();
		line = openedLine;
		running.set(true);
		Pcm16MonoResampler resampler = new Pcm16MonoResampler(
				opened.format().getSampleRate(), opened.format().getChannels());
		byte[] source = new byte[Math.max(2_048, opened.format().getFrameSize() * 2_048)];
		try {
			openedLine.start();
			while (running.get()) {
				int read = openedLine.read(source, 0, source.length);
				if (read <= 0) continue;
				byte[] converted = resampler.convert(source, read, opened.format().isBigEndian());
				if (converted.length > 0) callback.onPcm16Mono16Khz(converted);
			}
		} catch (Throwable throwable) {
			lastError = throwable;
			if (throwable instanceof Exception exception) throw exception;
			throw new IllegalStateException("Java Sound capture failed", throwable);
		} finally {
			running.set(false);
			line = null;
			closeLine(openedLine);
		}
	}

	@Override
	public void stop() {
		running.set(false);
		closeLine(line);
	}

	private static void closeLine(TargetDataLine current) {
		if (current == null) return;
		try { current.stop(); } catch (RuntimeException ignored) { }
		try { current.flush(); } catch (RuntimeException ignored) { }
		try { current.close(); } catch (RuntimeException ignored) { }
	}

	@Override public Throwable getLastError() { return lastError; }
	@Override public void close() { stop(); }
}
