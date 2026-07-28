package io.github.ikunkk02.chatcanvas.ui.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Simple text label.
 */
public class CanvasLabel extends CanvasWidget {
    private Component text;
    private final int color;
    private final boolean centered;

    public CanvasLabel(int x, int y, int width, int height, Component text, int color, boolean centered) {
        super(x, y, width, height);
        this.text = text;
        this.color = color;
        this.centered = centered;
    }

    public CanvasLabel(int x, int y, Component text, int color) {
        this(x, y, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, text, color, false);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        if (centered) {
            context.drawCenteredString(Minecraft.getInstance().font, text, x + width / 2, y, color);
        } else {
            context.drawString(Minecraft.getInstance().font, text, x, y, color);
        }
    }

    public void setText(Component text) { this.text = text; }
}
