package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasSettings;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.LayoutConfig;
import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;

public record EditorSnapshot(
		LayoutConfig layout,
		ChatTextConfig text,
		ChatBackgroundConfig background,
		PlayerColorConfig playerColors,
		MentionConfig mention
) {
	public EditorSnapshot(LayoutConfig layout, ChatTextConfig text) {
		this(layout, text, ChatBackgroundConfig.DEFAULT, PlayerColorConfig.DEFAULT,
				MentionConfig.DEFAULT);
	}

	public EditorSnapshot(LayoutConfig layout, ChatTextConfig text, ChatBackgroundConfig background) {
		this(layout, text, background, PlayerColorConfig.DEFAULT, MentionConfig.DEFAULT);
	}

	public EditorSnapshot(LayoutConfig layout, ChatTextConfig text, ChatBackgroundConfig background,
						  PlayerColorConfig playerColors) {
		this(layout, text, background, playerColors, MentionConfig.DEFAULT);
	}

	public EditorSnapshot {
		layout = layout == null ? LayoutConfig.DEFAULT : layout.sanitized();
		text = text == null ? ChatTextConfig.DEFAULT : text.sanitized();
		background = background == null ? ChatBackgroundConfig.DEFAULT : background.sanitized();
		playerColors = playerColors == null ? PlayerColorConfig.DEFAULT : playerColors.sanitized();
		mention = mention == null ? MentionConfig.DEFAULT : mention.sanitized();
	}

	public ChatCanvasSettings settings() {
		return new ChatCanvasSettings(
				layout, text, background, playerColors, mention, java.util.List.of());
	}
}
