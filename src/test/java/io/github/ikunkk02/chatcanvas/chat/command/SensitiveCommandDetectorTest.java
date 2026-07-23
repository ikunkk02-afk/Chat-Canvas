package io.github.ikunkk02.chatcanvas.chat.command;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveCommandDetectorTest {
	@ParameterizedTest
	@ValueSource(strings = {
			"/login secret", "/L secret", "/register secret secret",
			"/minecraft:auth token", "/passwd old new", "/ChangePass old new", "/cp old new"
	})
	void detectsSensitiveCommands(String command) {
		assertTrue(SensitiveCommandDetector.isSensitive(command));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"/gamemode creative @s", "/time set day", "/loginstatus", "login secret"
	})
	void ignoresUnrelatedCommands(String command) {
		assertFalse(SensitiveCommandDetector.isSensitive(command));
	}
}
