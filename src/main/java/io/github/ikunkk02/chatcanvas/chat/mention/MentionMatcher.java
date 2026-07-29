package io.github.ikunkk02.chatcanvas.chat.mention;

import io.github.ikunkk02.chatcanvas.chat.style.TextIndexing;
import io.github.ikunkk02.chatcanvas.chat.style.TextRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MentionMatcher {
	private MentionMatcher() {
	}

	public static List<TextRange> findMentions(
			String text, String localPlayerName, boolean requireAtSymbol) {
		if (text == null || text.isEmpty() || localPlayerName == null
				|| localPlayerName.isBlank()) {
			return List.of();
		}
		String lowerText = text.toLowerCase(Locale.ROOT);
		String lowerName = localPlayerName.toLowerCase(Locale.ROOT);
		List<TextRange> ranges = new ArrayList<>();
		int from = 0;
		while (from <= text.length() - localPlayerName.length()) {
			int nameStart = lowerText.indexOf(lowerName, from);
			if (nameStart < 0) break;
			int nameEnd = nameStart + localPlayerName.length();
			boolean hasAt = nameStart > 0 && text.charAt(nameStart - 1) == '@';
			int matchStart = hasAt ? nameStart - 1 : nameStart;
			if ((!requireAtSymbol || hasAt)
					&& validLeftBoundary(text, matchStart, hasAt)
					&& validRightBoundary(text, nameEnd)) {
				ranges.add(TextIndexing.utf16RangeToCodePoints(text, matchStart, nameEnd));
			}
			from = nameStart + Math.max(1, localPlayerName.length());
		}
		return List.copyOf(ranges);
	}

	private static boolean validLeftBoundary(String text, int matchStart, boolean hasAt) {
		if (matchStart == 0) return true;
		char previous = text.charAt(matchStart - 1);
		if (hasAt) {
			return previous != '@' && !isPlayerNameCharacter(previous);
		}
		return !isPlayerNameCharacter(previous) && previous != '@';
	}

	private static boolean validRightBoundary(String text, int nameEnd) {
		return nameEnd >= text.length() || !isPlayerNameCharacter(text.charAt(nameEnd));
	}

	public static boolean isPlayerNameCharacter(char value) {
		return value >= 'A' && value <= 'Z'
				|| value >= 'a' && value <= 'z'
				|| value >= '0' && value <= '9'
				|| value == '_';
	}
}
