package io.github.ikunkk02.chatcanvas.chat.text;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.util.FormattedCharSequence;

/**
 * Stub for Chat Heads compatibility on Forge 1.20.1.
 * Chat Heads Forge port may not exist; returns plain-text geometry.
 */
public final class ChatHeadsCompat {
    private ChatHeadsCompat() {}

    public static boolean channelAdapterAvailable() { return false; }

    public static int extraWidth(Object line) { return 0; }

    public static double textXAt(Font font, FormattedCharSequence text, double spacing,
                                  Object line, double visualX) {
        return visualX;
    }

    public static boolean renderChannelHead(GuiGraphics context, Minecraft client,
                                             Object message, int x, int y, int size) {
        return false;
    }

    public static int channelHeadWidth(Object message, Minecraft client) { return 0; }
}
