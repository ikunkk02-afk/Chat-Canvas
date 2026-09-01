package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class VoskEncodingBootstrapTest {
	@Test
	void forcesJnaUtf8IndependentlyOfJvmDefaultEncoding() {
		System.setProperty("jna.encoding", "windows-936");

		VoskEncodingBootstrap.initialize();

		assertEquals("UTF-8", System.getProperty("jna.encoding"));
		assertDoesNotThrow(VoskEncodingBootstrap::verifyBeforeVoskInitialization);
	}
}
