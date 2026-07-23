package io.github.ikunkk02.chatcanvas.chat.identity;

import io.github.ikunkk02.chatcanvas.chat.style.TextIndexing;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class PluginChatFallbackResolver {
	private static final int MAX_HEADER_START = 64;

	private PluginChatFallbackResolver() {
	}

	public static Optional<ChatMessageMetadata> resolve(
			Text message, Collection<PlayerChatIdentity> onlinePlayers) {
		if (message == null || onlinePlayers == null || onlinePlayers.isEmpty()) {
			return Optional.empty();
		}
		if (message.getContent() instanceof TranslatableTextContent) {
			return Optional.empty();
		}

		String plain = message.getString();
		List<PlayerChatIdentity> players = new ArrayList<>(onlinePlayers);
		players.sort(Comparator.comparingInt((PlayerChatIdentity player) ->
				player.playerName().length()).reversed());

		List<ChatMessageMetadata> matches = new ArrayList<>();
		int longest = -1;
		for (PlayerChatIdentity player : players) {
			String name = player.playerName();
			if (name.isBlank() || name.length() < longest) continue;
			for (int start = indexOfIgnoreCase(plain, name, 0);
				 start >= 0;
				 start = indexOfIgnoreCase(plain, name, start + 1)) {
				int end = start + name.length();
				if (start > MAX_HEADER_START
						|| !PlayerIdentityResolver.hasBoundaries(plain, start, end)
						|| !hasChatDelimiter(plain, end)) {
					continue;
				}
				if (name.length() > longest) {
					matches.clear();
					longest = name.length();
				}
				var range = TextIndexing.utf16RangeToCodePoints(plain, start, end);
				matches.add(new ChatMessageMetadata(
						new PlayerChatIdentity(player.uuid(), player.playerName(), false),
						range.startCodePoint(),
						range.endCodePoint()
				));
			}
		}
		if (matches.size() != 1) return Optional.empty();
		return Optional.of(matches.getFirst());
	}

	private static boolean hasChatDelimiter(String text, int afterName) {
		int index = afterName;
		while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;
		if (index >= text.length()) return false;
		char delimiter = text.charAt(index);
		return delimiter == ':' || delimiter == '：' || delimiter == '>' || delimiter == '»';
	}

	private static int indexOfIgnoreCase(String text, String needle, int from) {
		return text.toLowerCase(Locale.ROOT)
				.indexOf(needle.toLowerCase(Locale.ROOT), Math.max(0, from));
	}
}
