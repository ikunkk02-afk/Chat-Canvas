package io.github.ikunkk02.chatcanvas.voice;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

final class VoiceInputController {
	private final VoiceSettingsStorage settingsStorage = new VoiceSettingsStorage();
	private final VoiceModelManager models = new VoiceModelManager();
	private final AudioCaptureBackendFactory captureFactory = new AudioCaptureBackendFactory();
	private final VoiceErrorHandler errors = new VoiceErrorHandler();
	private final VoiceSessionStateMachine lifecycle = new VoiceSessionStateMachine();
	private final ExecutorService modelExecutor = executor("ChatCanvas-Voice-Model", 8);
	private final ExecutorService captureExecutor = executor("ChatCanvas-Voice-Capture", 2);
	private final ExecutorService recognitionExecutor = executor("ChatCanvas-Voice-ASR", 8);
	private final ScheduledExecutorService watchdog = new ScheduledThreadPoolExecutor(1, runnable -> {
		Thread thread = new Thread(runnable, "ChatCanvas-Voice-Watchdog");
		thread.setDaemon(true);
		return thread;
	});
	private final AtomicLong tokens = new AtomicLong();
	private volatile VoiceSettings settings;
	private volatile VoiceInputState state;
	private volatile VoiceInputSession session;
	private volatile String partial = "";
	private volatile long progress;
	private volatile long progressTotal;
	private volatile Consumer<VoiceRecognitionResult> resultConsumer;
	private volatile boolean pendingStart;
	private volatile boolean microphoneTesting;
	private volatile double microphoneTestLevel;
	private volatile AudioCaptureBackend microphoneTestBackend;
	private volatile CaptureCapabilities lastCaptureCapabilities =
			new CaptureCapabilities(true, "detecting", "Detecting", 16_000, 1,
					true, "", List.of());
	private volatile List<AudioCaptureDevice> lastCaptureDevices = List.of();
	private volatile long lastCaptureProbeAt;

	VoiceInputController() {
		settings = settingsStorage.load();
		String migrated = models.migrateSelection(settings.selectedModelId());
		if (!migrated.equals(settings.selectedModelId())) {
			settings = settings.withSelectedModel(migrated);
			settingsStorage.save(settings);
		}
		refreshAvailability();
		watchdog.scheduleAtFixedRate(this::watchdogTick, 50L, 50L, TimeUnit.MILLISECONDS);
		VoicePlatformSupport.VoicePlatform platform = VoicePlatformSupport.current();
		ChatCanvas.LOGGER.info("Voice platform: os={}, architecture={}, launcher={}",
				platform.os(), platform.architecture(), platform.launcher().isBlank() ? "unknown" : platform.launcher());
		scheduleCaptureProbe();
	}

	synchronized void prepareQuickStart() {
		if (state == VoiceInputState.IDLE) setLifecycle(VoiceInputState.OPENING_CHAT);
	}

	synchronized boolean begin(Consumer<VoiceRecognitionResult> consumer) {
		if (!settings.enabled()) { state = VoiceInputState.DISABLED; return false; }
		if (isBusy() && state != VoiceInputState.OPENING_CHAT) return false;
		if (microphoneTesting) stopMicrophoneTest();
		VoiceModelDescriptor model = selectedModel();
		if (model == null || !models.isReady(model)) { state = VoiceInputState.MODEL_MISSING; return false; }
		if (!VoicePlatformSupport.isSupported(VoicePlatformSupport.current())) {
			state = VoiceInputState.UNSUPPORTED_PLATFORM;
			errors.report("chat_canvas.voice.error.unsupported", null);
			return false;
		}
		if (!VoicePlatformSupport.supportsModel(VoicePlatformSupport.current(), model)) {
			state = VoiceInputState.ERROR;
			errors.report("chat_canvas.voice.platform.model_unsupported", null);
			return false;
		}
		if (lifecycle.state() == VoiceInputState.IDLE) setLifecycle(VoiceInputState.OPENING_CHAT);
		setLifecycle(VoiceInputState.WAITING_FOR_SPEECH);
		partial = "";
		pendingStart = true;
		resultConsumer = consumer;
		long token = tokens.incrementAndGet();
		int threads = effectiveInferenceThreads();
		try {
			captureExecutor.execute(() -> prepareCapture(token, model, threads));
		} catch (java.util.concurrent.RejectedExecutionException rejected) {
			pendingStart = false;
			resultConsumer = null;
			lifecycle.forceIdle();
			state = VoiceInputState.ERROR;
			errors.report("chat_canvas.voice.error.microphone", rejected);
			return false;
		}
		return true;
	}

