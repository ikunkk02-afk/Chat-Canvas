package io.github.ikunkk02.chatcanvas.chat.text;

import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpacedTextWrapperStyleTest {
	private static final TextRenderer FIXED_WIDTH_RENDERER = new TextRenderer(id -> null, false) {
		private final TextHandler handler = new TextHandler((codePoint, style) -> 1.0f);

		@Override
		public TextHandler getTextHandler() {
			return handler;
		}
	};

	@Test
	void wrappingKeepsEveryCharactersOriginalStyle() {
		Style plain = Style.EMPTY.withColor(0xFFFFFF);
		Style accept = interactive(
				new ClickEvent.RunCommand("/tpaccept"), "/tpaccept");
		Style deny = interactive(
				new ClickEvent.RunCommand("/tpdeny"), "/tpdeny");
		List<StyledGlyph> source = new ArrayList<>();
		append(source, "PlayerB ", plain);
		append(source, "AAAAAAAAAAAAAAAA", accept);
		append(source, " ", plain);
		append(source, "[deny]", deny);

		List<OrderedText> wrapped = SpacedTextWrapper.wrap(
				FIXED_WIDTH_RENDERER, List.of(ordered(source)), 8, 0.0);
		List<StyledGlyph> actual = flatten(wrapped);

		assertTrue(wrapped.size() >= 3, "the interactive segment must cross line boundaries");
		assertEquals(source.size(), actual.size());
		for (int index = 0; index < source.size(); index++) {
			assertEquals(source.get(index).codePoint(), actual.get(index).codePoint());
			assertSame(source.get(index).style(), actual.get(index).style());
		}
		assertEquals("/tpaccept", clickValue(actual.get(8).style().getClickEvent()));
		assertEquals("/tpaccept", clickValue(actual.get(20).style().getClickEvent()));
		assertEquals("/tpdeny", clickValue(actual.getLast().style().getClickEvent()));
	}

	@Test
	void characterSpacingDoesNotMergeIndependentInteractionStyles() {
		Style first = interactive(
				new ClickEvent.SuggestCommand("/first"), "/first");
		Style second = interactive(
				new ClickEvent.CopyToClipboard("second"), "second");
		List<StyledGlyph> source = new ArrayList<>();
		append(source, "[first]", first);
		append(source, " ", Style.EMPTY);
		append(source, "[second]", second);

		OrderedText line = ordered(source);
		List<StyledGlyph> actual = flatten(SpacedTextWrapper.wrap(
				FIXED_WIDTH_RENDERER, List.of(line), 100, 0.75));

		assertSame(first, actual.get(0).style());
		assertSame(first, actual.get(6).style());
		assertSame(second, actual.get(8).style());
		assertSame(second, actual.getLast().style());
		assertInstanceOf(ClickEvent.SuggestCommand.class,
				actual.get(0).style().getClickEvent());
		assertInstanceOf(ClickEvent.CopyToClipboard.class,
				actual.getLast().style().getClickEvent());
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
			ClickEvent event, String value) {
		return Style.EMPTY
				.withClickEvent(event)
				.withInsertion(value)
				.withColor(0x44CCFF)
				.withBold(true)
				.withItalic(true)
				.withUnderline(true)
				.withStrikethrough(true)
				.withObfuscated(true);
	}

	private static String clickValue(ClickEvent event) {
		if (event instanceof ClickEvent.RunCommand command) return command.command();
		if (event instanceof ClickEvent.SuggestCommand command) return command.command();
		if (event instanceof ClickEvent.OpenUrl url) return url.uri().toString();
		if (event instanceof ClickEvent.CopyToClipboard clipboard) return clipboard.value();
		throw new IllegalArgumentException("Unsupported test event: " + event);
	}

	private static void append(List<StyledGlyph> target, String text, Style style) {
		text.codePoints().forEach(codePoint -> target.add(new StyledGlyph(codePoint, style)));
	}

	private static OrderedText ordered(List<StyledGlyph> glyphs) {
		return visitor -> {
			int utf16 = 0;
			for (StyledGlyph glyph : glyphs) {
				if (!visitor.accept(utf16, glyph.style(), glyph.codePoint())) return false;
				utf16 += Character.charCount(glyph.codePoint());
			}
			return true;
		};
	}

	private static List<StyledGlyph> flatten(List<OrderedText> lines) {
		List<StyledGlyph> result = new ArrayList<>();
		for (OrderedText line : lines) {
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
