package io.github.ikunkk02.chatcanvas.voice;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public final class OpenALCaptureBackend implements AudioCaptureBackend {
	private final AtomicBoolean running = new AtomicBoolean();
	private volatile long device;
	private volatile Throwable lastError;

	@Override
	public CaptureCapabilities capabilities() {
		try {
			ALCCapabilities caps = ALC.getCapabilities();
			boolean functions = caps != null && caps.alcCaptureOpenDevice != 0L
					&& caps.alcCaptureStart != 0L && caps.alcCaptureSamples != 0L
					&& caps.alcCaptureStop != 0L && caps.alcCaptureCloseDevice != 0L;
			if (!functions || !(caps.OpenALC11 || caps.ALC_EXT_CAPTURE)) {
				return CaptureCapabilities.unavailable("openal", "OpenAL Capture",
						"chat_canvas.voice.error.openal_capture_unavailable");
			}
			return new CaptureCapabilities(true, "openal", "OpenAL Capture", 16_000, 1,
					true, "", List.of(new AudioCaptureDevice("", "OpenAL default", true)));
		} catch (Throwable throwable) {
			lastError = throwable;
			return CaptureCapabilities.unavailable("openal", "OpenAL Capture",
					"chat_canvas.voice.error.openal_capture_unavailable");
		}
	}

	@Override
	public void start(String deviceId, AudioCallback callback) throws Exception {
		if (!capabilities().available()) throw new IllegalStateException("OpenAL capture is unavailable", lastError);
		long opened = 0L;
		try {
			opened = ALC11.alcCaptureOpenDevice((CharSequence) null, 16_000,
					AL10.AL_FORMAT_MONO16, 32_000);
			if (opened == 0L) throw new IllegalStateException("OpenAL capture device could not be opened");
			device = opened;
			running.set(true);
			ALC11.alcCaptureStart(opened);
			short[] samples = new short[1_600];
			while (running.get()) {
				int available = Math.max(0, ALC10.alcGetInteger(opened, ALC11.ALC_CAPTURE_SAMPLES));
				if (available == 0) {
					LockSupport.parkNanos(10_000_000L);
					continue;
				}
				int count = Math.min(available, samples.length);
				ALC11.alcCaptureSamples(opened, samples, count);
				byte[] pcm = new byte[count * 2];
				for (int i = 0; i < count; i++) {
					pcm[i * 2] = (byte) samples[i];
					pcm[i * 2 + 1] = (byte) (samples[i] >>> 8);
				}
				callback.onPcm16Mono16Khz(pcm);
			}
		} catch (Throwable throwable) {
			lastError = throwable;
			if (throwable instanceof Exception exception) throw exception;
			throw new IllegalStateException("OpenAL capture failed", throwable);
		} finally {
			running.set(false);
			device = 0L;
			if (opened != 0L) {
				try { ALC11.alcCaptureStop(opened); } catch (Throwable ignored) { }
				try { ALC11.alcCaptureCloseDevice(opened); } catch (Throwable ignored) { }
			}
		}
	}

	@Override
	public void stop() {
		running.set(false);
		long current = device;
		if (current != 0L) {
			try { ALC11.alcCaptureStop(current); } catch (Throwable ignored) { }
		}
	}

	@Override public Throwable getLastError() { return lastError; }
	@Override public void close() { stop(); }
}
