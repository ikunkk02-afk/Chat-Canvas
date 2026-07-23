package io.github.ikunkk02.chatcanvas.config;

public record ChatCanvasSettings(LayoutConfig layout, ChatTextConfig text) {
	public static final ChatCanvasSettings DEFAULT =
			new ChatCanvasSettings(LayoutConfig.DEFAULT, ChatTextConfig.DEFAULT);

	public ChatCanvasSettings sanitized() {
		return new ChatCanvasSettings(
				layout == null ? LayoutConfig.DEFAULT : layout.sanitized(),
				text == null ? ChatTextConfig.DEFAULT : text.sanitized()
		);
	}
}
