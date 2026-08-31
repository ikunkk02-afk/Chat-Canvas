package io.github.ikunkk02.chatcanvas.voice;

public record VoiceModelFile(
		String relativePath,
		String downloadUrl,
		long size,
		String sha256
) {
	public VoiceModelFile {
		if (relativePath == null || relativePath.isBlank()) throw new IllegalArgumentException("relativePath");
		if (downloadUrl == null || !downloadUrl.startsWith("https://")) throw new IllegalArgumentException("downloadUrl");
		if (size <= 0L) throw new IllegalArgumentException("size");
		if (sha256 == null || !sha256.matches("(?i)[0-9a-f]{64}")) throw new IllegalArgumentException("sha256");
	}
}
