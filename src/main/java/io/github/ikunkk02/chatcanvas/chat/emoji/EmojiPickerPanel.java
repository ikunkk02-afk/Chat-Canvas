package io.github.ikunkk02.chatcanvas.chat.emoji;

import io.github.ikunkk02.chatcanvas.ui.forge.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Emoji picker — Canvas UI virtualized grid, category tabs.
 * No owo-lib dependency.
 */
public final class EmojiPickerPanel extends CanvasContainer {

    private static final int COLS = 8;
    private static final int TILE = 22;
    private static final int TAB_H = 16;

    private final Consumer<String> onSelect;
    private EmojiCategory selectedCat = EmojiCategory.RECENT;
    private List<EmojiEntry> entries = List.of();
    private int scroll;

    public EmojiPickerPanel(int x, int y, int w, int h, Consumer<String> onSelect) {
        super(x, y, w, h);
        this.onSelect = onSelect;
        switchCategory(EmojiCategory.SMILEYS);
    }

    private void switchCategory(EmojiCategory cat) {
        this.selectedCat = cat;
        this.entries = EmojiRegistry.instance().category(cat);
        this.scroll = 0;
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        if (!visible) return;
        var font = Minecraft.getInstance().font;
        EmojiCategory[] cats = EmojiCategory.values();
        int tabW = width / cats.length;

        // Tabs
        for (int i = 0; i < cats.length; i++) {
            int tx = x + i * tabW;
            boolean sel = cats[i] == selectedCat;
            ctx.fill(tx, y, tx + tabW, y + TAB_H, sel ? 0xFF4FC3F7 : 0xFF303030);
            var key = Component.translatable(cats[i].translationKey());
            ctx.drawCenteredString(font, key, tx + tabW / 2, y + 3, 0xFFFFFFFF);
        }

        // Grid
        int gy = y + TAB_H + 2, gh = height - TAB_H - 2;
        ctx.enableScissor(x, gy, x + width, gy + gh);
        int rows = (entries.size() + COLS - 1) / COLS;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = row * COLS + col;
                if (idx >= entries.size()) break;
                int tx = x + col * TILE;
                int ty = gy + row * TILE - scroll;
                if (ty + TILE < gy || ty > gy + gh) continue;
                boolean hover = mx >= tx && mx < tx + TILE && my >= ty && my < ty + TILE;
                if (hover) ctx.fill(tx, ty, tx + TILE, ty + TILE, 0xFF505050);
                ctx.drawString(font, entries.get(idx).unicode(), tx + 2, ty + 4, 0xFFFFFFFF);
            }
        }
        ctx.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (!isActive() || !visible || btn != 0) return false;
        // Tab click
        EmojiCategory[] cats = EmojiCategory.values();
        int tabW = width / cats.length;
        if (my >= y && my < y + TAB_H) {
            int idx = (int) ((mx - x) / tabW);
            if (idx >= 0 && idx < cats.length) { switchCategory(cats[idx]); return true; }
        }
        // Emoji click
        int gy = y + TAB_H + 2;
        if (my >= gy && my < y + height) {
            int col = (int) ((mx - x) / TILE);
            int row = (int) ((my - gy + scroll) / TILE);
            int idx = row * COLS + col;
            if (idx >= 0 && idx < entries.size()) {
                onSelect.accept(entries.get(idx).unicode());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (isMouseOver(mx, my)) {
            int max = Math.max(0, (entries.size() / COLS + 1) * TILE - (height - TAB_H - 2));
            scroll = Math.max(0, Math.min(scroll - (int) delta * 20, max));
            return true;
        }
        return false;
    }

    @Override public void layout() {}
}
