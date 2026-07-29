package io.github.ikunkk02.chatcanvas.chat.style;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextIndexingTest {
	@Test
	void convertsBetweenUtf16AndCodePointIndices() {
		String text = "中文 😀 @Player";
		int utf16 = text.indexOf('@');
		int codePoint = text.codePointCount(0, utf16);
		assertEquals(codePoint, TextIndexing.utf16ToCodePoint(text, utf16));
		assertEquals(utf16, TextIndexing.codePointToUtf16(text, codePoint));
	}
}
