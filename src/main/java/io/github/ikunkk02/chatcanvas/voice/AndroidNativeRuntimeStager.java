package io.github.ikunkk02.chatcanvas.voice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Copies verified Android JNI libraries out of shared storage before dlopen. */
final class AndroidNativeRuntimeStager {
	private static final String OVERRIDE_PROPERTY = "chatcanvas.android.native.dir";
	private static final String COMPLETE_MARKER = ".chatcanvas-runtime";

	private AndroidNativeRuntimeStager() {
	}

	static Path resolvePrivateCacheBase() throws IOException {
		String override = property(OVERRIDE_PROPERTY);
		if (!override.isBlank()) {
			Path selected = parse(override);
			if (!isPrivateAndroidPath(selected)) {
				throw new IOException("Configured Android native directory is outside the app-private linker namespace: "
						+ selected);
			}
			return ensureWritable(selected);
		}

		Set<String> candidates = new LinkedHashSet<>(List.of(
				property("java.io.tmpdir"),
				property("jna.tmpdir"),
				property("org.lwjgl.system.SharedLibraryExtractPath"),
				environment("TMPDIR")));
		for (String value : candidates) {
			if (value.isBlank()) continue;
			try {
				Path candidate = parse(value);
				if (isPrivateAndroidPath(candidate)) return ensureWritable(candidate);
			} catch (IOException | InvalidPathException ignored) {
				// Try the next launcher-provided private cache directory.
			}
		}
		throw new IOException("Android launcher does not expose a writable app-private native cache; "
				+ "java.io.tmpdir=" + property("java.io.tmpdir"));
	}

	static synchronized Path stage(Path installedDirectory, Path privateCacheBase,
									 String version, String platformId, String fingerprint) throws IOException {
		Path source = installedDirectory.toAbsolutePath().normalize();
		if (!Files.isDirectory(source)) throw new IOException("Installed Android runtime directory is missing");
		Path allowedRoot = privateCacheBase.toAbsolutePath().normalize().resolve("chatcanvas-native");
		Path target = allowedRoot.resolve("sherpa-onnx").resolve(version).resolve(platformId).normalize();
		if (!target.startsWith(allowedRoot)) throw new IOException("Unsafe Android native cache path");
		if (cacheMatches(source, target, fingerprint)) return target;

		Files.createDirectories(target.getParent());
		Path staging = target.resolveSibling(target.getFileName() + ".installing-"
				+ ProcessHandle.current().pid());
		VoiceArtifactInstaller.deleteTree(staging, allowedRoot);
		Files.createDirectories(staging);
		try {
			try (var files = Files.list(source)) {
				for (Path library : files.filter(Files::isRegularFile).toList()) {
					String name = library.getFileName().toString();
					if (!name.endsWith(".so")) continue;
					Files.copy(library, staging.resolve(name), StandardCopyOption.REPLACE_EXISTING);
				}
			}
			if (!hasJniLibrary(staging)) throw new IOException("Android runtime archive has no JNI library");
			Files.writeString(staging.resolve(COMPLETE_MARKER), fingerprint, StandardCharsets.UTF_8);
			VoiceArtifactInstaller.deleteTree(target, allowedRoot);
			VoiceArtifactInstaller.moveReplacing(staging, target);
			return target;
		} finally {
			VoiceArtifactInstaller.deleteTree(staging, allowedRoot);
		}
	}

	static boolean isPrivateAndroidPath(Path path) {
		String value = path.normalize().toString().replace('\\', '/').toLowerCase();
		return value.equals("/data") || value.startsWith("/data/")
				|| value.equals("/mnt/expand") || value.startsWith("/mnt/expand/");
	}

	private static boolean cacheMatches(Path source, Path target, String fingerprint) {
		try {
			if (!Files.isDirectory(target) || !hasJniLibrary(target)) return false;
			String marker = Files.readString(target.resolve(COMPLETE_MARKER), StandardCharsets.UTF_8);
			if (!fingerprint.equals(marker)) return false;
			try (var files = Files.list(source)) {
				for (Path library : files.filter(Files::isRegularFile).toList()) {
					String name = library.getFileName().toString();
					if (!name.endsWith(".so")) continue;
					Path cached = target.resolve(name);
					if (!Files.isRegularFile(cached) || Files.size(cached) != Files.size(library)) return false;
				}
			}
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}

	private static boolean hasJniLibrary(Path directory) throws IOException {
		try (var files = Files.list(directory)) {
			return files.anyMatch(path -> Files.isRegularFile(path)
					&& path.getFileName().toString().equals("libsherpa-onnx-jni.so"));
		}
	}

	private static Path ensureWritable(Path path) throws IOException {
		Files.createDirectories(path);
		if (!Files.isWritable(path)) throw new IOException("Android native cache is not writable: " + path);
		return path.toAbsolutePath().normalize();
	}

	private static Path parse(String value) {
		return Path.of(value).toAbsolutePath().normalize();
	}

	private static String property(String key) {
		try { return System.getProperty(key, ""); }
		catch (SecurityException ignored) { return ""; }
	}

	private static String environment(String key) {
		try { return System.getenv().getOrDefault(key, ""); }
		catch (SecurityException ignored) { return ""; }
	}
}
