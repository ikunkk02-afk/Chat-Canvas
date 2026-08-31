package io.github.ikunkk02.chatcanvas.voice;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongConsumer;

final class VoiceArtifactInstaller {
	private static final Set<String> TRUSTED_HOSTS = Set.of(
			"alphacephei.com", "huggingface.co", "cdn-lfs.huggingface.co",
			"github.com", "objects.githubusercontent.com", "release-assets.githubusercontent.com");
	private final HttpClient http = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NORMAL)
			.connectTimeout(Duration.ofSeconds(20)).build();
	private final AtomicBoolean cancelled = new AtomicBoolean();

	void cancel() { cancelled.set(true); }
	void reset() { cancelled.set(false); }

	void installFile(VoiceModelFile file, Path target, LongConsumer progress) throws Exception {
		if (Files.isRegularFile(target) && Files.size(target) == file.size()) {
			try {
				verifySha256(target, file.sha256());
				progress.accept(file.size());
				return;
			} catch (Exception corrupt) {
				Files.deleteIfExists(target);
			}
		}
		Files.createDirectories(target.getParent());
		Path part = target.resolveSibling(target.getFileName() + ".part");
		Files.deleteIfExists(part);
		try {
			download(file, part, progress);
			verifySha256(part, file.sha256());
			moveReplacing(part, target);
		} finally {
			Files.deleteIfExists(part);
		}
	}

	private void download(VoiceModelFile file, Path target, LongConsumer progress) throws Exception {
		URI source = URI.create(file.downloadUrl());
		validateUri(source);
		HttpRequest request = HttpRequest.newBuilder(source)
				.timeout(Duration.ofMinutes(15)).GET().build();
		HttpResponse<InputStream> response = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
		if (response.statusCode() / 100 != 2) throw new IOException("Download HTTP " + response.statusCode());
		validateUri(response.uri());
		long downloaded = 0L;
		try (InputStream input = response.body(); var output = Files.newOutputStream(target)) {
			byte[] buffer = new byte[64 * 1024];
			for (int read; (read = input.read(buffer)) >= 0;) {
				checkCancelled();
				if (read == 0) continue;
				downloaded += read;
				if (downloaded > file.size()) throw new IOException("Artifact is larger than its manifest");
				output.write(buffer, 0, read);
				progress.accept(downloaded);
			}
		}
		if (downloaded != file.size()) {
			throw new IOException("Unexpected artifact size " + downloaded + ", expected " + file.size());
		}
	}

	static void validateUri(URI uri) throws IOException {
		if (!"https".equalsIgnoreCase(uri.getScheme())) throw new IOException("Voice artifacts require HTTPS");
		String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
		boolean trusted = TRUSTED_HOSTS.contains(host) || host.endsWith(".xethub.hf.co")
				|| host.equals("cdn.hf.co") || host.endsWith(".cdn.hf.co")
				|| host.equals("cdn-lfs.hf.co") || host.endsWith(".cdn-lfs.hf.co")
				|| host.endsWith(".huggingface.co") || host.endsWith(".githubusercontent.com");
		if (!trusted) throw new IOException("Unexpected voice artifact host: " + host);
	}

	static void verifySha256(Path file, String expected) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		try (InputStream input = Files.newInputStream(file)) {
			byte[] buffer = new byte[64 * 1024];
			for (int read; (read = input.read(buffer)) >= 0;) if (read > 0) digest.update(buffer, 0, read);
		}
		String actual = HexFormat.of().withUpperCase().formatHex(digest.digest());
		if (!actual.equalsIgnoreCase(expected)) throw new IOException("SHA-256 mismatch for " + file.getFileName());
	}

	private void checkCancelled() throws IOException {
		if (cancelled.get() || Thread.currentThread().isInterrupted()) {
			throw new IOException("Voice model installation cancelled");
		}
	}

	static void moveReplacing(Path source, Path target) throws IOException {
		try {
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException ignored) {
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	static void deleteTree(Path root, Path allowedRoot) throws IOException {
		if (root == null || Files.notExists(root)) return;
		Path normalized = root.toAbsolutePath().normalize();
		if (!normalized.startsWith(allowedRoot.toAbsolutePath().normalize()) || normalized.equals(allowedRoot.toAbsolutePath().normalize())) {
			throw new IOException("Refusing to delete outside voice data directory: " + normalized);
		}
		try (var paths = Files.walk(normalized)) {
			for (Path path : paths.sorted((a, b) -> b.getNameCount() - a.getNameCount()).toList()) {
				Files.deleteIfExists(path);
			}
		}
	}
}
