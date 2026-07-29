package io.github.ikunkk02.chatcanvas.chat.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClipboardCommandParserTest {
	@Test
	void parsesMultipleCommandsAsIndependentNonExecutingCandidates() {
		ClipboardCommandParseResult result = ClipboardCommandParser.parse("""
				/gamemode creative

				time set day
				/weather clear
				""");

		assertTrue(result.multipleLines());
		assertEquals(3, result.candidates().size());
		assertEquals("/time set day", result.candidates().get(1).command());
		assertFalse(result.candidates().get(1).hadLeadingSlash());
	}

	@Test
	void rejectsNaturalLanguageAndControlOnlyContent() {
		assertTrue(ClipboardCommandParser.parse("这是一段普通文本").candidates().isEmpty());
		assertTrue(ClipboardCommandParser.parse("\u0000\r\n").candidates().isEmpty());
	}

	@Test
	void truncatesPreviewButRetainsSafeOriginalForCopy() {
		String original = "/say " + "x".repeat(400);
		ClipboardCommandCandidate candidate =
				ClipboardCommandParser.parse(original).candidates().getFirst();

		assertEquals(original, candidate.command());
		assertTrue(candidate.preview().endsWith("…"));
		assertTrue(candidate.preview().length() < candidate.command().length());
	}
}