	private void prepareCapture(long token, VoiceModelDescriptor model, int threads) {
		AudioCaptureBackend capture = captureFactory.create();
		CaptureCapabilities capabilities = capture.capabilities();
		lastCaptureCapabilities = capabilities;
		lastCaptureDevices = capture.devices();
		if (!capabilities.available()) {
			capture.close();
			fail(token, capabilities.unavailableReason(), capture.getLastError());
			return;
		}
		synchronized (this) {
			if (!pendingStart || tokens.get() != token) {
				capture.close();
				return;
			}
		}
		modelExecutor.execute(() -> startAfterModelLoad(token, model, capture, threads));
	}

	private void startAfterModelLoad(long token, VoiceModelDescriptor model,
								 AudioCaptureBackend capture, int threads) {
		try {
			models.load(model, threads);
			AsrSession recognizer = models.createSession();
			VadProcessor vad = models.createVad(settings, threads);
			VoiceInputSession created = new VoiceInputSession(token, capture, recognizer, vad,
					recognitionExecutor, settings, new SessionListener(token));
			synchronized (this) {
				if (!pendingStart || tokens.get() != token) {
					created.cancel();
					return;
				}
				session = created;
			}
			ChatCanvas.LOGGER.info("Voice session {} started: backend={}, provider={}, model={}", token,
					lastCaptureCapabilities.backendId(), model.provider(), model.id());
			captureExecutor.execute(created::startCapture);
		} catch (Throwable throwable) {
			capture.close();
			fail(token, modelFailureKey(throwable), throwable);
		}
	}

	synchronized void finish() {
		pendingStart = false;
		VoiceInputSession active = session;
		if (active != null) {
			active.requestFinish(VoiceFinishReason.MANUAL);
		} else if (isBusy()) {
			tokens.incrementAndGet();
			resultConsumer = null;
			partial = "";
			lifecycle.forceIdle();
			state = VoiceInputState.IDLE;
		}
	}

	synchronized void cancel() {
		pendingStart = false;
		tokens.incrementAndGet();
		VoiceInputSession active = session;
		session = null;
		resultConsumer = null;
		partial = "";
		if (active != null) active.cancel();
		lifecycle.forceIdle();
		state = settings.enabled() ? availabilityState() : VoiceInputState.DISABLED;
	}

	private void complete(long token, VoiceRecognitionResult result) {
		onClient(() -> {
			Consumer<VoiceRecognitionResult> consumer;
			synchronized (this) {
				if (session == null || session.token() != token || tokens.get() != token) return;
				session = null;
				pendingStart = false;
				partial = "";
				setLifecycle(VoiceInputState.COMMITTING_RESULT);
				consumer = resultConsumer;
				resultConsumer = null;
			}
			if (result.reason() == VoiceFinishReason.NO_SPEECH) {
				errors.report("chat_canvas.voice.error.no_speech", null);
			} else if (consumer != null) {
				consumer.accept(result);
				ChatCanvas.LOGGER.info("Voice session {} result committed ({} characters)", token, result.text().length());
			}
			synchronized (this) { setLifecycle(VoiceInputState.IDLE); }
		});
	}

	private void fail(long token, String key, Throwable throwable) {
		String detail = throwable == null ? key
				: throwable.getClass().getSimpleName() + ": " + String.valueOf(throwable.getMessage());
		ChatCanvas.LOGGER.warn("Voice session {} failed: {}", token, detail);
		onClient(() -> {
			synchronized (this) {
				if (token != tokens.get()) return;
				VoiceInputSession active = session;
				session = null;
				pendingStart = false;
				resultConsumer = null;
				partial = "";
				if (active != null) active.cancel();
				lifecycle.forceIdle();
				state = VoiceInputState.ERROR;
			}
			errors.report(key, throwable);
		});
	}

	private synchronized void sessionState(long token, VoiceInputState next) {
		if (token != tokens.get() || session == null || session.token() != token) return;
		if (next == VoiceInputState.FINALIZING || next == VoiceInputState.SPEAKING
				|| next == VoiceInputState.WAITING_FOR_ENDPOINT || next == VoiceInputState.WAITING_FOR_SPEECH) {
			setLifecycle(next);
		}
	}

