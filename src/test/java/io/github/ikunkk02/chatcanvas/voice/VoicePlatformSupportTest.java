package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoicePlatformSupportTest {
	@Test
	void detectsFclAsAndroidArm64InsteadOfLinux() {
		var platform = VoicePlatformSupport.detect(
				"Linux", "Android-16", "OpenJDK Runtime Environment", "aarch64", false,
				"mio.Wrapper com.tungsten.fcl");
		assertEquals(VoicePlatformSupport.OperatingSystem.ANDROID, platform.os());
		assertEquals(VoicePlatformSupport.CpuArchitecture.ARM64, platform.architecture());
		assertEquals("FCL", platform.launcher());
		assertEquals(1, VoicePlatformSupport.defaultInferenceThreads(platform));
	}

	@Test
	void detectsAndroidPojavWithoutAndroidFrameworkOnBootClasspath() {
		var platform = VoicePlatformSupport.detect(
				"Linux", "6.1.0-android", "OpenJDK 64-Bit Server VM", "aarch64", false,
				"net.kdt.pojavlaunch.PojavLauncher");
		assertEquals(VoicePlatformSupport.OperatingSystem.ANDROID, platform.os());
		assertEquals("PojavLauncher", platform.launcher());
		assertEquals("arm64-v8a", VoicePlatformSupport.sherpaPlatformId(platform));
	}

	@Test
	void detectsIosArm64WithoutTreatingItAsMacOs() {
		var platform = VoicePlatformSupport.detect(
				"Darwin", "OpenJDK Java", "arm64", false, "PojavLauncher");
		assertEquals(VoicePlatformSupport.OperatingSystem.IOS, platform.os());
		assertEquals(VoicePlatformSupport.CpuArchitecture.ARM64, platform.architecture());
		assertEquals("PojavLauncher", platform.launcher());
		assertTrue(VoicePlatformSupport.isSupported(platform));
	}

	@Test
	void desktopDefaultsToTwoThreads() {
		var platform = VoicePlatformSupport.detect(
				"Windows 11", "OpenJDK", "amd64", false, "Minecraft");
		assertEquals(VoicePlatformSupport.OperatingSystem.WINDOWS, platform.os());
		assertEquals(2, VoicePlatformSupport.defaultInferenceThreads(platform));
		assertEquals(4, VoicePlatformSupport.maximumInferenceThreads(platform));
	}

	@Test
	void preservedVoskRuntimeIsNotAdvertisedOnArm64() {
		var platform = VoicePlatformSupport.detect(
				"macOS", "OpenJDK", "arm64", false, "Minecraft");
		assertFalse(VoicePlatformSupport.supportsModel(platform,
				VoiceModelRegistry.get(VoiceModelRegistry.VOSK_CN)));
		assertTrue(VoicePlatformSupport.supportsModel(platform,
				VoiceModelRegistry.get(VoiceModelRegistry.SENSE_VOICE)));
	}
}
