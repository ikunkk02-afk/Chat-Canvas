package io.github.ikunkk02.chatcanvas.chat.interaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MentionInsertionControllerTest {
	@Test
	void insertsAtCursorWithOnlyNecessarySpaces() {
		var result = MentionInsertionController.plan("helloworld", 5, 5, 256, "Steve");
		assertTrue(result.successful());
		assertEquals("hello @Steve world", result.text());
		assertEquals("hello @Steve ".length(), result.cursorUtf16());
	}

	@Test
	void replacesSelectionAndKeepsExistingWhitespace() {
		var result = MentionInsertionController.plan("hello old world", 6, 9, 256, "Steve");
		assertEquals("hello @Steve world", result.text());
	}

	@Test
	void addsTrailingSpaceAtEndWithoutDuplicatingExistingSpace() {
		assertEquals("hello @Steve ",
				MentionInsertionController.plan("hello", 5, 5, 256, "Steve").text());
		assertEquals("hello @Steve ",
				MentionInsertionController.plan("hello ", 6, 6, 256, "Steve").text());
	}

	@Test
	void rejectsWholeInsertionWhenMaximumLengthWouldBeExceeded() {
		var result = MentionInsertionController.plan("12345", 5, 5, 10, "Steve");
		assertEquals(MentionInsertionController.Status.TOO_LONG, result.status());
		assertFalse(result.successful());
	}
}
