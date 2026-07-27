package io.github.ikunkk02.chatcanvas.voice;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import net.minecraft.client.MinecraftClient;

import java.awt.Desktop;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class VoiceInputManager {
	private final VoiceSettingsStorage settingsStorage = new VoiceSettingsStorage();
	private final VoskModelManager models = new VoskModelManager();
	private final VoiceModelDownloadManager downloader = new VoiceModelDownloadManager();
	private final VoskSpeechRecognitionBackend backend = new VoskSpeechRecognitionBackend();
	private final MicrophoneManager microphones = new MicrophoneManager();
	private final VoiceErrorHandler errors = new VoiceErrorHandler();
	private final ExecutorService modelExecutor = executor("ChatCanvas-Voice-Model");
	private final ExecutorService captureExecutor = executor("ChatCanvas-Voice-Capture");
	private final ExecutorService recognitionExecutor = executor("ChatCanvas-Voice-Recognition");
	private final AtomicLong tokenCounter = new AtomicLong();
	private volatile VoiceSettings settings;
	private volatile VoiceInputState state;
	private volatile VoiceInputSession session;
	private volatile String partial = "";
	private volatile long progress;
	private volatile long progressTotal;
	private volatile boolean pendingHold;
	private volatile Consumer<VoiceRecognitionResult> resultConsumer;
	private volatile boolean microphoneTesting;
	private volatile double microphoneTestLevel;
	private volatile MicrophoneManager.Lease microphoneTestLease;

	private VoiceInputManager() {
		settings = settingsStorage.load();
		refreshAvailability();
	}

	public static VoiceInputManager instance() {
		return Holder.INSTANCE;
	}

	private static final class Holder {
		private static final VoiceInputManager INSTANCE = create();

		private static VoiceInputManager create() {
			VoskEncodingBootstrap.initialize();
			return new VoiceInputManager();
		}
	}

	public synchronized boolean begin(Consumer<VoiceRecognitionResult> consumer) {
		if (!settings.enabled()) {
			state = VoiceInputState.DISABLED;
			return false;
		}
		if (!VoicePlatformSupport.isSupported(VoicePlatformSupport.current())) {
			state = VoiceInputState.UNSUPPORTED_PLATFORM;
			errors.report("chat_canvas.voice.error.unsupported", null);
			return false;
		}
		if (!models.isInstalled()) {
			state = VoiceInputState.MODEL_MISSING;
			return false;
		}
		if (session != null || pendingHold) return false;
		pendingHold = true;
		resultConsumer = consumer;
		long token = tokenCounter.incrementAndGet();
		if (backend.isReady()) {
			startSession(token);
		} else {
			state = VoiceInputState.MODEL_LOADING;
			modelExecutor.execute(() -> {
				try {
					backend.initialize(models.modelDirectory());
					onClient(() -> {
						synchronized (this) {
							if (pendingHold && token == tokenCounter.get()) startSession(token);
							else state = VoiceInputState.IDLE;
						}
					});
				} catch (Throwable throwable) {
					fail("chat_canvas.voice.error.model_corrupt", throwable);
				}
			});
		}
		return true;
	}

	private void startSession(long token) {
		try {
			captureExecutor.execute(() -> {
			try {
				MicrophoneManager.Lease lease = microphones.acquire(settings.microphoneId());
				RecognitionSession recognizer = backend.createSession(16_000.0f);
				VoiceInputSession created = new VoiceInputSession(
						token, lease, recognizer, recognitionExecutor, settings,
						value -> onClient(() -> {
							if (session != null && session.token() == token) partial = value;
						}),
						result -> onClient(() -> complete(token, result)),
						throwable -> fail("chat_canvas.voice.error.recognition", throwable),
						() -> onClient(() -> {
							if (session != null && session.token() == token) {
								state = VoiceInputState.RECOGNIZING;
							}
						}));
				synchronized (this) {
					if (!pendingHold || token != tokenCounter.get()) {
						created.cancel();
						state = VoiceInputState.IDLE;
						return;
					}
					session = created;
					state = VoiceInputState.LISTENING;
					partial = "";
				}
				created.startCapture();
			} catch (Throwable throwable) {
				fail("chat_canvas.voice.error.microphone", throwable);
			}
		});
		} catch (java.util.concurrent.RejectedExecutionException e) {
			fail("chat_canvas.voice.error.microphone", e);
		}
	}

	public synchronized void finish() {
		pendingHold = false;
		VoiceInputSession active = session;
		if (active == null) return;
		state = VoiceInputState.RECOGNIZING;
		active.finish();
	}

	public synchronized void cancel() {
		pendingHold = false;
		tokenCounter.incrementAndGet();
		VoiceInputSession active = session;
		session = null;
		resultConsumer = null;
		partial = "";
		if (active != null) active.cancel();
		state = models.isInstalled() ? VoiceInputState.CANCELLED : VoiceInputState.MODEL_MISSING;
		onClient(() -> {
			if (state == VoiceInputState.CANCELLED) state = VoiceInputState.IDLE;
		});
	}

	private synchronized void complete(long token, VoiceRecognitionResult result) {
		if (session == null || session.token() != token) return;
		session = null;
		pendingHold = false;
		partial = "";
		state = VoiceInputState.COMPLETED;
		Consumer<VoiceRecognitionResult> consumer = resultConsumer;
		resultConsumer = null;
		if (consumer != null) consumer.accept(result);
		state = VoiceInputState.IDLE;
	}

	public synchronized void installModel() {
		if (state == VoiceInputState.MODEL_DOWNLOADING
				|| state == VoiceInputState.MODEL_VERIFYING
				|| state == VoiceInputState.MODEL_EXTRACTING) return;
		modelExecutor.execute(() -> {
			try {
				downloader.install(models, new VoiceModelDownloadManager.ProgressListener() {
					@Override public void state(VoiceInputState value) { state = value; }
					@Override public void progress(long done, long total) {
						progress = done; progressTotal = total;
					}
				});
				state = VoiceInputState.MODEL_LOADING;
				backend.initialize(models.modelDirectory());
				state = VoiceInputState.IDLE;
			} catch (Throwable throwable) {
				fail("chat_canvas.voice.error.download", throwable);
				refreshAvailability();
			}
		});
	}

	public synchronized void toggleMicrophoneTest() {
		if (microphoneTesting) {
			stopMicrophoneTest();
			return;
		}
		if (isBusy()) return;
		microphoneTesting = true;
		captureExecutor.execute(() -> {
			AudioLevelMeter meter = new AudioLevelMeter();
			try {
				MicrophoneManager.Lease lease = microphones.acquire(settings.microphoneId());
				microphoneTestLease = lease;
				var opened = lease.opened();
				var line = opened.line();
				var format = opened.format();
				var resampler = new Pcm16MonoResampler(
						format.getSampleRate(), format.getChannels());
				byte[] source = new byte[Math.max(2048, format.getFrameSize() * 2048)];
				line.start();
				while (microphoneTesting) {
					int read = line.read(source, 0, source.length);
					if (read <= 0) continue;
					byte[] converted = resampler.convert(source, read, format.isBigEndian());
					long millis = converted.length * 1000L / 32_000L;
					microphoneTestLevel = meter.acceptPcm16Le(
							converted, converted.length, settings.noiseThreshold(), millis);
				}
			} catch (Throwable throwable) {
				if (microphoneTesting) fail("chat_canvas.voice.error.microphone", throwable);
			} finally {
				MicrophoneManager.Lease lease = microphoneTestLease;
				microphoneTestLease = null;
				if (lease != null) lease.close();
				microphoneTesting = false;
				microphoneTestLevel = 0.0;
			}
		});
	}

	public synchronized void stopMicrophoneTest() {
		microphoneTesting = false;
		MicrophoneManager.Lease lease = microphoneTestLease;
		if (lease != null) lease.close();
	}

	public void cancelModelInstall() {
		downloader.cancel();
	}

	public synchronized void releaseModel() {
		cancel();
		modelExecutor.execute(() -> {
			backend.close();
			refreshAvailability();
		});
	}

	public void openModelsDirectory() {
		try {
			Files.createDirectories(models.modelsDirectory());
			if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(models.modelsDirectory().toFile());
		} catch (Exception exception) {
			errors.report("chat_canvas.voice.error.open_directory", exception);
		}
	}

	public synchronized void refreshAvailability() {
		if (!settings.enabled()) state = VoiceInputState.DISABLED;
		else if (!VoicePlatformSupport.isSupported(VoicePlatformSupport.current())) {
			state = VoiceInputState.UNSUPPORTED_PLATFORM;
		} else state = models.isInstalled() ? VoiceInputState.IDLE : VoiceInputState.MODEL_MISSING;
	}

	public synchronized void updateSettings(VoiceSettings value) {
		settings = value;
		settingsStorage.save(value);
		if (!value.enabled()) cancel();
		refreshAvailability();
	}

	public void shutdown() {
		stopMicrophoneTest();
		cancel();
		downloader.cancel();
		backend.close();
		modelExecutor.shutdownNow();
		captureExecutor.shutdownNow();
		recognitionExecutor.shutdownNow();
	}

	private void fail(String key, Throwable throwable) {
		ChatCanvas.LOGGER.error("Voice input failure", throwable);
		onClient(() -> {
			synchronized (this) {
				VoiceInputSession active = session;
				session = null;
				pendingHold = false;
				if (active != null) active.cancel();
				state = VoiceInputState.ERROR;
				errors.report(key, throwable);
				refreshAvailability();
			}
		});
	}

	private static ThreadPoolExecutor executor(String name) {
		return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(1), runnable -> {
					Thread thread = new Thread(runnable, name);
					thread.setDaemon(true);
					return thread;
				}, new ThreadPoolExecutor.DiscardOldestPolicy());
	}

	private static void onClient(Runnable runnable) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client != null) client.execute(runnable);
	}

	public VoiceInputState state() { return state; }
	public VoiceSettings settings() { return settings; }
	public String partial() { return partial; }
	public double level() {
		VoiceInputSession active = session;
		return active == null ? 0.0 : active.level();
	}
	public long progress() { return progress; }
	public long progressTotal() { return progressTotal; }
	public List<AudioDeviceManager.AudioDevice> devices() { return microphones.devices().devices(); }
	public boolean isListening() { return state == VoiceInputState.LISTENING; }
	public boolean isBusy() {
		return state == VoiceInputState.LISTENING
				|| state == VoiceInputState.RECOGNIZING
				|| state == VoiceInputState.MODEL_LOADING;
	}
	public boolean isMicrophoneTesting() { return microphoneTesting; }
	public double microphoneTestLevel() { return microphoneTestLevel; }
}
