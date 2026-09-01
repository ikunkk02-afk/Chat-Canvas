package io.github.ikunkk02.chatcanvas.voice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Android fallback without a compile-time Android framework dependency. */
public final class AndroidAudioRecordCaptureBackend implements AudioCaptureBackend {
	private final AtomicBoolean running = new AtomicBoolean();
	private volatile Object recorder;
	private volatile Throwable lastError;

	@Override
	public CaptureCapabilities capabilities() {
		if (VoicePlatformSupport.current().os() != VoicePlatformSupport.OperatingSystem.ANDROID) {
			return CaptureCapabilities.unavailable("android_audio_record", "Android AudioRecord",
					"chat_canvas.voice.error.android_runtime_unavailable");
		}
		try {
			androidClass("android.media.AudioRecord");
			if (!permissionGranted()) {
				return CaptureCapabilities.unavailable("android_audio_record", "Android AudioRecord",
						"chat_canvas.voice.error.microphone_permission");
			}
			return new CaptureCapabilities(true, "android_audio_record", "Android AudioRecord",
					16_000, 1, true, "", List.of(new AudioCaptureDevice("", "Android default", true)));
		} catch (Throwable throwable) {
			lastError = throwable;
			return CaptureCapabilities.unavailable("android_audio_record", "Android AudioRecord",
					"chat_canvas.voice.error.android_runtime_unavailable");
		}
	}

	@Override
	public void start(String deviceId, AudioCallback callback) throws Exception {
		if (!permissionGranted()) throw new SecurityException("RECORD_AUDIO permission denied");
		Class<?> audioRecord = androidClass("android.media.AudioRecord");
		Class<?> audioFormat = androidClass("android.media.AudioFormat");
		Class<?> source = androidClass("android.media.MediaRecorder$AudioSource");
		int mono = audioFormat.getField("CHANNEL_IN_MONO").getInt(null);
		int pcm16 = audioFormat.getField("ENCODING_PCM_16BIT").getInt(null);
		int voiceRecognition = source.getField("VOICE_RECOGNITION").getInt(null);
		Method minBuffer = audioRecord.getMethod("getMinBufferSize", int.class, int.class, int.class);
		Constructor<?> constructor = audioRecord.getConstructor(
				int.class, int.class, int.class, int.class, int.class);
		int sampleRate = 16_000;
		int minimum = (int) minBuffer.invoke(null, sampleRate, mono, pcm16);
		if (minimum <= 0) {
			sampleRate = 48_000;
			minimum = (int) minBuffer.invoke(null, sampleRate, mono, pcm16);
		}
		if (minimum <= 0) throw new IllegalStateException("AudioRecord has no supported PCM16 mono rate");
		Object opened = constructor.newInstance(voiceRecognition, sampleRate, mono, pcm16,
				Math.max(minimum * 2, sampleRate / 5 * 2));
		recorder = opened;
		int initialized = audioRecord.getField("STATE_INITIALIZED").getInt(null);
		if ((int) audioRecord.getMethod("getState").invoke(opened) != initialized) {
			throw new IllegalStateException("AudioRecord initialization failed");
		}
		Method start = audioRecord.getMethod("startRecording");
		Method read = audioRecord.getMethod("read", byte[].class, int.class, int.class);
		Method stop = audioRecord.getMethod("stop");
		Method release = audioRecord.getMethod("release");
		Pcm16MonoResampler resampler = sampleRate == 16_000 ? null : new Pcm16MonoResampler(sampleRate, 1);
		byte[] buffer = new byte[Math.max(minimum, 3_200)];
		try {
			running.set(true);
			start.invoke(opened);
			while (running.get()) {
				int count = (int) read.invoke(opened, buffer, 0, buffer.length);
				if (count <= 0) continue;
				byte[] pcm = resampler == null
						? java.util.Arrays.copyOf(buffer, count)
						: resampler.convert(buffer, count, false);
				if (pcm.length > 0) callback.onPcm16Mono16Khz(pcm);
			}
		} catch (Throwable throwable) {
			lastError = throwable;
			Throwable cause = throwable.getCause() == null ? throwable : throwable.getCause();
			if (cause instanceof Exception exception) throw exception;
			throw new IllegalStateException("Android AudioRecord capture failed", cause);
		} finally {
			running.set(false);
			recorder = null;
			try { stop.invoke(opened); } catch (Throwable ignored) { }
			try { release.invoke(opened); } catch (Throwable ignored) { }
		}
	}

	@Override
	public void stop() {
		running.set(false);
		Object current = recorder;
		if (current != null) {
			try { current.getClass().getMethod("stop").invoke(current); } catch (Throwable ignored) { }
		}
	}

	private static boolean permissionGranted() {
		try {
			Class<?> activityThread = androidClass("android.app.ActivityThread");
			Object application = activityThread.getMethod("currentApplication").invoke(null);
			if (application == null) return true;
			Method check = application.getClass().getMethod("checkSelfPermission", String.class);
			return (int) check.invoke(application, "android.permission.RECORD_AUDIO") == 0;
		} catch (ClassNotFoundException ignored) {
			return false;
		} catch (NoSuchMethodException ignored) {
			return true;
		} catch (Throwable throwable) {
			return false;
		}
	}

	private static Class<?> androidClass(String name) throws ClassNotFoundException {
		ClassLoader context = Thread.currentThread().getContextClassLoader();
		ClassLoader own = AndroidAudioRecordCaptureBackend.class.getClassLoader();
		ClassLoader system = ClassLoader.getSystemClassLoader();
		for (ClassLoader loader : new ClassLoader[] {context, own, system, null}) {
			try { return Class.forName(name, false, loader); }
			catch (ClassNotFoundException ignored) { }
		}
		throw new ClassNotFoundException(name);
	}

	@Override public Throwable getLastError() { return lastError; }
	@Override public void close() { stop(); }
}
