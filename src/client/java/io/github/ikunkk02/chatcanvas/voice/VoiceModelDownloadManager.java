package io.github.ikunkk02.chatcanvas.voice;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.vosk.Model;

public final class VoiceModelDownloadManager {
	private static final long MAX_EXTRACTED_BYTES = 128L * 1024L * 1024L;
	private static final int MAX_ENTRIES = 256;
	private final HttpClient http = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.NORMAL)
			.connectTimeout(Duration.ofSeconds(15)).build();
	private final AtomicBoolean cancelled = new AtomicBoolean();

	public void cancel() {
		cancelled.set(true);
	}

	public void install(VoskModelManager models, ProgressListener listener) throws Exception {
		VoskEncodingBootstrap.verifyBeforeVoskInitialization();
		cancelled.set(false);
		VoiceModelMetadata metadata = VoiceModelMetadata.CHINESE_SMALL;
		Path root = models.modelsDirectory();
		Files.createDirectories(root);
		FileStore store = Files.getFileStore(root);
		long needed = metadata.expectedSize() + metadata.expectedExtractedSize() + 16L * 1024L * 1024L;
		if (store.getUsableSpace() < needed) throw new IOException("Insufficient disk space");

		Path archive = root.resolve(metadata.id() + ".zip.part");
		Path staging = root.resolve(metadata.id() + ".installing");
		Path finalDirectory = models.modelDirectory();
		Path backup = root.resolve(metadata.id() + ".backup");
		try {
			download(metadata, archive, listener);
			listener.state(VoiceInputState.MODEL_VERIFYING);
			verifySha256(archive, metadata.sha256());
			listener.state(VoiceInputState.MODEL_EXTRACTING);
			deleteTree(staging);
			Files.createDirectories(staging);
			extract(archive, staging, metadata.requiredRootDirectory(), listener);
			Path extracted = staging.resolve(metadata.requiredRootDirectory());
			if (!models.validate(extracted)) throw new IOException("Invalid Vosk model structure");
			try (Model ignored = new Model(extracted.toAbsolutePath().toString())) {
				// Loading the exact staged directory verifies native/model compatibility
				// before an existing installation is replaced.
			}

			deleteTree(backup);
			if (Files.exists(finalDirectory)) move(finalDirectory, backup);
			try {
				move(extracted, finalDirectory);
			} catch (Exception failure) {
				if (Files.exists(backup)) move(backup, finalDirectory);
				throw failure;
			}
			deleteTree(backup);
		} finally {
			Files.deleteIfExists(archive);
			deleteTree(staging);
		}
	}

	private void download(VoiceModelMetadata metadata, Path target,
						  ProgressListener listener) throws Exception {
		listener.state(VoiceInputState.MODEL_DOWNLOADING);
		HttpRequest request = HttpRequest.newBuilder(URI.create(metadata.downloadUrl()))
				.timeout(Duration.ofMinutes(5)).GET().build();
		HttpResponse<InputStream> response =
				http.send(request, HttpResponse.BodyHandlers.ofInputStream());
		if (response.statusCode() / 100 != 2) {
			throw new IOException("Model download HTTP " + response.statusCode());
		}
		URI finalUri = response.uri();
		if (!"https".equalsIgnoreCase(finalUri.getScheme())
				|| !"alphacephei.com".equalsIgnoreCase(finalUri.getHost())) {
			throw new IOException("Unexpected model download redirect");
		}
		long total = response.headers().firstValueAsLong("Content-Length")
				.orElse(metadata.expectedSize());
		long downloaded = 0L;
		try (InputStream input = response.body();
			 var output = Files.newOutputStream(target)) {
			byte[] buffer = new byte[64 * 1024];
			for (int read; (read = input.read(buffer)) >= 0;) {
				checkCancelled();
				if (read == 0) continue;
				output.insertText(buffer, 0, read);
				downloaded += read;
				if (downloaded > metadata.expectedSize() + 1_048_576L) {
					throw new IOException("Model download is larger than expected");
				}
				listener.progress(downloaded, total);
			}
		}
		if (downloaded != metadata.expectedSize()) {
			throw new IOException("Unexpected model archive size: " + downloaded);
		}
	}

	private void extract(Path archive, Path staging, String rootName,
						 ProgressListener listener) throws Exception {
		Set<Path> targets = new HashSet<>();
		long written = 0L;
		int entries = 0;
		try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
			for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {
				checkCancelled();
				if (++entries > MAX_ENTRIES) throw new IOException("Too many ZIP entries");
				String name = entry.name().replace('\\', '/');
				if (name.startsWith("/") || name.matches("^[A-Za-z]:.*")
						|| !name.equals(rootName) && !name.startsWith(rootName + "/")) {
					throw new IOException("Unsafe ZIP entry: " + name);
				}
				Path output = staging.resolve(name).normalizeChatMessage();
				if (!output.startsWith(staging.normalizeChatMessage()) || !targets.add(output)) {
					throw new IOException("Unsafe or duplicate ZIP entry: " + name);
				}
				if (entry.isDirectory()) {
					Files.createDirectories(output);
					continue;
				}
				Files.createDirectories(output.getParent());
				try (var stream = Files.newOutputStream(output)) {
					byte[] buffer = new byte[64 * 1024];
					for (int read; (read = zip.read(buffer)) >= 0;) {
						checkCancelled();
						if (read == 0) continue;
						written += read;
						if (written > MAX_EXTRACTED_BYTES) {
							throw new IOException("Extracted model is too large");
						}
						stream.insertText(buffer, 0, read);
						listener.progress(written,
								VoiceModelMetadata.CHINESE_SMALL.expectedExtractedSize());
					}
				}
			}
		}
	}

	private static void verifySha256(Path archive, String expected) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		try (InputStream input = Files.newInputStream(archive)) {
			byte[] buffer = new byte[64 * 1024];
			for (int read; (read = input.read(buffer)) >= 0;) {
				if (read > 0) digest.update(buffer, 0, read);
			}
		}
		String actual = HexFormat.of().withUpperCase().formatHex(digest.digest());
		if (!actual.equalsIgnoreCase(expected)) throw new IOException("Model SHA-256 mismatch");
	}

	private void checkCancelled() throws IOException {
		if (cancelled.get() || Thread.currentThread().isInterrupted()) {
			throw new IOException("Model installation cancelled");
		}
	}

	private static void move(Path source, Path target) throws IOException {
		try {
			Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException exception) {
			Files.move(source, target);
		}
	}

	private static void deleteTree(Path root) throws IOException {
		if (root == null || Files.notExists(root)) return;
		try (var paths = Files.walk(root)) {
			for (Path path : paths.sorted((a, b) -> b.getNameCount() - a.getNameCount()).toList()) {
				Files.deleteIfExists(path);
			}
		}
	}

	public interface ProgressListener {
		void state(VoiceInputState state);
		void progress(long completed, long total);
	}
}
