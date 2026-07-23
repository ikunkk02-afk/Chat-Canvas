package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasSettings;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.LayoutConfig;

public record EditorSnapshot(LayoutConfig layout, ChatTextConfig text) {
	public EditorSnapshot {
		layout = layout == null ? LayoutConfig.DEFAULT : layout.sanitized();
		text = text == null ? ChatTextConfig.DEFAULT : text.sanitized();
	}

	public ChatCanvasSettings settings() {
		return new ChatCanvasSettings(layout, text);
	}
}
