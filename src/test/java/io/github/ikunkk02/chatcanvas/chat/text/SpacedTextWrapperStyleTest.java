package io.github.ikunkk02.chatcanvas.chat.text;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpacedTextWrapperStyleTest {
	private static final Font FIXED_WIDTH_RENDERER = new Font(id -> null, false) {
		private final StringSplitter handler = new StringSplitter((codePoint, style) -> 1.0f);

		@Override
		public StringSplitter getSplitter() {
			return handler;
		}
	};

	@Test
	void wrappingKeepsEveryCharactersOriginalStyle() {
		Style plain = Style.EMPTY.withColor(0xFFFFFF);
		Style accept = interactive(
				ClickEvent.Action.RUN_COMMAND, "/tpaccept");
		Style deny = interactive(
				ClickEvent.Action.RUN_COMMAND, "/tpdeny");
		List<StyledGlyph> source = new ArrayList<>();
		append(source, "PlayerB ", plain);
		append(source, "AAAAAAAAAAAAAAAA", accept);
		append(source, " ", plain);
		append(source, "[deny]", deny);

		List<FormattedCharSequence> wrapped = SpacedTextWrapper.wrap(
				FIXED_WIDTH_RENDERER, List.of(ordered(source)), 8, 0.0);
		List<StyledGlyph> actual = flatten(wrapped);

		assertTrue(wrapped.size() >= 3, "the interactive segment must cross line boundaries");
		assertEquals(source.size(), actual.size());
		for (int index = 0; index < source.size(); index++) {
			assertEquals(source.get(index).codePoint(), actual.get(index).codePoint());
			assertSame(source.get(index).style(), actual.get(index).style());
		}
		assertEquals("/tpaccept", actual.get(8).style().getClickEvent().getValue());
		assertEquals("/tpaccept", actual.get(20).style().getClickEvent().getValue());
		assertEquals("/tpdeny", actual.getLast().style().getClickEvent().getValue());
	}

	@Test
	void characterSpacingDoesNotMergeIndependentInteractionStyles() {
		Style first = interactive(
				ClickEvent.Action.SUGGEST_COMMAND, "/first");
		Style second = interactive(
				ClickEvent.Action.COPY_TO_CLIPBOARD, "second");
		List<StyledGlyph> source = new ArrayList<>();
		append(source, "[first]", first);
		append(source, " ", Style.EMPTY);
		append(source, "[second]", second);

		FormattedCharSequence line = ordered(source);
		List<StyledGlyph> actual = flatten(SpacedTextWrapper.wrap(
				FIXED_WIDTH_RENDERER, List.of(line), 100, 0.75));

		assertSame(first, actual.get(0).style());
		assertSame(first, actual.get(6).style());
		assertSame(second, actual.get(8).style());
		assertSame(second, actual.getLast().style());
		assertEquals(ClickEvent.Action.SUGGEST_COMMAND,
				actual.get(0).style().getClickEvent().getAction());
		assertEquals(ClickEvent.Action.COPY_TO_CLIPBOARD,
				actual.getLast().style().getClickEvent().getAction());
		assertSame(first, SpacedTextHitTester.styleAt(
				FIXED_WIDTH_RENDERER, line, 0.75, 0.25));
		double whitespaceX = SpacedTextMetrics.xAtCodePoint(
				FIXED_WIDTH_RENDERER, line, 0.75, 7);
		assertSame(Style.EMPTY, SpacedTextHitTester.styleAt(
				FIXED_WIDTH_RENDERER, line, 0.75, whitespaceX + 0.25));
		double secondX = SpacedTextMetrics.xAtCodePoint(
				FIXED_WIDTH_RENDERER, line, 0.75, 8);
		assertSame(second, SpacedTextHitTester.styleAt(
				FIXED_WIDTH_RENDERER, line, 0.75, secondX + 0.25));
	}

	private static Style interactive(
			ClickEvent.Action action, String value) {
		return Style.EMPTY
				.withClickEvent(new ClickEvent(action, value))
				.withInsertion(value)
				.withColor(0x44CCFF)
				.withBold(true)
				.withItalic(true)
				.withUnderlined(true)
				.withStrikethrough(true)
				.withObfuscated(true);
	}

	private static void append(List<StyledGlyph> target, String text, Style style) {
		text.codePoints().forEach(codePoint -> target.add(new StyledGlyph(codePoint, style)));
	}

	private static FormattedCharSequence ordered(List<StyledGlyph> glyphs) {
		return visitor -> {
			int utf16 = 0;
			for (StyledGlyph glyph : glyphs) {
				if (!visitor.accept(utf16, glyph.style(), glyph.codePoint())) return false;
				utf16 += Character.charCount(glyph.codePoint());
			}
			return true;
		};
	}

	private static List<StyledGlyph> flatten(List<FormattedCharSequence> lines) {
		List<StyledGlyph> result = new ArrayList<>();
		for (FormattedCharSequence line : lines) {
			line.accept((index, style, codePoint) -> {
				result.add(new StyledGlyph(codePoint, style));
				return true;
			});
		}
		return result;
	}

	private record StyledGlyph(int codePoint, Style style) {
	}
}
