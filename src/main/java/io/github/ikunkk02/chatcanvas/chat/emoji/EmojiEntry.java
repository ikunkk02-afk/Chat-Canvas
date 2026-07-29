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
}
