package io.github.ikunkk02.chatcanvas.voice;

import com.k2fsa.sherpa.onnx.LibraryUtils;
import io.github.ikunkk02.chatcanvas.ChatCanvas;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.LongConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class VoiceRuntimeManager {
	public static final String SHERPA_VERSION = "1.13.4";
	public static final VoiceModelFile SILERO_VAD = new VoiceModelFile(
			"silero_vad.int8.onnx",
			"https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/silero_vad.int8.onnx",
			212_860L, "C36D490AFF5AB924CA6C7AEEC4D8F6BD3D22DB6FA17611B9C5B17EAE58AC3A20");

	private final VoiceArtifactInstaller installer;
	private final Path root = FMLPaths.CONFIGDIR.get()
			.resolve("chatcanvas").resolve("voice-runtime").resolve("sherpa-onnx")
			.resolve(SHERPA_VERSION);
	private volatile boolean loaded;
	private volatile String lastFailure = "";
	private volatile Throwable loadFailure;

	VoiceRuntimeManager(VoiceArtifactInstaller installer) { this.installer = installer; }

	public Path vadModel() { return root.resolve("vad").resolve(SILERO_VAD.relativePath()); }
	public String lastFailure() { return lastFailure; }
	public boolean isLoaded() { return loaded; }

	public long missingDownloadBytes() {
		long missing = installedVad() ? 0L : SILERO_VAD.size();
		RuntimeArtifact artifact = artifactFor(VoicePlatformSupport.current());
		if (artifact != null && !installedRuntime(artifact)) missing += artifact.file().size();
		return missing;
	}

	public boolean isInstalled() {
		return nativeRuntimeAvailable() && installedVad();
	}

	public boolean nativeRuntimeAvailable() {
		RuntimeArtifact artifact = artifactFor(VoicePlatformSupport.current());
		if (VoicePlatformSupport.current().os() == VoicePlatformSupport.OperatingSystem.IOS) {
			return iosBridge().map(bridge -> safeAvailable(bridge) && Files.isDirectory(bridge.nativeDirectory()))
					.orElse(false);
		}
		return artifact != null && installedRuntime(artifact);
	}

	public long installRequirements(LongConsumer cumulativeProgress) throws Exception {
		long completed = 0L;
		RuntimeArtifact artifact = artifactFor(VoicePlatformSupport.current());
		if (artifact == null && !nativeRuntimeAvailable()) {
			throw new IOException("No downloadable sherpa runtime for " + VoicePlatformSupport.current());
		}
		if (artifact != null && !installedRuntime(artifact)) {
			Path downloads = root.resolve("downloads");
			Path archive = downloads.resolve(artifact.file().relativePath());
			long base = completed;
			installer.installFile(artifact.file(), archive, done -> cumulativeProgress.accept(base + done));
			extractRuntime(archive, artifact);
			completed += artifact.file().size();
			cumulativeProgress.accept(completed);
		}
		if (!installedVad()) {
			long base = completed;
			installer.installFile(SILERO_VAD, vadModel(), done -> cumulativeProgress.accept(base + done));
			completed += SILERO_VAD.size();
			cumulativeProgress.accept(completed);
		}
		loadFailure = null;
		lastFailure = "";
		return completed;
	}

	public synchronized void load() {
		if (loaded) return;
		if (loadFailure != null) throw new VoiceNativeRuntimeException(lastFailure, loadFailure);
		try {
			Path nativeDirectory = nativeDirectory();
			if (!Files.isDirectory(nativeDirectory)) throw new IOException("Sherpa native directory is missing");
			preloadDependencies(nativeDirectory);
			System.setProperty("sherpa_onnx.native.path", nativeDirectory.toAbsolutePath().toString());
			LibraryUtils.load();
			loaded = true;
			loadFailure = null;
			lastFailure = "";
			ChatCanvas.LOGGER.info("Loaded sherpa-onnx {} runtime for {}", SHERPA_VERSION,
					VoicePlatformSupport.sherpaPlatformId(VoicePlatformSupport.current()));
		} catch (Throwable throwable) {
			loaded = false;
			loadFailure = throwable;
			lastFailure = throwable.getClass().getSimpleName() + ": " + String.valueOf(throwable.getMessage());
			ChatCanvas.LOGGER.error("Unable to load sherpa-onnx runtime: {}", lastFailure, throwable);
			throw new VoiceNativeRuntimeException(lastFailure, throwable);
		}
	}

	private Path nativeDirectory() throws IOException {
		VoicePlatformSupport.VoicePlatform platform = VoicePlatformSupport.current();
		if (platform.os() == VoicePlatformSupport.OperatingSystem.IOS) {
			return iosBridge().filter(VoiceRuntimeManager::safeAvailable)
					.map(SherpaRuntimeBridge::nativeDirectory)
					.orElseThrow(() -> new IOException("Current iOS launcher has no signed sherpa runtime bridge"));
		}
		String platformId = VoicePlatformSupport.sherpaPlatformId(platform);
		Path installed = root.resolve("native").resolve(platformId);
		if (platform.os() == VoicePlatformSupport.OperatingSystem.ANDROID) {
			RuntimeArtifact artifact = artifactFor(platform);
			if (artifact == null) throw new IOException("No Android sherpa runtime for " + platformId);
			Path privateCache = AndroidNativeRuntimeStager.resolvePrivateCacheBase();
			Path staged = AndroidNativeRuntimeStager.stage(installed, privateCache,
					SHERPA_VERSION, platformId, artifact.file().sha256());
			ChatCanvas.LOGGER.info("Prepared Android sherpa runtime in app-private cache: {}", staged);
			return staged;
		}
		return installed;
	}

	private void extractRuntime(Path archive, RuntimeArtifact artifact) throws Exception {
		Path nativeRoot = root.resolve("native");
		Path target = nativeRoot.resolve(VoicePlatformSupport.sherpaPlatformId(VoicePlatformSupport.current()));
		Path staging = nativeRoot.resolve(target.getFileName() + ".installing");
		VoiceArtifactInstaller.deleteTree(staging, nativeRoot);
		Files.createDirectories(staging);
		long extracted = 0L;
		try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
			for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
				String name = entry.getName().replace('\\', '/');
				if (entry.isDirectory() || !name.startsWith(artifact.entryPrefix())) continue;
				String leaf = name.substring(artifact.entryPrefix().length());
				if (leaf.isBlank() || leaf.contains("/") || leaf.contains("\\")) continue;
				Path output = staging.resolve(leaf).normalize();
				if (!output.startsWith(staging.normalize())) throw new IOException("Unsafe runtime entry");
				try (var stream = Files.newOutputStream(output)) {
					byte[] buffer = new byte[64 * 1024];
					for (int read; (read = zip.read(buffer)) >= 0;) {
						if (read == 0) continue;
						extracted += read;
						if (extracted > 96L * 1024L * 1024L) throw new IOException("Runtime extraction limit exceeded");
						stream.write(buffer, 0, read);
					}
				}
			}
		}
		if (!runtimeFilesPresent(staging)) throw new IOException("Sherpa runtime archive has no JNI library");
		VoiceArtifactInstaller.deleteTree(target, nativeRoot);
		VoiceArtifactInstaller.moveReplacing(staging, target);
	}

	private static void preloadDependencies(Path directory) throws IOException {
		List<Path> libraries;
		try (var files = Files.list(directory)) {
			libraries = files.filter(Files::isRegularFile).toList();
		}
		for (Path library : dependencyLoadOrder(libraries)) {
			System.load(library.toAbsolutePath().toString());
		}
	}

	static List<Path> dependencyLoadOrder(List<Path> libraries) {
		List<Path> result = new ArrayList<>();
		// Android's C++ API has a DT_NEEDED entry for the C API. Load C before C++ explicitly
		// because app-private cache directories are not part of the launcher's library search path.
		for (String token : List.of("onnxruntime", "sherpa-onnx-c-api", "sherpa-onnx-cxx-api")) {
			for (Path library : libraries) {
				String name = library.getFileName().toString().toLowerCase();
				if (name.contains(token) && !name.contains("jni") && !result.contains(library)) {
					result.add(library);
				}
			}
		}
		return List.copyOf(result);
	}

	private boolean installedVad() {
		try { return Files.isRegularFile(vadModel()) && Files.size(vadModel()) == SILERO_VAD.size(); }
		catch (IOException ignored) { return false; }
	}

	private boolean installedRuntime(RuntimeArtifact artifact) {
		try { return runtimeFilesPresent(root.resolve("native")
				.resolve(VoicePlatformSupport.sherpaPlatformId(VoicePlatformSupport.current()))); }
		catch (IOException ignored) { return false; }
	}

	private static boolean runtimeFilesPresent(Path directory) throws IOException {
		if (!Files.isDirectory(directory)) return false;
		try (var files = Files.list(directory)) {
			return files.anyMatch(path -> path.getFileName().toString().toLowerCase().contains("sherpa-onnx-jni"));
		}
	}

	private static java.util.Optional<SherpaRuntimeBridge> iosBridge() {
		try {
			return ServiceLoader.load(SherpaRuntimeBridge.class).stream().map(ServiceLoader.Provider::get)
					.filter(bridge -> "ios-arm64".equals(bridge.platformId())).findFirst();
		} catch (Throwable ignored) { return java.util.Optional.empty(); }
	}

	private static boolean safeAvailable(SherpaRuntimeBridge bridge) {
		try { return bridge.isAvailable(); } catch (Throwable ignored) { return false; }
	}

	private static RuntimeArtifact artifactFor(VoicePlatformSupport.VoicePlatform platform) {
		String base = "https://github.com/k2-fsa/sherpa-onnx/releases/download/v" + SHERPA_VERSION + "/";
		String id = VoicePlatformSupport.sherpaPlatformId(platform);
		if (platform.os() == VoicePlatformSupport.OperatingSystem.ANDROID) {
			String name = "sherpa-onnx-1.13.4.aar";
			return new RuntimeArtifact(new VoiceModelFile(name, base + name, 48_847_529L,
					"03F9C4DF965F21C71269365A7951A7F23B5696FDDD093FA318C80D65550AB780"), "jni/" + id + "/");
		}
		return switch (id) {
			case "win-x64" -> runtime(base, id, 8_049_207L, "9A718E3ECFDDF818389F91B276C5B570EF3218D19BBBC7B35F3F15011B83824F");
			case "win-arm64" -> runtime(base, id, 7_699_615L, "EBD65A5F997D95AA864FE46CE7BC53C8D50926FF1819828B557A6D6FAF059BB4");
			case "linux-x64" -> runtime(base, id, 10_215_007L, "E8728EBB2E902ACD7872F55E7C584CA9FDB4BDC7122F0680CAD5D5468A74BC0F");
			case "linux-aarch64" -> runtime(base, id, 12_895_199L, "D5571BAEC2F05F0F8FFE2E12101E613FEB12200CA873C72FCA5C22F3E9EDCF34");
			case "osx-x64" -> runtime(base, id, 10_491_108L, "462648128E98AB86305CE7C4C4A06186DDCC653FDC2E73A0A91021C6238184B5");
			case "osx-aarch64" -> runtime(base, id, 9_215_809L, "794A0C95FF2C892A4619224B613AAFADE2D2CB61C1ADD772F151FAC4D5C1865E");
			default -> null;
		};
	}

	private static RuntimeArtifact runtime(String base, String id, long size, String sha) {
		String name = "sherpa-onnx-native-lib-" + id + "-" + SHERPA_VERSION + ".jar";
		return new RuntimeArtifact(new VoiceModelFile(name, base + name, size, sha),
				"sherpa-onnx/native/" + id + "/");
	}

	private record RuntimeArtifact(VoiceModelFile file, String entryPrefix) { }
}
