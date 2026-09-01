package io.github.ikunkk02.chatcanvas.chat.style;

public final class TextIndexing {
	private TextIndexing() {
	}

	public static int utf16ToCodePoint(String text, int utf16Index) {
		if (text == null || text.isEmpty()) return 0;
		int safe = Math.max(0, Math.min(text.length(), utf16Index));
		return text.codePointCount(0, safe);
	}

	public static int codePointToUtf16(String text, int codePointIndex) {
		if (text == null || text.isEmpty()) return 0;
		int count = text.codePointCount(0, text.length());
		int safe = Math.max(0, Math.min(count, codePointIndex));
		return text.offsetByCodePoints(0, safe);
	}

	public static TextRange utf16RangeToCodePoints(String text, int start, int end) {
		return new TextRange(utf16ToCodePoint(text, start), utf16ToCodePoint(text, end));
	}
}
