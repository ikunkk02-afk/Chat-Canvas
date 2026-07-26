package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerColorRuntime;
import io.github.ikunkk02.chatcanvas.chat.mention.MentionMatcher;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasChannel;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessage;
import io.github.ikunkk02.chatcanvas.chat.style.StyledRangePipeline;
import io.github.ikunkk02.chatcanvas.chat.style.TextRange;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextMetrics;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextWrapper;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;

public final class ChannelMessageLayoutEngine {
	private static final int MAX_ENTRIES = 1_024;
	private static final ChannelMessageLayoutEngine INSTANCE = new ChannelMessageLayoutEngine();
	private final Map<Key, Layout> cache = new LinkedHashMap<>(128, .75f, true);
	private final StyledRangePipeline styles = new StyledRangePipeline();
	private long resourceEpoch;

	private ChannelMessageLayoutEngine() {}

	public static ChannelMessageLayoutEngine instance() {
		return INSTANCE;
	}

	public synchronized Layout layout(ChatCanvasMessage message, TextRenderer renderer,
									  int availablePixels, ChatTextConfig config,
									  int lineHeight, int messageSpacing, long historyEpoch) {
		int glyphWidth = Math.max(1, (int) Math.floor(availablePixels / config.fontScale()));
		Key key = new Key(message.messageId(), message.channel(), glyphWidth,
				Double.doubleToLongBits(config.fontScale()),
				Double.doubleToLongBits(config.characterSpacing()),
				Double.doubleToLongBits(config.lineSpacing()),
				lineHeight, messageSpacing, historyEpoch, resourceEpoch);
		Layout cached = cache.get(key);
		if (cached != null) return cached;
		OrderedText styled = styled(message);
		List<OrderedText> lines = SpacedTextWrapper.wrap(
				renderer, List.of(styled), glyphWidth, config.characterSpacing());
		int width = 0;
		for (OrderedText line : lines) {
			width = Math.max(width, (int) Math.ceil(
					SpacedTextMetrics.width(renderer, line, config.characterSpacing())
							* config.fontScale()));
		}
		Layout result = new Layout(lines, width,
				lines.size() * lineHeight + Math.max(0, messageSpacing));
		cache.put(key, result);
		trim();
		return result;
	}

	public synchronized void invalidateResources() {
		resourceEpoch++;
		cache.clear();
	}

	public synchronized void clearWorld() {
		cache.clear();
	}

	private OrderedText styled(ChatCanvasMessage message) {
		OrderedText original = message.content().asOrderedText();
		if (message.channel() != ChatCanvasChannel.PLAYER_CHAT) return original;
		String plain = message.content().getString();
		String name = message.senderName() == null ? "" : message.senderName().getString();
		int nameStart = name.isEmpty() ? -1 : plain.indexOf(name);
		TextRange nameRange = nameStart < 0 ? null : new TextRange(nameStart, nameStart + name.length());
		OptionalInt color = nameRange == null ? OptionalInt.empty()
				: PlayerColorRuntime.provider().colorFor(new PlayerChatIdentity(
				message.senderUuid(), name, true));
		List<TextRange> mentions = MentionMatcher.findMentions(
				plain,
				net.minecraft.client.MinecraftClient.getInstance().player == null ? ""
						: net.minecraft.client.MinecraftClient.getInstance().player
						.getGameProfile().getName(),
				ChatCanvasConfig.instance().mention().requireAtSymbol());
		return styles.apply(original, nameRange, color, mentions, ChatCanvasConfig.instance().mention());
	}

	private void trim() {
		Iterator<Key> iterator = cache.keySet().iterator();
		while (cache.size() > MAX_ENTRIES && iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	public record Layout(List<OrderedText> lines, int width, int height) {}

	private record Key(UUID id, ChatCanvasChannel channel, int width, long scale,
					   long spacing, long lineSpacing, int lineHeight,
					   int messageSpacing, long historyEpoch, long resourceEpoch) {}
}
