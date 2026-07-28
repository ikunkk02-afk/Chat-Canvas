package io.github.ikunkk02.chatcanvas.chat.command.ui;

import io.github.ikunkk02.chatcanvas.ui.forge.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Command tools panel — displays clipboard + favorite commands.
 * Canvas UI native.
 */
public final class CommandToolPanel extends CanvasContainer {

    private final Consumer<String> onSelect;
    private final List<String> favorites;
    private final List<String> clipboard;
    private int scroll;

    public CommandToolPanel(int x, int y, int w, int h,
                             List<String> favorites, List<String> clipboard,
                             Consumer<String> onSelect) {
        super(x, y, w, h);
        this.favorites = favorites;
        this.clipboard = clipboard;
        this.onSelect = onSelect;
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        if (!visible) return;
        var font = Minecraft.getInstance().font;
        int lh = 12, yPos = y - scroll;

        ctx.drawString(font, Component.translatable("chat_canvas.cmd.favorites"), x + 4, yPos, 0xFFFFAA00);
        yPos += lh + 2;
        for (String cmd : favorites) {
            if (yPos + lh > y && yPos < y + height)
                ctx.drawString(font, Component.literal("/" + cmd), x + 8, yPos, 0xFFFFFFFF);
            yPos += lh;
        }
        yPos += 4;
        ctx.drawString(font, Component.translatable("chat_canvas.cmd.clipboard"), x + 4, yPos, 0xFFFFAA00);
        yPos += lh + 2;
        for (String cmd : clipboard) {
            if (yPos + lh > y && yPos < y + height) {
                String d = cmd.length() > 36 ? cmd.substring(0, 34) + ".." : cmd;
                ctx.drawString(font, Component.literal(d), x + 8, yPos, 0xFFAAAAAA);
            }
            yPos += lh;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (!isActive() || !visible || btn != 0 || !isMouseOver(mx, my)) return false;
        int lh = 12, yPos = y - scroll + lh + 2;
        for (String cmd : favorites) {
            if (my >= yPos && my < yPos + lh) { onSelect.accept("/" + cmd); return true; }
            yPos += lh;
        }
        yPos += 4 + lh + 2;
        for (String cmd : clipboard) {
            if (my >= yPos && my < yPos + lh) { onSelect.accept(cmd); return true; }
            yPos += lh;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (isMouseOver(mx, my)) {
            int max = Math.max(0, (favorites.size() + clipboard.size() + 4) * 14 - height);
            scroll = Math.max(0, Math.min(scroll - (int) delta * 20, max));
            return true;
        }
        return false;
    }

    @Override public void layout() {}
}
