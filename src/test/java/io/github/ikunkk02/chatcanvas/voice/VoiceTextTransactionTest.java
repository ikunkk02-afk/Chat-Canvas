package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoiceTextTransactionTest {
	@Test
	void partialsReplaceOneProvisionalSegmentWithoutDuplication() {
		VoiceTextTransaction transaction = new VoiceTextTransaction("小明 ", 3, 3);
		VoiceTextTransaction.Edit first = transaction.updatePartial("小明 ", 3, 3, "我们今天去", 256);
		VoiceTextTransaction.Edit second = transaction.updatePartial(
				first.text(), first.cursor(), first.selectionEnd(), "我们今天去挖矿", 256);
		assertEquals("小明 我们今天去挖矿", second.text());
	}

	@Test
	void finalResultReplacesSelectionAndPreservesOriginalPrefixAndSuffix() {
		VoiceTextTransaction transaction = new VoiceTextTransaction("前缀旧文字后缀", 2, 5);
		VoiceTextTransaction.Edit result = transaction.commit("前缀旧文字后缀", 2, 5, "新语音", 256);
		assertEquals("前缀新语音后缀", result.text());
	}

	@Test
	void cancelRestoresTextReplacedByPartial() {
		VoiceTextTransaction transaction = new VoiceTextTransaction("hello world", 6, 11);
		VoiceTextTransaction.Edit partial = transaction.updatePartial("hello world", 6, 11, "voice", 256);
		VoiceTextTransaction.Edit cancelled = transaction.cancel(
				partial.text(), partial.cursor(), partial.selectionEnd());
		assertEquals("hello world", cancelled.text());
	}

	@Test
	void externalEditOutsideProvisionalIsPreservedAndDoesNotDuplicatePartial() {
		VoiceTextTransaction transaction = new VoiceTextTransaction("小明 ", 3, 3);
		VoiceTextTransaction.Edit first = transaction.updatePartial("小明 ", 3, 3, "你过来", 256);
		String userEdited = first.text() + "！";
		VoiceTextTransaction.Edit second = transaction.updatePartial(
				userEdited, userEdited.length(), userEdited.length(), "一下", 256);
		assertEquals("小明 ！一下", second.text());
	}

	@Test
	void rejectsResultOverChatLimitWithoutChangingText() {
		VoiceTextTransaction transaction = new VoiceTextTransaction("base", 4, 4);
		VoiceTextTransaction.Edit result = transaction.commit("base", 4, 4, "x".repeat(300), 256);
		assertTrue(result.limitExceeded());
		assertEquals("base", result.text());
	}
}
