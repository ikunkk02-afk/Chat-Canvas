package io.github.ikunkk02.chatcanvas.voice;

import com.sun.jna.Platform;

import java.util.Locale;
import java.util.Set;

public final class VoicePlatformSupport {
	private static final Set<String> X64 = Set.of("amd64", "x86_64", "x86-64");
	private static final Set<String> ARM64 = Set.of("aarch64", "arm64");

	private VoicePlatformSupport() {
	}

	public static VoicePlatform current() {
		OperatingSystem os = Platform.isWindows() ? OperatingSystem.WINDOWS
				: Platform.isLinux() ? OperatingSystem.LINUX
				: Platform.isMac() ? OperatingSystem.MACOS : OperatingSystem.UNKNOWN;
		String value = Platform.ARCH.toLowerCase(Locale.ROOT);
		CpuArchitecture architecture = X64.contains(value) ? CpuArchitecture.X86_64
				: ARM64.contains(value) ? CpuArchitecture.ARM64 : CpuArchitecture.UNKNOWN;
		return new VoicePlatform(os, architecture);
	}

	public static boolean isSupported(VoicePlatform platform) {
		return platform.architecture() == CpuArchitecture.X86_64
				&& (platform.os() == OperatingSystem.WINDOWS
				|| platform.os() == OperatingSystem.LINUX
				|| platform.os() == OperatingSystem.MACOS)
				|| platform.os() == OperatingSystem.MACOS
				&& platform.architecture() == CpuArchitecture.ARM64;
	}

	public enum OperatingSystem { WINDOWS, LINUX, MACOS, UNKNOWN }
	public enum CpuArchitecture { X86_64, ARM64, UNKNOWN }
	public record VoicePlatform(OperatingSystem os, CpuArchitecture architecture) { }
}
