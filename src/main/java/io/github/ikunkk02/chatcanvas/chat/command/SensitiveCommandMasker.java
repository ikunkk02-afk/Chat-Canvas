package io.github.ikunkk02.chatcanvas.chat.command;

public final class SensitiveCommandMasker {
	private SensitiveCommandMasker() {
	}

	public static String display(String commandWithoutSlash) {
		String raw = commandWithoutSlash == null ? "" : commandWithoutSlash.strip();
		String withSlash = raw.startsWith("/") ? raw : "/" + raw;
		if (!SensitiveCommandDetector.isSensitive(withSlash)) return withSlash;
		int separator = firstWhitespace(withSlash);
		return separator < 0 ? withSlash : withSlash.substring(0, separator) + " ******";
	}

	private static int firstWhitespace(String value) {
		for (int index = 0; index < value.length(); index++) {
			if (Character.isWhitespace(value.charAt(index))) return index;
		}
		return -1;
	}
}
