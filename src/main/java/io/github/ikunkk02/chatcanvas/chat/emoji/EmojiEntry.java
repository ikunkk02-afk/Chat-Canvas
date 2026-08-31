package io.github.ikunkk02.chatcanvas.chat.emoji;

import java.util.List;
import java.util.Objects;

public record EmojiEntry(
		String unicode,
		String chineseName,
		String englishName,
		List<String> keywords,
		EmojiCategory category
) {
	public EmojiEntry {
		unicode = Objects.requireNonNull(unicode, "unicode");
		chineseName = Objects.requireNonNull(chineseName, "chineseName");
		englishName = Objects.requireNonNull(englishName, "englishName");
		keywords = List.copyOf(keywords == null ? List.of() : keywords);
		category = Objects.requireNonNull(category, "category");
		if (unicode.isBlank()) throw new IllegalArgumentException("unicode");
	}

	public String translationKey() {
		StringBuilder key = new StringBuilder("chat_canvas.emoji.name.u");
		unicode.codePoints().forEach(codePoint -> {
			if (key.charAt(key.length() - 1) != 'u') key.append('_');
			key.append(Integer.toHexString(codePoint));
		});
		return key.toString();
	}
}
