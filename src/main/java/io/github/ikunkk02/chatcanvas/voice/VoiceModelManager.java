package io.github.ikunkk02.chatcanvas.voice;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class VoiceModelManager implements AutoCloseable {
	private final Path modelsDirectory = FMLPaths.CONFIGDIR.get()
			.resolve("chatcanvas").resolve("voice-models");
	private final VoskModelManager legacyVosk = new VoskModelManager();
	private final VoiceArtifactInstaller installer = new VoiceArtifactInstaller();
	private final VoiceRuntimeManager runtime = new VoiceRuntimeManager(installer);
	private final VoiceModelDownloadManager legacyDownloader = new VoiceModelDownloadManager();
	private AsrProvider provider;
	private VoiceModelDescriptor loadedModel;

	public Path modelsDirectory() { return modelsDirectory; }
	public VoiceRuntimeManager runtime() { return runtime; }
	public List<VoiceModelDescriptor> models() { return VoiceModelRegistry.all(); }
	public VoiceModelDescriptor loadedModel() { return loadedModel; }

	public String migrateSelection(String selectedId) {
		if (VoiceModelRegistry.get(selectedId) != null) return selectedId;
		if (legacyVosk.isInstalled()) return VoiceModelRegistry.VOSK_CN;
		for (VoiceModelDescriptor model : models()) if (isInstalled(model)) return model.id();
		return "";
	}

	public boolean hasAnyInstalled() { return models().stream().anyMatch(this::isInstalled); }

	public boolean isReady(VoiceModelDescriptor model) {
		return isInstalled(model) && (!model.requiresSherpaRuntime() || runtime.isInstalled());
	}

	public boolean isInstalled(VoiceModelDescriptor model) {
		if (model == null) return false;
		if (model.provider() == VoiceModelProvider.VOSK) return legacyVosk.isInstalled();
		Path root = modelDirectory(model);
		for (VoiceModelFile file : model.files()) {
			try {
				if (!Files.isRegularFile(root.resolve(file.relativePath()))
						|| Files.size(root.resolve(file.relativePath())) != file.size()) return false;
			} catch (IOException ignored) { return false; }
		}
		return true;
	}

	public Path modelDirectory(VoiceModelDescriptor model) {
		return modelsDirectory.resolve(model.rootDirectory());
	}

	public VoiceModelCapability capability(VoiceModelDescriptor model, CaptureCapabilities audio) {
		VoicePlatformSupport.VoicePlatform platform = VoicePlatformSupport.current();
		if (!VoicePlatformSupport.supportsModel(platform, model)) {
			return VoiceModelCapability.unavailable("chat_canvas.voice.platform.model_unsupported", "");
		}
		if (!audio.available()) return VoiceModelCapability.unavailable(audio.unavailableReason(), "");
		if (platform.os() == VoicePlatformSupport.OperatingSystem.IOS && model.requiresSherpaRuntime()
				&& !runtime.nativeRuntimeAvailable()) {
			return VoiceModelCapability.unavailable("chat_canvas.voice.error.ios_runtime_unavailable",
					runtime.lastFailure());
		}
		return VoiceModelCapability.supported();
	}

	public void install(VoiceModelDescriptor model,
					VoiceModelDownloadManager.ProgressListener listener) throws Exception {
		if (model.provider() == VoiceModelProvider.VOSK) {
			legacyDownloader.install(legacyVosk, listener);
			return;
		}
		installer.reset();
		listener.state(VoiceInputState.MODEL_DOWNLOADING);
		long runtimeBytes = runtime.missingDownloadBytes();
		long modelBytes = isInstalled(model) ? 0L : model.downloadSize();
		long total = runtimeBytes + modelBytes;
		long runtimeDone = runtime.installRequirements(done -> listener.progress(done, total));
		if (!isInstalled(model)) installDirectModel(model, runtimeDone, total, listener);
		listener.progress(total, total);
	}

	private void installDirectModel(VoiceModelDescriptor model, long base, long total,
								VoiceModelDownloadManager.ProgressListener listener) throws Exception {
		Path staging = modelsDirectory.resolve(model.id() + ".installing");
		VoiceArtifactInstaller.deleteTree(staging, modelsDirectory);
		Files.createDirectories(staging);
		long completed = 0L;
		try {
			for (VoiceModelFile file : model.files()) {
				long offset = completed;
				installer.installFile(file, staging.resolve(file.relativePath()),
						done -> listener.progress(base + offset + done, total));
				completed += file.size();
			}
			listener.state(VoiceInputState.MODEL_VERIFYING);
			for (VoiceModelFile file : model.files()) {
				VoiceArtifactInstaller.verifySha256(staging.resolve(file.relativePath()), file.sha256());
			}
			listener.state(VoiceInputState.MODEL_INSTALLING);
			Path target = modelDirectory(model);
			VoiceArtifactInstaller.deleteTree(target, modelsDirectory);
			VoiceArtifactInstaller.moveReplacing(staging, target);
		} finally {
			VoiceArtifactInstaller.deleteTree(staging, modelsDirectory);
		}
	}

	public synchronized void load(VoiceModelDescriptor model, int threads) throws Exception {
		if (!isInstalled(model)) throw new IOException("Voice model is not installed: " + model.id());
		if (loadedModel != null && loadedModel.id().equals(model.id()) && provider != null && provider.isLoaded()) return;
		long started = System.nanoTime();
		unload();
		if (model.requiresSherpaRuntime()) runtime.load();
		provider = switch (model.provider()) {
			case VOSK -> new VoskAsrProvider();
			case SHERPA_ONLINE -> new SherpaOnlineAsrProvider();
			case SHERPA_SENSE_VOICE, SHERPA_WHISPER -> new SherpaOfflineAsrProvider(model.provider());
		};
		provider.loadModel(model, modelDirectory(model), new AsrRuntimeOptions(threads, false));
		loadedModel = model;
		ChatCanvas.LOGGER.info("Loaded voice model {} with provider {} in {} ms", model.id(), provider.id(),
				(System.nanoTime() - started) / 1_000_000L);
	}

	public synchronized AsrSession createSession() throws Exception {
		if (provider == null || !provider.isLoaded()) throw new IllegalStateException("No ASR model loaded");
		return provider.createSession();
	}

	public synchronized VadProcessor createVad(VoiceSettings settings, int threads) {
		if (loadedModel == null || !loadedModel.requiresSherpaRuntime()) return null;
		return new SileroVadProcessor(runtime.vadModel(), settings, 1);
	}

	public synchronized boolean providerSuppliesEndpoint() {
		return provider != null && provider.suppliesEndpoint();
	}

	public synchronized void unload() {
		if (provider != null) {
			try { provider.close(); } catch (Throwable throwable) {
				ChatCanvas.LOGGER.warn("Failed to release voice model", throwable);
			}
		}
		provider = null;
		loadedModel = null;
	}

	public void cancelInstall() { installer.cancel(); legacyDownloader.cancel(); }
	@Override public void close() { cancelInstall(); unload(); }
}
