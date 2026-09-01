package io.github.ikunkk02.chatcanvas.chat.text;

import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.util.FormattedCharSequence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpacedTextWrapperStyleTest {
	@BeforeAll
	static void bootstrapMinecraftRegistries() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	private static final GlyphInfo FIXED_ADVANCE = new GlyphInfo() {
		@Override
		public float getAdvance() {
			return 1.0f;
		}

		@Override
		public float getAdvance(boolean bold) {
			return 1.0f;
		}
	};
	private static final BakedGlyph FIXED_GLYPH = new BakedGlyph() {
		@Override
		public GlyphInfo info() {
			return FIXED_ADVANCE;
		}

		@Override
		public net.minecraft.client.gui.font.TextRenderable.Styled createGlyph(
				float x, float y, int color, int packedLight, Style style, float boldOffset, float shadowOffset) {
			throw new UnsupportedOperationException("Rendering is not used by this test");
		}
	};
	private static final GlyphSource FIXED_GLYPHS = new GlyphSource() {
		@Override
		public BakedGlyph getGlyph(int codePoint) {
			return FIXED_GLYPH;
		}

		@Override
		public BakedGlyph getRandomGlyph(net.minecraft.util.RandomSource random, int codePoint) {
			return FIXED_GLYPH;
		}
	};
	private static final Font FIXED_WIDTH_RENDERER = new Font(new Font.Provider() {
		@Override
		public GlyphSource glyphs(FontDescription description) {
			return FIXED_GLYPHS;
		}

		@Override
		public net.minecraft.client.gui.font.glyphs.EffectGlyph effect() {
			return null;
		}
	});

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
		assertEquals("/tpaccept", clickValue(actual.get(8).style().getClickEvent()));
		assertEquals("/tpaccept", clickValue(actual.get(20).style().getClickEvent()));
		assertEquals("/tpdeny", clickValue(actual.getLast().style().getClickEvent()));
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
				actual.get(0).style().getClickEvent().action());
		assertEquals(ClickEvent.Action.COPY_TO_CLIPBOARD,
				actual.getLast().style().getClickEvent().action());
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
		ClickEvent event = switch (action) {
			case RUN_COMMAND -> new ClickEvent.RunCommand(value);
			case SUGGEST_COMMAND -> new ClickEvent.SuggestCommand(value);
			case COPY_TO_CLIPBOARD -> new ClickEvent.CopyToClipboard(value);
			default -> throw new IllegalArgumentException("Unsupported test click action: " + action);
		};
		return Style.EMPTY
				.withClickEvent(event)
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

	private static String clickValue(ClickEvent event) {
		return switch (event) {
			case ClickEvent.RunCommand command -> command.command();
			case ClickEvent.SuggestCommand command -> command.command();
			case ClickEvent.CopyToClipboard clipboard -> clipboard.value();
			default -> throw new AssertionError("Unsupported test click event: " + event);
		};
	}

	private record StyledGlyph(int codePoint, Style style) {
	}
}
