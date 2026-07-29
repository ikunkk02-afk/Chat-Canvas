package io.github.ikunkk02.chatcanvas.chat.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnicodeTextNavigatorTest {
	@Test
	void countsExtendedGraphemeClusters() {
		String text = "中😀❤️👍🏽👨‍👩‍👧‍👦🇨🇳e\u0301A";

		assertEquals(8, UnicodeTextNavigator.graphemeCount(text));
	}

	@Test
	void movesAcrossWholeEmojiClusters() {
		String family = "👨‍👩‍👧‍👦";
		String text = "A" + family + "B";
		int afterFamily = 1 + family.length();

		assertEquals(1,
				UnicodeTextNavigator.previousGraphemeBoundary(text, afterFamily));
		assertEquals(afterFamily,
				UnicodeTextNavigator.nextGraphemeBoundary(text, 1));
	}

	@Test
	void deletionRemovesOneWholeCluster() {
		String text = "中👍🏽文";
		int afterEmoji = 1 + "👍🏽".length();

		var backwards = UnicodeTextNavigator.deletePreviousGrapheme(
				text, afterEmoji, afterEmoji);
		var forwards = UnicodeTextNavigator.deleteNextGrapheme(text, 1, 1);

		assertEquals("中文", backwards.text());
		assertEquals("中文", forwards.text());
		assertTrue(UnicodeTextNavigator.isWellFormedUtf16(backwards.text()));
		assertTrue(UnicodeTextNavigator.isWellFormedUtf16(forwards.text()));
	}

	@Test
	void selectionExpandsToClusterEdges() {
		String heart = "❤️";
		String text = "A" + heart + "B";

		var result = UnicodeTextNavigator.replaceSelection(
				text, 2, 1, "😀", 256);

		assertEquals("A😀B", result.text());
		assertEquals(3, result.cursor());
	}

	@Test
	void insertionAtCursorPreservesSurroundingText() {
		var result = UnicodeTextNavigator.replaceSelection(
				"大家好", 2, 2, "😀", 256);

		assertEquals("大家😀好", result.text());
		assertEquals(4, result.cursor());
		assertFalse(result.limitExceeded());
	}

	@Test
	void rejectsInsertionThatCannotFitAsAWhole() {
		String existing = "a".repeat(255);

		var result = UnicodeTextNavigator.replaceSelection(
				existing, existing.length(), existing.length(), "😀", 256);

		assertTrue(result.limitExceeded());
		assertFalse(result.changed());
		assertEquals(existing, result.text());
	}

	@Test
	void truncatesOnlyAtAWholeCluster() {
		String text = "a".repeat(254) + "❤️" + "b";
		String truncated = UnicodeTextNavigator.truncateAtGraphemeBoundary(text, 255);

		assertEquals("a".repeat(254), truncated);
		assertTrue(UnicodeTextNavigator.isWellFormedUtf16(truncated));
	}

	@Test
	void recognizesMalformedSurrogates() {
		assertFalse(UnicodeTextNavigator.isWellFormedUtf16("\uD83D"));
		assertFalse(UnicodeTextNavigator.isWellFormedUtf16("\uDE00"));
		assertTrue(UnicodeTextNavigator.isWellFormedUtf16("😀"));
	}
}
