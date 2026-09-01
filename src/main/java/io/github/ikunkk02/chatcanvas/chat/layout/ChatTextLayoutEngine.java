package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextWrapper;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;

/**
 * Bounded cache for the styled line layout owned by vanilla {@code ChatHud}.
 * The source message is compared by identity so two equal messages remain two
 * independently cached entries.
 */
public final class ChatTextLayoutEngine {
	public static final int MAX_ENTRIES = 1_024;
	private static final ChatTextLayoutEngine INSTANCE = new ChatTextLayoutEngine();

	private final LinkedHashMap<Key, List<FormattedCharSequence>> cache =
			new LinkedHashMap<>(128, 0.75f, true);
	private long epoch;
	private long hits;
	private long misses;

	private ChatTextLayoutEngine() {
	}

	public static ChatTextLayoutEngine instance() {
		return INSTANCE;
	}

	public synchronized List<FormattedCharSequence> wrap(
			GuiMessage message,
			Font renderer,
			int glyphWidth,
			double characterSpacing,
			double fontScale,
			Supplier<List<FormattedCharSequence>> logicalLines) {
		Key key = new Key(message, glyphWidth,
				Double.doubleToLongBits(characterSpacing),
				Double.doubleToLongBits(fontScale), epoch);
		List<FormattedCharSequence> cached = cache.get(key);
		if (cached != null) {
			hits++;
			return cached;
		}
		misses++;
		List<FormattedCharSequence> wrapped = SpacedTextWrapper.wrap(
				renderer, logicalLines.get(), glyphWidth, characterSpacing);
		cache.put(key, wrapped);
		trim();
		return wrapped;
	}

	public synchronized void invalidateLayout() {
		epoch++;
		cache.clear();
	}

	public synchronized void clearWorld() {
		invalidateLayout();
		hits = 0;
		misses = 0;
	}

	public synchronized int size() {
		return cache.size();
	}

	public synchronized double hitRate() {
		long total = hits + misses;
		return total == 0 ? 0.0 : (double) hits / total;
	}

	private void trim() {
		Iterator<Map.Entry<Key, List<FormattedCharSequence>>> iterator = cache.entrySet().iterator();
		while (cache.size() > MAX_ENTRIES && iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	private static final class Key {
		private final GuiMessage message;
		private final int width;
		private final long spacingBits;
		private final long scaleBits;
		private final long epoch;
		private final int hash;

		private Key(
				GuiMessage message, int width, long spacingBits,
				long scaleBits, long epoch) {
			this.message = message;
			this.width = width;
			this.spacingBits = spacingBits;
			this.scaleBits = scaleBits;
			this.epoch = epoch;
			this.hash = (((System.identityHashCode(message) * 31 + width) * 31
					+ Long.hashCode(spacingBits)) * 31 + Long.hashCode(scaleBits)) * 31
					+ Long.hashCode(epoch);
		}

		@Override
		public boolean equals(Object object) {
			return object instanceof Key other
					&& message == other.message
					&& width == other.width
					&& spacingBits == other.spacingBits
					&& scaleBits == other.scaleBits
					&& epoch == other.epoch;
		}

		@Override
		public int hashCode() {
			return hash;
		}
	}
}
