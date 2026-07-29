package io.github.ikunkk02.chatcanvas.chat.render;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerColorConfig;
import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.config.PlayerChatLayoutMode;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public record ChatRenderContext(
		OwoUIGraphics drawContext,
		font font,
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
