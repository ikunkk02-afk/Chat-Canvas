package io.github.ikunkk02.chatcanvas.chat.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DangerousCommandDetectorTest {
	@Test
	void warnsByRootWithoutClaimingCustomCommandsAreSafe() {
		assertTrue(DangerousCommandDetector.mayChangeWorldOrPlayers(
				"/minecraft:fill 0 0 0 1 1 1 stone"));
		assertTrue(DangerousCommandDetector.mayChangeWorldOrPlayers("/stop"));
		assertFalse(DangerousCommandDetector.mayChangeWorldOrPlayers("/warp home"));
	}
}
