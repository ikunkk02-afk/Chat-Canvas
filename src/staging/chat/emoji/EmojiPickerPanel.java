package io.github.ikunkk02.chatcanvas.chat.emoji;

import io.github.ikunkk02.chatcanvas.ui.forge.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Emoji picker panel — renders a grid of emoji tiles with category tabs.
 * Uses Canvas UI (no owo-lib). Virtualized: only renders visible rows.
 */
public final class EmojiPickerPanel extends CanvasContainer {
    private static final int COLS = 8;
    private static final int TILE_SIZE = 22;
    private static final int TAB_HEIGHT = 16;

    private final Consumer<String> onEmojiSelected;
    private EmojiCategory selectedCategory;
    private List<EmojiEntry> visibleEntries = List.of();
    private int scrollOffset;
    private boolean dragging;

    public EmojiPickerPanel(int x, int y, int width, int height, Consumer<String> onEmojiSelected) {
        super(x, y, width, height);
        this.onEmojiSelected = onEmojiSelected;
        this.selectedCategory = EmojiCategory.RECENT;
    }

    public void setCategory(EmojiCategory category) {
        this.selectedCategory = category;
        this.visibleEntries = EmojiRegistry.entriesFor(category);
        this.scrollOffset = 0;
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        if (!visible) return;
        var font = Minecraft.getInstance().font;

        // Category tabs
        EmojiCategory[] cats = EmojiCategory.values();
        int tabW = width / cats.length;
        for (int i = 0; i < cats.length; i++) {
            int tx = x + i * tabW;
            boolean sel = cats[i] == selectedCategory;
            ctx.fill(tx, y, tx + tabW, y + TAB_HEIGHT, sel ? 0xFF4FC3F7 : 0xFF303030);
            ctx.drawCenteredString(font, cats[i].icon(), tx + tabW / 2, y + 3, 0xFFFFFFFF);
        }

        // Emoji grid (clipped)
        int gridY = y + TAB_HEIGHT + 2;
        CanvasClipStack.push(ctx, x, gridY, width, height - TAB_HEIGHT - 2);
        drawGrid(ctx, mx, my, gridY);
        CanvasClipStack.pop(ctx);
    }

    private void drawGrid(GuiGraphics ctx, int mx, int my, int gridY) {
        var font = Minecraft.getInstance().font;
        int firstRow = scrollOffset / TILE_SIZE;
        int visibleRows = (height - TAB_HEIGHT) / TILE_SIZE + 1;
        int tileGap = 2;

        for (int row = firstRow; row < firstRow + visibleRows && row * COLS < visibleEntries.size(); row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = row * COLS + col;
                if (idx >= visibleEntries.size()) break;
                EmojiEntry entry = visibleEntries.get(idx);
                int tx = x + col * TILE_SIZE;
                int ty = gridY + row * TILE_SIZE - scrollOffset;
                boolean hovered = mx >= tx && mx < tx + TILE_SIZE && my >= ty && my < ty + TILE_SIZE;
                if (hovered) ctx.fill(tx, ty, tx + TILE_SIZE, ty + TILE_SIZE, 0xFF505050);
                ctx.drawString(font, entry.glyph(), tx + 2, ty + 4, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (!isActive() || !visible || btn != 0) return false;

        // Tab click
        EmojiCategory[] cats = EmojiCategory.values();
        int tabW = width / cats.length;
        if (my >= y && my < y + TAB_HEIGHT) {
            int idx = (int)((mx - x) / tabW);
            if (idx >= 0 && idx < cats.length) { setCategory(cats[idx]); return true; }
        }

        // Emoji click
        int gridY = y + TAB_HEIGHT + 2;
        if (my >= gridY) {
            dragging = true;
            int row = (int)((my - gridY + scrollOffset) / TILE_SIZE);
            int col = (int)((mx - x) / TILE_SIZE);
            int idx = row * COLS + col;
            if (idx >= 0 && idx < visibleEntries.size()) {
                EmojiEntry entry = visibleEntries.get(idx);
                EmojiRecentManager.instance().markUsed(entry.id());
                onEmojiSelected.accept(entry.glyph());
                return true;
            }
        }
        return false;
    }

    @Override public boolean mouseReleased(double mx, double my, int btn) { dragging = false; return false; }

    @Override public boolean mouseScrolled(double mx, double my, double delta) {
        if (isMouseOver(mx, my)) {
            int maxScroll = Math.max(0, (visibleEntries.size() / COLS + 1) * TILE_SIZE - (height - TAB_HEIGHT));
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int)delta * 20, maxScroll));
            return true;
        }
        return false;
    }
}
