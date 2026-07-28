package io.github.ikunkk02.chatcanvas.chat.render;

import io.github.ikunkk02.chatcanvas.config.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Render context — holds all state for the canvas render pass.
 * Forge-native replacement for owo OwoUIDrawContext wrapper.
 */
public record ChatRenderContext(
        GuiGraphics graphics,
        Font font,
        int x, int y, int width, int height,
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
    public int right() { return x + width; }
    public int bottom() { return y + height; }
}
