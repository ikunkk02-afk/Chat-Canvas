package io.github.ikunkk02.chatcanvas.voice;

public final class VoiceTextSanitizer {
	private VoiceTextSanitizer() {
	}

	public static String sanitize(String input, boolean addFinalPunctuation) {
		if (input == null || input.isBlank()) return "";
		StringBuilder cleaned = new StringBuilder(input.length());
		boolean pendingSpace = false;
		for (int offset = 0; offset < input.length();) {
			int codePoint = input.codePointAt(offset);
			offset += Character.charCount(codePoint);
			if (Character.isISOControl(codePoint)) continue;
			if (Character.isWhitespace(codePoint)) {
				pendingSpace = cleaned.length() > 0;
				continue;
			}
			if (pendingSpace && cleaned.length() > 0) {
				int previous = cleaned.codePointBefore(cleaned.length());
				if (!(isHan(previous) && isHan(codePoint))) cleaned.append(' ');
			}
			pendingSpace = false;
			cleaned.appendCodePoint(codePoint);
		}
		String result = cleaned.toString().strip();
		if (addFinalPunctuation && !result.isEmpty()
				&& "。！？!?；;.".indexOf(result.codePointBefore(result.length())) < 0) {
			result += "。";
		}
		return result;
	}

	private static boolean isHan(int codePoint) {
		return Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN;
	}
}
