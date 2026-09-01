package io.github.ikunkk02.chatcanvas.chat.style;

import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.util.FormattedCharSequence;

public final class StyledRangePipeline {
	public static final int PLAYER_COLOR_PRIORITY = 10;
	public static final int MENTION_PRIORITY = 20;

	private final MentionStyleProvider mentionStyles = new MentionStyleProvider();

	public FormattedCharSequence apply(
			FormattedCharSequence original,
			TextRange playerNameRange,
			OptionalInt playerColor,
			List<TextRange> mentionRanges,
			MentionConfig mentionConfig
	) {
		List<StyledRange> ranges = new ArrayList<>();
		if (playerNameRange != null && playerColor != null && playerColor.isPresent()) {
			int rgb = playerColor.getAsInt();
			ranges.add(new StyledRange(
					playerNameRange, PLAYER_COLOR_PRIORITY, style -> style.withColor(rgb)));
		}
		MentionConfig safeMention = mentionConfig == null ? MentionConfig.DEFAULT : mentionConfig;
		if (safeMention.highlightEnabled() && mentionRanges != null) {
			for (TextRange range : mentionRanges) {
				ranges.add(new StyledRange(
						range, MENTION_PRIORITY, mentionStyles.overlay(safeMention)));
			}
		}
		return OrderedTextStyleOverlay.apply(original, ranges);
	}
}
