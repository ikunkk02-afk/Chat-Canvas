package io.github.ikunkk02.chatcanvas.chat.command.ui;

import io.github.ikunkk02.chatcanvas.chat.command.ClipboardCommandCandidate;
import io.github.ikunkk02.chatcanvas.chat.command.CommandClipboardData;
import io.github.ikunkk02.chatcanvas.chat.command.CommandToolManager;
import io.github.ikunkk02.chatcanvas.chat.command.FavoriteCommandEntry;
import io.github.ikunkk02.chatcanvas.ui.forge.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Command tools panel — clipboard commands + favorites + sensitive filter.
 * Canvas UI native (replaces owo-lib version).
 */
public final class CommandToolPanel extends CanvasContainer {
    private final Consumer<String> onCommandSelected;
    private final List<String> clipboard = new ArrayList<>();
    private final List<FavoriteCommandEntry> favorites = new ArrayList<>();
    private int scrollOffset;

    public CommandToolPanel(int x, int y, int width, int height, Consumer<String> onCommandSelected) {
        super(x, y, width, height);
        this.onCommandSelected = onCommandSelected;
        refresh();
    }

    public void refresh() {
        clipboard.clear();
        favorites.clear();
        var data = CommandToolManager.instance().data();
        for (ClipboardCommandCandidate c : data.clipboard()) clipboard.add(c.command());
        favorites.addAll(data.favorites());
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        if (!visible) return;
        var font = Minecraft.getInstance().font;
        int lineH = 12, yOff = y - scrollOffset;

        // "Favorites" header
        ctx.drawString(font, Component.translatable("chat_canvas.cmd.favorites"), x + 4, yOff + 2, 0xFFFFAA00);
        yOff += lineH + 2;
        for (var fav : favorites) {
            if (yOff + lineH > y && yOff < y + height) {
                ctx.drawString(font, Component.literal("/" + fav.command()), x + 8, yOff + 2, 0xFFFFFFFF);
            }
            yOff += lineH;
        }

        yOff += 4;
        ctx.drawString(font, Component.translatable("chat_canvas.cmd.clipboard"), x + 4, yOff + 2, 0xFFFFAA00);
        yOff += lineH + 2;
        for (String cmd : clipboard) {
            if (yOff + lineH > y && yOff < y + height) {
                String display = cmd.length() > 40 ? cmd.substring(0, 38) + ".." : cmd;
                ctx.drawString(font, Component.literal(display), x + 8, yOff + 2, 0xFFAAAAAA);
            }
            yOff += lineH;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (!isActive() || !visible || btn != 0 || !isMouseOver(mx, my)) return false;
        var font = Minecraft.getInstance().font;
        int lineH = 12, yOff = y - scrollOffset + lineH + 2;
        for (var fav : favorites) {
            if (my >= yOff && my < yOff + lineH) { onCommandSelected.accept("/" + fav.command()); return true; }
            yOff += lineH;
        }
        yOff += 4 + lineH + 2;
        for (int i = 0; i < clipboard.size(); i++) {
            if (my >= yOff && my < yOff + lineH) { onCommandSelected.accept(clipboard.get(i)); return true; }
            yOff += lineH;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (isMouseOver(mx, my)) {
            int maxScroll = (favorites.size() + clipboard.size() + 4) * 14 - height;
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int)delta * 20, Math.max(0, maxScroll)));
            return true;
        }
        return false;
    }
}
