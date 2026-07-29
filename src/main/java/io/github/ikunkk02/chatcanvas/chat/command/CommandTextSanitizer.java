package io.github.ikunkk02.chatcanvas.chat.command;

public final class CommandTextSanitizer {
	public static final int MAX_COMMAND_LENGTH = 256;
	public static final int MAX_CLIPBOARD_COMMAND_LENGTH = 8192;

	private CommandTextSanitizer() {
	}

	public static String normalize(String value) {
		return normalize(value, MAX_COMMAND_LENGTH);
	}

	public static String normalizeForClipboard(String value) {
		return normalize(value, MAX_CLIPBOARD_COMMAND_LENGTH);
	}

	private static String normalize(String value, int limit) {
		if (value == null) return "";
		StringBuilder result = new StringBuilder(Math.min(value.length(), limit));
		boolean previousSpace = false;
		for (int offset = 0; offset < value.length()
				&& result.length() < limit;) {
			int codePoint = value.codePointAt(offset);
			offset += Character.charCount(codePoint);
			if (codePoint == '\r' || codePoint == '\n' || codePoint == '\t') {
				codePoint = ' ';
			} else if (Character.isISOControl(codePoint)) {
				continue;
			}
			boolean space = Character.isWhitespace(codePoint);
			if (space) {
				if (result.isEmpty() || previousSpace) continue;
				result.append(' ');
			} else {
				result.appendCodePoint(codePoint);
			}
			previousSpace = space;
		}
		return result.toString().strip();
	}

	public static String normalizeCommand(String value) {
		String normalized = normalize(value);
		if (normalized.isEmpty()) return "";
		if (!normalized.startsWith("/")) normalized = "/" + normalized;
		return normalized.length() <= MAX_COMMAND_LENGTH
				? normalized : normalized.substring(0, MAX_COMMAND_LENGTH);
	}

	public static String normalizeClipboardCommand(String value) {
		String normalized = normalizeForClipboard(value);
		if (normalized.isEmpty()) return "";
		return normalized.startsWith("/") ? normalized : "/" + normalized;
	}

	public static String commandName(String command) {
		String normalized = normalizeCommand(command);
		if (normalized.length() <= 1) return "";
		int end = 1;
		while (end < normalized.length()
				&& !Character.isWhitespace(normalized.charAt(end))) end++;
		return normalized.substring(1, end);
	}
}
