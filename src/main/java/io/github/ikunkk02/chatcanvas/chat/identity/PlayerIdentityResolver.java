package io.github.ikunkk02.chatcanvas.chat.identity;

import io.github.ikunkk02.chatcanvas.chat.style.TextIndexing;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;

public final class PlayerIdentityResolver {
	private PlayerIdentityResolver() {
	}

	public static Optional<ChatMessageMetadata> resolveStandard(
			Component message, Component senderComponent, UUID uuid, String playerName) {
		if (message == null || senderComponent == null || playerName == null || playerName.isBlank()) {
			return Optional.empty();
		}
		String senderText = senderComponent.getString();
		int localName = boundedIndexOf(senderText, playerName, 0);
		if (localName < 0) return Optional.empty();

		String fullText = message.getString();
		int senderStart = indexOfIgnoreCase(fullText, senderText, 0);
		int nameStart = senderStart >= 0
				? senderStart + localName
				: boundedIndexOf(fullText, playerName, 0);
		if (nameStart < 0) return Optional.empty();
		var range = TextIndexing.utf16RangeToCodePoints(
				fullText, nameStart, nameStart + playerName.length());
		return Optional.of(new ChatMessageMetadata(
				new PlayerChatIdentity(uuid, playerName, true),
				range.startCodePoint(),
				range.endCodePoint()
		));
	}

	public static Optional<ChatMessageMetadata> revalidate(
			Component finalMessage, ChatMessageMetadata metadata) {
		if (finalMessage == null || metadata == null) return Optional.empty();
		String text = finalMessage.getString();
		String name = metadata.sender().playerName();
		int preferredUtf16 = TextIndexing.codePointToUtf16(text, metadata.nameStart());
		int exact = boundedIndexOf(text, name, preferredUtf16);
		if (exact < 0) exact = boundedIndexOf(text, name, 0);
		if (exact < 0) return Optional.empty();
		var range = TextIndexing.utf16RangeToCodePoints(text, exact, exact + name.length());
		return Optional.of(new ChatMessageMetadata(
				metadata.sender(), range.startCodePoint(), range.endCodePoint()));
	}

	public static int boundedIndexOf(String text, String needle, int preferredStart) {
		if (text == null || needle == null || needle.isEmpty()) return -1;
		int from = Math.max(0, Math.min(preferredStart, text.length()));
		int match = indexOfIgnoreCase(text, needle, from);
		if (match >= 0 && hasBoundaries(text, match, match + needle.length())) return match;
		for (match = indexOfIgnoreCase(text, needle, 0);
			 match >= 0;
			 match = indexOfIgnoreCase(text, needle, match + 1)) {
			if (hasBoundaries(text, match, match + needle.length())) return match;
		}
		return -1;
	}

	public static boolean hasBoundaries(String text, int start, int end) {
		if (start < 0 || end > text.length() || start >= end) return false;
		boolean left = start == 0 || !isNameCharacter(text.charAt(start - 1));
		boolean right = end == text.length() || !isNameCharacter(text.charAt(end));
		return left && right;
	}

	private static boolean isNameCharacter(char value) {
		return Character.isLetterOrDigit(value) || value == '_';
	}

	private static int indexOfIgnoreCase(String text, String needle, int from) {
		return text.toLowerCase(Locale.ROOT)
				.indexOf(needle.toLowerCase(Locale.ROOT), Math.max(0, from));
	}
}
