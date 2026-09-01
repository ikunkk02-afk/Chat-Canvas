package io.github.ikunkk02.chatcanvas.chat.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CommandTextSanitizerTest {
	@Test
	void normalizesToOneSafeLineAndOneLeadingSlash() {
		assertEquals("/execute as @s run say \"你好 🌍\"",
				CommandTextSanitizer.normalizeCommand(
						" execute\tas @s\r\nrun say \"你好 🌍\" "));
		assertEquals("/time set day",
				CommandTextSanitizer.normalizeCommand("time set day"));
	}

	@Test
	void removesControlCharactersWithoutSplittingUnicode() {
		String result = CommandTextSanitizer.normalizeCommand(
				"/say A\u0000B 👨‍👩‍👧");
		assertEquals("/say AB 👨‍👩‍👧", result);
		assertFalse(result.contains("\u0000"));
	}
}
