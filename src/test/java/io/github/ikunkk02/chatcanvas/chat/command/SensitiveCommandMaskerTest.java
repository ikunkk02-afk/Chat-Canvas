package io.github.ikunkk02.chatcanvas.chat.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SensitiveCommandMaskerTest {
	@Test
	void masksAllSensitiveParametersWithoutLeakingLength() {
		assertEquals("/login ******", SensitiveCommandMasker.display("login hunter2"));
		assertEquals("/auth:register ******",
				SensitiveCommandMasker.display("/auth:register very long secret"));
	}

	@Test
	void leavesOrdinaryCommandsReadable() {
		assertEquals("/time set day", SensitiveCommandMasker.display("time set day"));
	}
}
