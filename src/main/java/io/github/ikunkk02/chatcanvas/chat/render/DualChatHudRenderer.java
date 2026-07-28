package io.github.ikunkk02.chatcanvas.chat.render;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Stub — returns false so vanilla chat falls through.
 * TODO: port full DualChatHudRenderer from staging.
 */
public final class DualChatHudRenderer {
    private static final DualChatHudRenderer INSTANCE = new DualChatHudRenderer();

    private DualChatHudRenderer() {}

    public static DualChatHudRenderer instance() { return INSTANCE; }

    public boolean render(GuiGraphics ctx, int mouseX, int mouseY, boolean chatFocused) {
        return false; // fallback to vanilla
    }
}
