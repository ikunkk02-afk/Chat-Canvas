package io.github.ikunkk02.chatcanvas.config;

import java.util.List;

public record ChatCanvasSettings(
		LayoutConfig layout,
		ChatTextConfig text,
		ChatBackgroundConfig background,
		PlayerColorConfig playerColors,
		MentionConfig mention,
		List<Integer> recentColors
) {
	public static final ChatCanvasSettings DEFAULT = new ChatCanvasSettings(
			LayoutConfig.DEFAULT,
			ChatTextConfig.DEFAULT,
			ChatBackgroundConfig.DEFAULT,
			PlayerColorConfig.DEFAULT,
			MentionConfig.DEFAULT,
			List.of()
	);

	public ChatCanvasSettings(LayoutConfig layout, ChatTextConfig text) {
		this(layout, text, ChatBackgroundConfig.DEFAULT, PlayerColorConfig.DEFAULT,
				MentionConfig.DEFAULT, List.of());
	}

	public ChatCanvasSettings(LayoutConfig layout, ChatTextConfig text,
							  ChatBackgroundConfig background) {
		this(layout, text, background, PlayerColorConfig.DEFAULT, MentionConfig.DEFAULT, List.of());
	}

	public ChatCanvasSettings(LayoutConfig layout, ChatTextConfig text,
							  ChatBackgroundConfig background, List<Integer> recentColors) {
		this(layout, text, background, PlayerColorConfig.DEFAULT, MentionConfig.DEFAULT, recentColors);
	}

	public ChatCanvasSettings(LayoutConfig layout, ChatTextConfig text,
							  ChatBackgroundConfig background, PlayerColorConfig playerColors,
							  List<Integer> recentColors) {
		this(layout, text, background, playerColors, MentionConfig.DEFAULT, recentColors);
	}

	public ChatCanvasSettings {
		recentColors = RecentColorStore.sanitizedCopy(recentColors);
	}

	public ChatCanvasSettings sanitized() {
		return new ChatCanvasSettings(
				layout == null ? LayoutConfig.DEFAULT : layout.sanitized(),
				text == null ? ChatTextConfig.DEFAULT : text.sanitized(),
				background == null ? ChatBackgroundConfig.DEFAULT : background.sanitized(),
				playerColors == null ? PlayerColorConfig.DEFAULT : playerColors.sanitized(),
				mention == null ? MentionConfig.DEFAULT : mention.sanitized(),
				recentColors
		);
	}
}
