package io.github.ikunkk02.chatcanvas.voice;

import io.github.ikunkk02.chatcanvas.ChatCanvas;

import java.util.ArrayList;
import java.util.List;

public final class AudioCaptureBackendFactory {
	public AudioCaptureBackend create() {
		VoicePlatformSupport.VoicePlatform platform = VoicePlatformSupport.current();
		List<AudioCaptureBackend> candidates = new ArrayList<>();
		candidates.add(new OpenALCaptureBackend());
		if (platform.os() == VoicePlatformSupport.OperatingSystem.ANDROID) {
			candidates.add(new AndroidAudioRecordCaptureBackend());
		} else if (platform.os() == VoicePlatformSupport.OperatingSystem.IOS) {
			candidates.add(new IOSNativeCaptureBackend());
		} else {
			AudioCaptureBackend javaSound = createJavaSoundLazily();
			if (javaSound != null) candidates.add(javaSound);
		}
		StringBuilder failures = new StringBuilder();
		List<AudioCaptureBackend> available = new ArrayList<>();
		String mobileTerminalReason = "";
		for (AudioCaptureBackend candidate : candidates) {
			CaptureCapabilities capabilities = candidate.capabilities();
			if (capabilities.available()) {
				available.add(candidate);
				continue;
			}
			if (!failures.isEmpty()) failures.append("; ");
			failures.append(capabilities.displayName()).append(": ")
					.append(capabilities.unavailableReason());
			if (platform.os() == VoicePlatformSupport.OperatingSystem.ANDROID
					|| platform.os() == VoicePlatformSupport.OperatingSystem.IOS) {
				mobileTerminalReason = capabilities.unavailableReason();
			}
			candidate.close();
		}
		if (!available.isEmpty()) return new FallbackAudioCaptureBackend(available,
				mobileTerminalReason.isBlank() ? null : new VoiceCapabilityException(mobileTerminalReason));
		ChatCanvas.LOGGER.warn("No voice capture backend available: {}", failures);
		return new UnavailableAudioCaptureBackend(
				platform.os() == VoicePlatformSupport.OperatingSystem.IOS
						? "chat_canvas.voice.error.ios_runtime_unavailable"
						: "chat_canvas.voice.error.microphone");
	}

	private static AudioCaptureBackend createJavaSoundLazily() {
		try {
			return (AudioCaptureBackend) Class.forName(
					"io.github.ikunkk02.chatcanvas.voice.JavaSoundCaptureBackend")
					.getConstructor().newInstance();
		} catch (Throwable throwable) {
			ChatCanvas.LOGGER.debug("Java Sound capture backend is unavailable", throwable);
			return null;
		}
	}
}
