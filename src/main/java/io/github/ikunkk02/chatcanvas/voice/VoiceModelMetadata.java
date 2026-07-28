package io.github.ikunkk02.chatcanvas.voice;

public record VoiceModelMetadata(
		String id,
		String displayName,
		String language,
		String version,
		long expectedSize,
		long expectedExtractedSize,
		String downloadUrl,
		String sha256,
		String license,
		String requiredRootDirectory
) {
	public static final VoiceModelMetadata CHINESE_SMALL = new VoiceModelMetadata(
			"vosk-model-small-cn-0.22",
			"Vosk 中文小模型",
			"中文",
			"0.22",
			43_898_754L,
			68_292_271L,
			"https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip",
			"3AF8B0E7E0F835AE9D414CE5DF580237A3CFB08D586C9FBBB0F7FF29AD5B14BA",
			"Apache License 2.0",
			"vosk-model-small-cn-0.22"
	);
}
