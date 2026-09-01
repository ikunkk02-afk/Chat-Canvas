package io.github.ikunkk02.chatcanvas.chat.render;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerChatLayoutMode;

public record ChatRenderContext(
		OwoUIDrawContext drawContext,
		Font textRenderer,
		int x,
		int y,
		int width,
		int height,
		float messageOpacity,
		float inputProgress,
		Component inputPlaceholder,
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
