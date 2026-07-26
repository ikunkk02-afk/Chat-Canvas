package io.github.ikunkk02.chatcanvas.chat.render;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerChatLayoutMode;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public record ChatRenderContext(
		OwoUIDrawContext drawContext,
		TextRenderer textRenderer,
		int x,
		int y,
		int width,
		int height,
		float messageOpacity,
		float inputProgress,
		Text inputPlaceholder,
		ChatTextConfig textConfig,
		ChatBackgroundConfig backgroundConfig,
		PlayerColorConfig playerColorConfig,
		MentionConfig mentionConfig,
		String localPlayerName,
		PlayerChatLayoutMode playerChatLayoutMode,
		double splitMessageMaxWidthRatio,
		double vanillaBackgroundOpacity
) {
	public int right() {
		return x + width;
	}

	public int bottom() {
		return y + height;
	}
}
