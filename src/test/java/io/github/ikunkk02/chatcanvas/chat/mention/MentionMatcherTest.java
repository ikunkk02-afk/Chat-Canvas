package io.github.ikunkk02.chatcanvas.chat.mention;

import io.github.ikunkk02.chatcanvas.chat.style.TextIndexing;
import io.github.ikunkk02.chatcanvas.chat.style.TextRange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MentionMatcherTest {
	@Test
	void findsAllCaseInsensitiveMentionsWithStrictBoundaries() {
		String text = "@ShouYun hi @shouyun, @ShouYun123 abc@ShouYun_xyz @Shou";
		List<TextRange> ranges = MentionMatcher.findMentions(text, "ShouYun", true);
		assertEquals(2, ranges.size());
		assertEquals("@ShouYun", slice(text, ranges.get(0)));
		assertEquals("@shouyun", slice(text, ranges.get(1)));
	}

	@Test
	void codePointRangesRemainCorrectAfterChineseAndEmoji() {
		String text = "中文 😀 @Player，再来 @PLAYER!";
		List<TextRange> ranges = MentionMatcher.findMentions(text, "Player", true);
		assertEquals(2, ranges.size());
		assertEquals("@Player", slice(text, ranges.get(0)));
		assertEquals(text.codePointCount(0, text.indexOf('@')),
				ranges.getFirst().startCodePoint());
	}

	@Test
	void rejectsNamesThatOnlyShareTheTargetPrefix() {
		assertTrue(MentionMatcher.findMentions("@Steve123", "Steve", true).isEmpty());
		assertTrue(MentionMatcher.findMentions("@Steve_Alt", "Steve", true).isEmpty());
		assertEquals(1, MentionMatcher.findMentions("你好，@Steve！", "Steve", true).size());
	}

	@Test
	void optionalAtModeIncludesBareNamesButRejectsWordAndAtPrefixes() {
		String text = "Player @Player xPlayer Player_x @@Player";
		List<TextRange> ranges = MentionMatcher.findMentions(text, "Player", false);
		assertEquals(List.of("Player", "@Player"),
				ranges.stream().map(range -> slice(text, range)).toList());
		assertTrue(MentionMatcher.findMentions("Player", "Player", true).isEmpty());
	}

	private static String slice(String text, TextRange range) {
		return text.substring(
				TextIndexing.codePointToUtf16(text, range.startCodePoint()),
				TextIndexing.codePointToUtf16(text, range.endCodePoint()));
	}
}