	private synchronized void setLifecycle(VoiceInputState next) {
		try { lifecycle.transition(next); }
		catch (IllegalStateException invalid) {
			ChatCanvas.LOGGER.debug("Resetting inconsistent voice lifecycle: {}", invalid.getMessage());
			lifecycle.forceIdle();
			if (next != VoiceInputState.IDLE) {
				lifecycle.transition(VoiceInputState.OPENING_CHAT);
				if (next != VoiceInputState.OPENING_CHAT) lifecycle.transition(VoiceInputState.WAITING_FOR_SPEECH);
				if (next != VoiceInputState.OPENING_CHAT && next != VoiceInputState.WAITING_FOR_SPEECH) lifecycle.transition(next);
			}
		}
		state = lifecycle.state();
	}

	void installModel(String modelId, Consumer<VoiceRecognitionResult> consumer, boolean startAfter) {
		VoiceModelDescriptor model = VoiceModelRegistry.get(modelId);
		if (model == null || isDownloadBusy()) return;
		VoiceModelCapability capability = modelCapability(model);
		if (!capability.available()) {
			errors.report(capability.reasonKey(), null);
			return;
		}
		VoiceInputSession draining;
		long operationToken;
		synchronized (this) {
			draining = session;
			if (draining != null) cancel();
			operationToken = tokens.incrementAndGet();
			state = VoiceInputState.MODEL_DOWNLOADING;
			progress = 0L;
			progressTotal = 0L;
		}
		selectModelInternal(modelId);
		modelExecutor.execute(() -> {
			try {
				awaitModelSafe(draining);
				models.install(model, new VoiceModelDownloadManager.ProgressListener() {
					@Override public void state(VoiceInputState value) {
						if (tokens.get() == operationToken) state = value;
					}
					@Override public void progress(long done, long total) {
						if (tokens.get() == operationToken) { progress = done; progressTotal = total; }
					}
				});
				if (tokens.get() != operationToken) return;
				state = VoiceInputState.MODEL_LOADING;
				models.load(model, effectiveInferenceThreads());
				onClient(() -> {
					if (tokens.get() != operationToken) return;
					state = VoiceInputState.IDLE;
					if (startAfter) begin(consumer);
				});
			} catch (Throwable throwable) {
				if (tokens.get() != operationToken) return;
				onClient(() -> {
					if (tokens.get() != operationToken) return;
					state = VoiceInputState.ERROR;
					errors.report(installFailureKey(throwable), throwable);
				});
			}
		});
	}

	void installSelectedModel() {
		VoiceModelDescriptor selected = selectedModel();
		installModel(selected == null ? VoiceModelRegistry.defaultModel().id() : selected.id(), null, false);
	}

	synchronized void selectModel(String modelId) {
		VoiceModelDescriptor model = VoiceModelRegistry.get(modelId);
		if (model == null) return;
		if (!VoicePlatformSupport.supportsModel(VoicePlatformSupport.current(), model)) {
			errors.report("chat_canvas.voice.platform.model_unsupported", null);
			return;
		}
		VoiceInputSession draining = session;
		cancel();
		selectModelInternal(modelId);
		if (!models.isReady(model)) { state = VoiceInputState.MODEL_MISSING; return; }
		state = VoiceInputState.MODEL_LOADING;
		modelExecutor.execute(() -> {
			try { awaitModelSafe(draining); models.load(model, effectiveInferenceThreads()); state = VoiceInputState.IDLE; }
			catch (Throwable throwable) { fail(tokens.get(), modelFailureKey(throwable), throwable); }
		});
	}

	private synchronized void selectModelInternal(String id) {
		settings = settings.withSelectedModel(id);
		settingsStorage.save(settings);
	}

	void warmSelectedModel() {
		VoiceModelDescriptor model = selectedModel();
		if (model == null || !models.isReady(model) || models.loadedModel() != null) return;
		modelExecutor.execute(() -> {
			try { models.load(model, effectiveInferenceThreads()); }
			catch (Throwable throwable) { ChatCanvas.LOGGER.warn("Voice model warm-up failed", throwable); }
		});
	}

