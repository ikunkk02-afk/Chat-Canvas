package io.github.ikunkk02.chatcanvas.voice;

/**
 * Establishes the native string encoding before any Vosk or JNA-backed voice
 * object is initialized.
 */
public final class VoskEncodingBootstrap {
	private static boolean initialized;

	private VoskEncodingBootstrap() {
	}

	public static synchronized void initialize() {
		if (initialized && "UTF-8".equalsIgnoreCase(System.getProperty("jna.encoding"))) {
			return;
		}
		try {
			System.setProperty("jna.encoding", "UTF-8");
		} catch (SecurityException ignored) {
			// The voice backend verifies the property before touching Vosk and
			// reports a recoverable voice error if the runtime forbids it.
		}
		initialized = true;
	}

	public static synchronized void verifyBeforeVoskInitialization() {
		String encoding = System.getProperty("jna.encoding");
		if (!"UTF-8".equalsIgnoreCase(encoding)) {
			throw new IllegalStateException(
					"Vosk must be initialized with jna.encoding=UTF-8");
		}
	}
}
