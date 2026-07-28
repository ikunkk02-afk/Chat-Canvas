package io.github.ikunkk02.chatcanvas.ui.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Horizontal tab bar for category navigation.
 */
public class CanvasTabBar extends CanvasWidget {
    public record Tab(String id, Component label, boolean selected) {}

    private final List<Tab> tabs = new ArrayList<>();
    private final Consumer<String> onTabSelected;
    private final int bgColor;
    private final int selectedColor;
    private final int textColor;

    public CanvasTabBar(int x, int y, int width, int height, Consumer<String> onTabSelected,
                        int bgColor, int selectedColor, int textColor) {
        super(x, y, width, height);
        this.onTabSelected = onTabSelected;
        this.bgColor = bgColor;
        this.selectedColor = selectedColor;
        this.textColor = textColor;
    }

    public void setTabs(List<Tab> tabs) { this.tabs.clear(); this.tabs.addAll(tabs); }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!visible || tabs.isEmpty()) return;
        int tabWidth = width / tabs.size();
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            int tx = x + i * tabWidth;
            int color = tab.selected ? selectedColor : bgColor;
            context.fill(tx, y, tx + tabWidth, y + height, color);
            context.drawCenteredString(Minecraft.getInstance().font, tab.label,
                    tx + tabWidth / 2, y + (height - 8) / 2, textColor);
        }
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!isActive() || !visible || button != 0) return false;
        if (isMouseOver(mx, my)) {
            int tabWidth = width / tabs.size();
            int idx = (int)((mx - x) / tabWidth);
            if (idx >= 0 && idx < tabs.size()) {
                onTabSelected.accept(tabs.get(idx).id());
                return true;
            }
        }
        return false;
    }
}
