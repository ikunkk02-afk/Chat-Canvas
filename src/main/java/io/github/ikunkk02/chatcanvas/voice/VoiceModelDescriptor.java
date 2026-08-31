package io.github.ikunkk02.chatcanvas.voice;

import java.util.List;

public record VoiceModelDescriptor(
		String id,
		String displayNameKey,
		VoiceModelProvider provider,
		List<String> languageKeys,
		long downloadSize,
		long installedSize,
		String performanceKey,
		String accuracyKey,
		String responseKey,
		boolean supportsStreaming,
		boolean supportsAndroid,
		boolean supportsIos,
		boolean supportsWindows,
		boolean supportsLinux,
		boolean supportsMacOs,
		ReloadPolicy reloadPolicy,
		String descriptionKey,
		String rootDirectory,
		List<VoiceModelFile> files
) {
	public VoiceModelDescriptor {
		languageKeys = List.copyOf(languageKeys);
		files = List.copyOf(files);
	}

	public boolean requiresSherpaRuntime() {
		return provider != VoiceModelProvider.VOSK;
	}
}