	void toggleMicrophoneTest() {
		if (microphoneTesting) { stopMicrophoneTest(); return; }
		if (isBusy()) return;
		microphoneTesting = true;
		captureExecutor.execute(() -> {
			AudioCaptureBackend backend = captureFactory.create();
			microphoneTestBackend = backend;
			AudioLevelMeter meter = new AudioLevelMeter();
			try {
				backend.start(settings.microphoneId(), pcm -> {
					long millis = pcm.length * 1_000L / 32_000L;
					microphoneTestLevel = meter.acceptPcm16Le(pcm, pcm.length,
							settings.noiseThreshold(), millis);
				});
			} catch (Throwable throwable) {
				if (microphoneTesting) onClient(() ->
						errors.report(microphoneFailureKey(throwable), throwable));
			} finally {
				backend.close();
				microphoneTestBackend = null;
				microphoneTesting = false;
				microphoneTestLevel = 0.0;
			}
		});
	}

	void stopMicrophoneTest() {
		microphoneTesting = false;
		AudioCaptureBackend backend = microphoneTestBackend;
		if (backend != null) backend.stop();
	}

	private void scheduleCaptureProbe() {
		long now = System.currentTimeMillis();
		synchronized (this) {
			if (microphoneTesting || isBusy()) return;
			if (now - lastCaptureProbeAt < 2_000L) return;
			lastCaptureProbeAt = now;
		}
		try {
			captureExecutor.execute(() -> {
				AudioCaptureBackend backend = captureFactory.create();
				try {
					lastCaptureCapabilities = backend.capabilities();
					lastCaptureDevices = backend.devices();
				} catch (Throwable throwable) {
					ChatCanvas.LOGGER.debug("Voice capture capability probe failed", throwable);
				} finally {
					backend.close();
				}
			});
		} catch (java.util.concurrent.RejectedExecutionException ignored) {
			lastCaptureProbeAt = 0L;
		}
	}

	void releaseModel() {
		VoiceInputSession draining;
		synchronized (this) { draining = session; cancel(); }
		modelExecutor.execute(() -> { awaitModelSafe(draining); models.unload(); refreshAvailability(); });
	}
	synchronized void cancelModelInstall() {
		models.cancelInstall();
		tokens.incrementAndGet();
		pendingStart = false;
		progress = 0L;
		progressTotal = 0L;
		lifecycle.forceIdle();
		state = settings.enabled() ? availabilityState() : VoiceInputState.DISABLED;
	}

	void openModelsDirectory() {
		try {
			var directory = models.modelsDirectory().toAbsolutePath().normalize();
			Files.createDirectories(directory);
			Util.getOperatingSystem().open(directory);
			ChatCanvas.LOGGER.info("Opened voice model directory: {}", directory);
		} catch (Throwable throwable) {
			ChatCanvas.LOGGER.warn("Unable to open the voice model directory", throwable);
			errors.report("chat_canvas.voice.error.open_directory", throwable);
		}
	}

	synchronized void refreshAvailability() {
		lifecycle.forceIdle();
		state = settings.enabled() ? availabilityState() : VoiceInputState.DISABLED;
	}

	private VoiceInputState availabilityState() {
		if (!VoicePlatformSupport.isSupported(VoicePlatformSupport.current())) return VoiceInputState.UNSUPPORTED_PLATFORM;
		VoiceModelDescriptor selected = selectedModel();
		return selected != null && models.isReady(selected) ? VoiceInputState.IDLE : VoiceInputState.MODEL_MISSING;
	}

	synchronized void updateSettings(VoiceSettings value) {
		settings = value == null ? VoiceSettings.DEFAULT : value;
		settingsStorage.save(settings);
		if (!settings.enabled()) cancel();
		refreshAvailability();
	}

	void shutdown() {
		stopMicrophoneTest();
		VoiceInputSession draining;
		synchronized (this) { draining = session; cancel(); }
		awaitModelSafe(draining);
		models.close();
		watchdog.shutdownNow();
		modelExecutor.shutdownNow();
		captureExecutor.shutdownNow();
		recognitionExecutor.shutdownNow();
	}

	private void watchdogTick() {
		VoiceInputSession active = session;
		if (active != null) active.tick(System.currentTimeMillis());
	}

