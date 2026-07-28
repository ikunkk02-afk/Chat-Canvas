package io.github.ikunkk02.chatcanvas.voice;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class VoskResultParserTest {
	private final VoskResultParser parser = new VoskResultParser(new Gson());

	@Test
	void parsesChinesePartialWithoutCharsetRoundTrip() {
		String json = """
				{
				  "partial": "你好，我们去下界"
				}
				""";

		assertEquals("你好，我们去下界", parser.parsePartial(json));
	}

	@Test
	void parsesChineseFinalResultWithoutCharsetRoundTrip() {
		String json = """
				{
				  "text": "苦力怕在后面"
				}
				""";

		assertEquals("苦力怕在后面", parser.parseFinal(json));
		assertEquals("苦力怕在后面", parser.parseResult(json));
	}

	@Test
	void preservesMixedChineseLatinNumbersAndEmoji() {
		String expected = "你好 Steve，我们去坐标 100 64 200 😀";
		String json = """
				{"text":"你好 Steve，我们去坐标 100 64 200 😀"}
				""";

		assertEquals(expected, parser.parseFinal(json));
	}

	@Test
	void preservesEveryUnicodeCodePoint() {
		String expected = "你好 Steve，我们去坐标 100 64 200 😀";
		String parsed = parser.parseFinal(
				"{\"text\":\"你好 Steve，我们去坐标 100 64 200 😀\"}");

		assertArrayEquals(expected.codePoints().toArray(), parsed.codePoints().toArray());
	}
}
