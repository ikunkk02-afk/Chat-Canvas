package io.github.ikunkk02.chatcanvas.voice;

import java.util.Locale;
import java.util.Set;

public final class VoicePlatformSupport {
	private static final Set<String> X64 = Set.of("amd64", "x86_64", "x86-64");
	private static final Set<String> ARM64 = Set.of("aarch64", "arm64");
	private static final Set<String> ARM32 = Set.of("arm", "arm32", "armeabi-v7a");

	private VoicePlatformSupport() {
	}

	public static VoicePlatform current() {
		String osName = property("os.name");
		String runtime = property("java.runtime.name") + " " + property("java.vm.name")
				+ " " + property("java.vendor");
		return detect(osName, property("os.version"), runtime, property("os.arch"),
				classExists("android.os.Build"), property("sun.java.command"));
	}

	static VoicePlatform detect(String osName, String runtime, String architectureValue,
								boolean androidClassPresent, String command) {
		return detect(osName, "", runtime, architectureValue, androidClassPresent, command);
	}

	static VoicePlatform detect(String osName, String osVersion, String runtime, String architectureValue,
								boolean androidClassPresent, String command) {
		osName = safeLower(osName);
		osVersion = safeLower(osVersion);
		runtime = safeLower(runtime);
		String value = safeLower(architectureValue);
		String commandValue = safeLower(command);
		String launcher = detectLauncher(runtime + " " + commandValue);
		boolean ios = osName.contains("ios") || runtime.contains("robovm")
				|| runtime.contains("iphone") || runtime.contains("j2objc")
				|| commandValue.contains("pojav")
				&& (osName.contains("darwin") || osName.contains("mac"));
		boolean android = !ios && (androidClassPresent || runtime.contains("android")
				|| osName.contains("android") || osVersion.contains("android")
				|| "FCL".equals(launcher)
				|| "PojavLauncher".equals(launcher) && (osName.contains("linux") || osName.contains("nux")));
		OperatingSystem os = android ? OperatingSystem.ANDROID
				: ios ? OperatingSystem.IOS
				: osName.contains("win") ? OperatingSystem.WINDOWS
				: osName.contains("mac") || osName.contains("darwin") ? OperatingSystem.MACOS
				: osName.contains("linux") || osName.contains("nux") ? OperatingSystem.LINUX
				: OperatingSystem.UNKNOWN;
		CpuArchitecture architecture = X64.contains(value) ? CpuArchitecture.X86_64
				: ARM64.contains(value) ? CpuArchitecture.ARM64
				: ARM32.contains(value) || value.startsWith("arm") ? CpuArchitecture.ARM32
				: value.startsWith("x86") ? CpuArchitecture.X86_32 : CpuArchitecture.UNKNOWN;
		return new VoicePlatform(os, architecture, launcher);
	}

	public static boolean isSupported(VoicePlatform platform) {
		if (platform.architecture() == CpuArchitecture.UNKNOWN) return false;
		return switch (platform.os()) {
			case WINDOWS, LINUX, MACOS -> platform.architecture() == CpuArchitecture.X86_64
					|| platform.architecture() == CpuArchitecture.ARM64;
			case ANDROID -> platform.architecture() == CpuArchitecture.ARM64
					|| platform.architecture() == CpuArchitecture.ARM32;
			case IOS -> platform.architecture() == CpuArchitecture.ARM64;
			default -> false;
		};
	}

	public static boolean supportsModel(VoicePlatform platform, VoiceModelDescriptor model) {
		if (!isSupported(platform)) return false;
		// The preserved Vosk 0.3.45 Java artifact only bundles desktop x86-64 natives.
		if (model.provider() == VoiceModelProvider.VOSK
				&& platform.architecture() != CpuArchitecture.X86_64) return false;
		return switch (platform.os()) {
			case WINDOWS -> model.supportsWindows();
			case LINUX -> model.supportsLinux();
			case MACOS -> model.supportsMacOs();
			case ANDROID -> model.supportsAndroid();
			case IOS -> model.supportsIos();
			default -> false;
		};
	}

	public static int defaultInferenceThreads(VoicePlatform platform) {
		return isMobile(platform) ? 1 : 2;
	}

	public static int maximumInferenceThreads(VoicePlatform platform) {
		return isMobile(platform) ? 2 : 4;
	}

	public static boolean isMobile(VoicePlatform platform) {
		return platform.os() == OperatingSystem.ANDROID || platform.os() == OperatingSystem.IOS;
	}

	public static String sherpaPlatformId(VoicePlatform platform) {
		return switch (platform.os()) {
			case WINDOWS -> platform.architecture() == CpuArchitecture.ARM64 ? "win-arm64" : "win-x64";
			case LINUX -> platform.architecture() == CpuArchitecture.ARM64 ? "linux-aarch64" : "linux-x64";
			case MACOS -> platform.architecture() == CpuArchitecture.ARM64 ? "osx-aarch64" : "osx-x64";
			case ANDROID -> platform.architecture() == CpuArchitecture.ARM32 ? "armeabi-v7a" : "arm64-v8a";
			case IOS -> "ios-arm64";
			default -> "unknown";
		};
	}

	private static String detectLauncher(String combined) {
		if (combined.contains("foldcraft") || combined.contains("fcl")) return "FCL";
		if (combined.contains("pojav")) return "PojavLauncher";
		return "";
	}

	private static String safeLower(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}

	private static String property(String key) {
		try { return System.getProperty(key, "").toLowerCase(Locale.ROOT); }
		catch (SecurityException ignored) { return ""; }
	}

	private static boolean classExists(String name) {
		try { Class.forName(name, false, VoicePlatformSupport.class.getClassLoader()); return true; }
		catch (Throwable ignored) { return false; }
	}

	public enum OperatingSystem { WINDOWS, LINUX, MACOS, ANDROID, IOS, UNKNOWN }
	public enum CpuArchitecture { X86_64, X86_32, ARM64, ARM32, UNKNOWN }
	public record VoicePlatform(OperatingSystem os, CpuArchitecture architecture,
							String launcher) { }
}