	private static String microphoneFailureKey(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			if (current instanceof VoiceCapabilityException capability) return capability.reasonKey();
			if (current instanceof SecurityException) return "chat_canvas.voice.error.microphone_permission";
			current = current.getCause();
		}
		return "chat_canvas.voice.error.microphone";
	}

	private static String modelFailureKey(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			if (current instanceof VoiceNativeRuntimeException
					|| current instanceof LinkageError || current instanceof SecurityException) {
				return VoicePlatformSupport.current().os() == VoicePlatformSupport.OperatingSystem.IOS
						? "chat_canvas.voice.error.ios_runtime_unavailable"
						: "chat_canvas.voice.error.native_runtime";
			}
			current = current.getCause();
		}
		return "chat_canvas.voice.error.model_corrupt";
	}

	private static String installFailureKey(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			if (current instanceof VoiceNativeRuntimeException
					|| current instanceof LinkageError || current instanceof SecurityException) {
				return modelFailureKey(throwable);
			}
			current = current.getCause();
		}
		return "chat_canvas.voice.error.download";
	}

	private static void awaitModelSafe(VoiceInputSession draining) {
		if (draining != null && !draining.awaitClosed(5L, TimeUnit.SECONDS)) {
			ChatCanvas.LOGGER.warn("Timed out waiting for the previous voice session to release its recognizer");
		}
	}

	private static ThreadPoolExecutor executor(String name, int queueCapacity) {
		return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(queueCapacity), runnable -> {
			Thread thread = new Thread(runnable, name);
			thread.setDaemon(true);
			return thread;
		}, new ThreadPoolExecutor.AbortPolicy());
	}

	private static void onClient(Runnable runnable) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client != null) client.execute(runnable); else runnable.run();
	}

	private boolean isDownloadBusy() {
		return state == VoiceInputState.MODEL_DOWNLOADING || state == VoiceInputState.MODEL_VERIFYING
				|| state == VoiceInputState.MODEL_EXTRACTING || state == VoiceInputState.MODEL_INSTALLING;
	}

	VoiceModelDescriptor selectedModel() { return VoiceModelRegistry.get(settings.selectedModelId()); }
	List<VoiceModelDescriptor> registeredModels() { return models.models(); }
	boolean isModelInstalled(String id) { return models.isReady(VoiceModelRegistry.get(id)); }
	VoiceModelCapability modelCapability(VoiceModelDescriptor model) {
		return models.capability(model, lastCaptureCapabilities);
	}
	int effectiveInferenceThreads() {
		VoicePlatformSupport.VoicePlatform platform = VoicePlatformSupport.current();
		int requested = settings.inferenceThreads() == 0
				? VoicePlatformSupport.defaultInferenceThreads(platform) : settings.inferenceThreads();
		return Math.min(requested, VoicePlatformSupport.maximumInferenceThreads(platform));
	}

	VoiceInputState state() { return state; }
	VoiceSettings settings() { return settings; }
	String partial() { return partial; }
	double level() { VoiceInputSession active = session; return active == null ? 0.0 : active.level(); }
	long progress() { return progress; }
	long progressTotal() { return progressTotal; }
	List<AudioCaptureDevice> devices() { scheduleCaptureProbe(); return lastCaptureDevices; }
	CaptureCapabilities captureCapabilities() { return lastCaptureCapabilities; }
	boolean isBusy() {
		return state == VoiceInputState.WAITING_FOR_SPEECH
				|| state == VoiceInputState.SPEAKING || state == VoiceInputState.WAITING_FOR_ENDPOINT
				|| state == VoiceInputState.FINALIZING || state == VoiceInputState.COMMITTING_RESULT
				|| pendingStart;
	}
	boolean isListening() {
		return state == VoiceInputState.WAITING_FOR_SPEECH || state == VoiceInputState.SPEAKING
				|| state == VoiceInputState.WAITING_FOR_ENDPOINT;
	}
	boolean isMicrophoneTesting() { return microphoneTesting; }
	double microphoneTestLevel() { return microphoneTestLevel; }

	private final class SessionListener implements VoiceInputSession.Listener {
		private final long token;
		private SessionListener(long token) { this.token = token; }
		@Override public void state(VoiceInputState value) { sessionState(token, value); }
		@Override public void partial(String text) {
			if (token == tokens.get() && session != null && session.token() == token) partial = text;
		}
		@Override public void completed(VoiceRecognitionResult result) { complete(token, result); }
		@Override public void failure(Throwable throwable) { fail(token, microphoneFailureKey(throwable), throwable); }
	}
}
