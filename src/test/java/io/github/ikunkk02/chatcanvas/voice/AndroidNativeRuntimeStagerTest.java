package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AndroidNativeRuntimeStagerTest {
	@TempDir Path temporary;

	@Test
	void recognizesFclPrivateCacheAndRejectsSharedStorage() {
		assertTrue(AndroidNativeRuntimeStager.isPrivateAndroidPath(
				Path.of("/data/user/0/com.tungsten.fcl/cache/fclauncher")));
		assertTrue(AndroidNativeRuntimeStager.isPrivateAndroidPath(
				Path.of("/data/data/com.tungsten.fcl/cache/fclauncher")));
		assertFalse(AndroidNativeRuntimeStager.isPrivateAndroidPath(
				Path.of("/storage/emulated/0/FCL/.minecraft/config")));
	}

	@Test
	void stagesOnlySharedLibrariesAndReusesVerifiedCache() throws Exception {
		Path source = Files.createDirectories(temporary.resolve("installed"));
		Files.writeString(source.resolve("libonnxruntime.so"), "onnx");
		Files.writeString(source.resolve("libsherpa-onnx-jni.so"), "jni");
		Files.writeString(source.resolve("ignored.txt"), "ignore");
		Path cache = Files.createDirectories(temporary.resolve("private-cache"));

		Path first = AndroidNativeRuntimeStager.stage(source, cache, "1.13.4", "arm64-v8a", "fingerprint-a");
		assertTrue(Files.isRegularFile(first.resolve("libonnxruntime.so")));
		assertFalse(Files.exists(first.resolve("ignored.txt")));
		Files.writeString(first.resolve("sentinel"), "preserved");

		Path second = AndroidNativeRuntimeStager.stage(source, cache, "1.13.4", "arm64-v8a", "fingerprint-a");
		assertEquals(first, second);
		assertTrue(Files.isRegularFile(second.resolve("sentinel")));

		Path third = AndroidNativeRuntimeStager.stage(source, cache, "1.13.4", "arm64-v8a", "fingerprint-b");
		assertEquals(first, third);
		assertFalse(Files.exists(third.resolve("sentinel")));
	}
}
