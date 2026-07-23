package io.github.ikunkk02.chatcanvas.chat.text;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

import java.util.ArrayList;
import java.util.List;

public final class SpacedTextWrapper {
	private SpacedTextWrapper() {
	}

	public static List<OrderedText> wrap(
			TextRenderer renderer, List<OrderedText> logicalLines, int width, double spacing) {
		if (Math.abs(spacing) < 0.00001) return logicalLines;
		List<OrderedText> result = new ArrayList<>();
		for (OrderedText line : logicalLines) {
			wrapLine(renderer, line, Math.max(1, width), spacing, result);
		}
		return result.isEmpty() ? List.of(OrderedText.EMPTY) : List.copyOf(result);
	}

	private static void wrapLine(
			TextRenderer renderer, OrderedText text, int width, double spacing,
			List<OrderedText> output) {
		List<Atom> atoms = collect(renderer, text);
		if (atoms.isEmpty()) {
			output.add(OrderedText.EMPTY);
			return;
		}
		int start = 0;
		boolean continuation = false;
		while (start < atoms.size()) {
			int end = start;
			int lastWhitespace = -1;
			while (end < atoms.size()) {
				Atom atom = atoms.get(end);
				double candidateWidth = widthOf(
						renderer, atoms, start, end + 1, continuation, spacing);
				if (end > start && candidateWidth > width) break;
				if (Character.isWhitespace(atom.codePoint())) lastWhitespace = end;
				end++;
			}
			if (end < atoms.size() && lastWhitespace >= start) {
				end = lastWhitespace + 1;
			}
			if (end <= start) end = start + 1;
			List<Atom> slice = new ArrayList<>();
			if (continuation) slice.add(new Atom(' ', Style.EMPTY,
					renderer.getTextHandler().getWidth(OrderedText.styled(' ', Style.EMPTY))));
			slice.addAll(atoms.subList(start, end));
			output.add(asOrderedText(List.copyOf(slice)));
			start = end;
			continuation = true;
		}
	}

	private static double widthOf(
			TextRenderer renderer, List<Atom> atoms, int start, int end,
			boolean continuation, double spacing) {
		int prefix = continuation ? 1 : 0;
		double[] advances = new double[prefix + end - start];
		int target = 0;
		if (continuation) {
			advances[target++] = renderer.getTextHandler()
					.getWidth(OrderedText.styled(' ', Style.EMPTY));
		}
		for (int index = start; index < end; index++) {
			advances[target++] = atoms.get(index).vanillaAdvance();
		}
		return SpacedAdvanceMath.width(advances, spacing);
	}

	private static List<Atom> collect(TextRenderer renderer, OrderedText text) {
		List<Atom> atoms = new ArrayList<>();
		text.accept((index, style, codePoint) -> {
			Style safe = style == null ? Style.EMPTY : style;
			atoms.add(new Atom(
					codePoint,
					safe,
					renderer.getTextHandler().getWidth(OrderedText.styled(codePoint, safe))));
			return true;
		});
		return atoms;
	}

	private static OrderedText asOrderedText(List<Atom> atoms) {
		return visitor -> {
			int utf16 = 0;
			for (Atom atom : atoms) {
				if (!visitor.accept(utf16, atom.style(), atom.codePoint())) return false;
				utf16 += Character.charCount(atom.codePoint());
			}
			return true;
		};
	}

	private record Atom(int codePoint, Style style, float vanillaAdvance) {
	}
}
