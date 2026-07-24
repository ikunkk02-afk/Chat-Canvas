package io.github.ikunkk02.chatcanvas.config;

import io.github.ikunkk02.chatcanvas.editor.EditorUiStyle;

import java.util.List;

public record ChatCanvasSettings(
		LayoutConfig layout,
		ChatTextConfig text,
		ChatBackgroundConfig background,
		PlayerColorConfig playerColors,
		MentionConfig mention,
		CommandClipboardConfig commandClipboard,
		List<Integer> recentColors,
		EditorUiStyle editorUiStyle
) {
	public static final ChatCanvasSettings DEFAULT = new ChatCanvasSettings(
			LayoutConfig.DEFAULT,
			ChatTextConfig.DEFAULT,
			ChatBackgroundConfig.DEFAULT,
			PlayerColorConfig.DEFAULT,
			MentionConfig.DEFAULT,
			CommandClipboardConfig.DEFAULT,
			List.of(),
			EditorUiStyle.CHAT_CANVAS
	);

	public ChatCanvasSettings(LayoutConfig layout, ChatTextConfig text) {
		this(layout, text, ChatBackgroundConfig.DEFAULT, PlayerColorConfig.DEFAULT,
				MentionConfig.DEFAULT, CommandClipboardConfig.DEFAULT, List.of(),
				EditorUiStyle.CHAT_CANVAS);
	}

	public ChatCanvasSettings(LayoutConfig layout, ChatTextConfig text,
							  ChatBackgroundConfig background) {
		this(layout, text, background, PlayerColorConfig.DEFAULT, MentionConfig.DEFAULT,
				CommandClipboardConfig.DEFAULT, List.of(), EditorUiStyle.CHAT_CANVAS);
	}

	public ChatCanvasSettings(LayoutConfig layout, ChatTextConfig text,
							  ChatBackgroundConfig background, List<Integer> recentColors) {
		this(layout, text, background, PlayerColorConfig.DEFAULT, MentionConfig.DEFAULT,
				CommandClipboardConfig.DEFAULT, recentColors, EditorUiStyle.CHAT_CANVAS);
	}

	public ChatCanvasSettings(LayoutConfig layout, ChatTextConfig text,
							  ChatBackgroundConfig background, PlayerColorConfig playerColors,
							  List<Integer> recentColors) {
		this(layout, text, background, playerColors, MentionConfig.DEFAULT,
				CommandClipboardConfig.DEFAULT, recentColors, EditorUiStyle.CHAT_CANVAS);
	}

	public ChatCanvasSettings(LayoutConfig layout, ChatTextConfig text,
							  ChatBackgroundConfig background, PlayerColorConfig playerColors,
							  MentionConfig mention, List<Integer> recentColors) {
		this(layout, text, background, playerColors, mention,
				CommandClipboardConfig.DEFAULT, recentColors, EditorUiStyle.CHAT_CANVAS);
	}

	public ChatCanvasSettings(LayoutConfig layout, ChatTextConfig text,
							  ChatBackgroundConfig background, PlayerColorConfig playerColors,
							  MentionConfig mention, CommandClipboardConfig commandClipboard,
							  List<Integer> recentColors) {
		this(layout, text, background, playerColors, mention,
				commandClipboard, recentColors, EditorUiStyle.CHAT_CANVAS);
	}

	public ChatCanvasSettings {
		recentColors = RecentColorStore.sanitizedCopy(recentColors);
		if (editorUiStyle == null) editorUiStyle = EditorUiStyle.CHAT_CANVAS;
	}

	public ChatCanvasSettings sanitized() {
		return new ChatCanvasSettings(
				layout == null ? LayoutConfig.DEFAULT : layout.sanitized(),
				text == null ? ChatTextConfig.DEFAULT : text.sanitized(),
				background == null ? ChatBackgroundConfig.DEFAULT : background.sanitized(),
				playerColors == null ? PlayerColorConfig.DEFAULT : playerColors.sanitized(),
				mention == null ? MentionConfig.DEFAULT : mention.sanitized(),
				commandClipboard == null
						? CommandClipboardConfig.DEFAULT : commandClipboard.sanitized(),
				recentColors,
				editorUiStyle == null ? EditorUiStyle.CHAT_CANVAS : editorUiStyle
		);
	}
}
