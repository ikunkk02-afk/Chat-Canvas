package io.github.ikunkk02.chatcanvas.voice;

import java.util.stream.Collectors;

public final class VoiceEncodingDiagnostics {
	public static final String DEBUG_PROPERTY = "chatcanvas.debug.voiceEncoding";

	private VoiceEncodingDiagnostics() {
	}

	public static boolean enabled() {
		return Boolean.getBoolean(DEBUG_PROPERTY);
	}

	public static String describeCodePoints(String value) {
		if (value == null) return "null";
		return value.codePoints()
				.mapToObj(codePoint -> String.format("U+%04X", codePoint))
				.collect(Collectors.joining(" "));
	}
}
