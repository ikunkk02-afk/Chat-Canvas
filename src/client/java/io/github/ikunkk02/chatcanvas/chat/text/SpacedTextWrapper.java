package io.github.ikunkk02.chatcanvas.chat.text;

import com.ibm.icu.text.BreakIterator;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SpacedTextWrapper {
	private SpacedTextWrapper() {
	}

	public static List<OrderedText> wrap(
			TextRenderer renderer, List<OrderedText> logicalLines, int width, double spacing) {
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
		List<Cluster> clusters = clusters(atoms);
		if (clusters.isEmpty()) {
			output.add(OrderedText.EMPTY);
			return;
		}
		Set<Integer> preferredBreaks = lineBreaks(atoms);
		int start = 0;
		while (start < clusters.size()) {
			int end = start;
			int lastPreferredBreak = -1;
			while (end < clusters.size()) {
				double candidateWidth = widthOf(
						clusters, start, end + 1, spacing);
				if (end > start && candidateWidth > width) break;
				if (preferredBreaks.contains(clusters.get(end).endUtf16())) {
					lastPreferredBreak = end + 1;
				}
				end++;
			}
			if (end < clusters.size() && lastPreferredBreak > start) {
				end = lastPreferredBreak;
			}
			if (end <= start) end = start + 1;
			List<Atom> slice = new ArrayList<>();
			for (int index = start; index < end; index++) {
				slice.addAll(clusters.get(index).atoms());
			}
			output.add(asOrderedText(List.copyOf(slice)));
			start = end;
		}
	}

	private static double widthOf(
			List<Cluster> clusters, int start, int end, double spacing) {
		int atomCount = 0;
		for (int index = start; index < end; index++) {
			atomCount += clusters.get(index).atoms().size();
		}
		double[] advances = new double[atomCount];
		int target = 0;
		for (int clusterIndex = start; clusterIndex < end; clusterIndex++) {
			for (Atom atom : clusters.get(clusterIndex).atoms()) {
				advances[target++] = atom.vanillaAdvance();
			}
		}
		return SpacedAdvanceMath.width(advances, spacing);
	}

	private static List<Cluster> clusters(List<Atom> atoms) {
		StringBuilder plain = new StringBuilder();
		for (Atom atom : atoms) plain.appendCodePoint(atom.codePoint());
		BreakIterator iterator = BreakIterator.getCharacterInstance(Locale.ROOT);
		iterator.setText(plain.toString());
		List<Cluster> result = new ArrayList<>();
		int atomIndex = 0;
		int start = iterator.first();
		for (int end = iterator.next(); end != BreakIterator.DONE;
			 start = end, end = iterator.next()) {
			List<Atom> members = new ArrayList<>();
			int cursor = start;
			while (atomIndex < atoms.size() && cursor < end) {
				Atom atom = atoms.get(atomIndex++);
				members.add(atom);
				cursor += Character.charCount(atom.codePoint());
			}
			result.add(new Cluster(List.copyOf(members), start, end));
		}
		return result;
	}

	private static Set<Integer> lineBreaks(List<Atom> atoms) {
		StringBuilder plain = new StringBuilder();
		for (Atom atom : atoms) plain.appendCodePoint(atom.codePoint());
		BreakIterator iterator = BreakIterator.getLineInstance(Locale.ROOT);
		iterator.setText(plain.toString());
		Set<Integer> result = new HashSet<>();
		for (int position = iterator.first(); position != BreakIterator.DONE;
			 position = iterator.next()) {
			result.add(position);
		}
		return result;
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

	private record Cluster(List<Atom> atoms, int startUtf16, int endUtf16) {
	}
}
