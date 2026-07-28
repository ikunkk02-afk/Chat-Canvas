package io.github.ikunkk02.chatcanvas.ui.forge;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages nested scissor clipping via a stack.
 */
public final class CanvasClipStack {
    private CanvasClipStack() {}

    private static final Deque<int[]> CLIP_STACK = new ArrayDeque<>();

    public static void push(GuiGraphics context, int x, int y, int width, int height) {
        if (!CLIP_STACK.isEmpty()) {
            int[] prev = CLIP_STACK.peek();
            x = Math.max(x, prev[0]);
            y = Math.max(y, prev[1]);
            width = Math.min(x + width, prev[0] + prev[2]) - x;
            height = Math.min(y + height, prev[1] + prev[3]) - y;
        }
        CLIP_STACK.push(new int[]{x, y, width, height});
        context.enableScissor(x, y, x + width, y + height);
    }

    public static void pop(GuiGraphics context) {
        CLIP_STACK.pop();
        if (CLIP_STACK.isEmpty()) {
            context.disableScissor();
        } else {
            int[] prev = CLIP_STACK.peek();
            context.enableScissor(prev[0], prev[1], prev[0] + prev[2], prev[1] + prev[3]);
        }
    }
}
