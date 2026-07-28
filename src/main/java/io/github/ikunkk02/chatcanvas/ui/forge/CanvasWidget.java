package io.github.ikunkk02.chatcanvas.ui.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Base widget for the Canvas UI framework.
 * Wraps CreativeCore's GuiControl-like behavior with a simpler API.
 */
public abstract class CanvasWidget implements Renderable, GuiEventListener {

    protected int x, y, width, height;
    protected boolean visible = true;
    protected boolean active = true;
    protected boolean focused;
    @Nullable protected Component tooltip;
    @Nullable protected Supplier<Component> tooltipSupplier;

    protected CanvasWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setSize(int width, int height) { this.width = width; this.height = height; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isVisible() { return visible; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active && visible; }

    @Override public void setFocused(boolean focused) { this.focused = focused; }
    @Override public boolean isFocused() { return focused; }

    public void setTooltip(@Nullable Component tooltip) { this.tooltip = tooltip; }
    public void setTooltipSupplier(@Nullable Supplier<Component> supplier) { this.tooltipSupplier = supplier; }

    @Nullable public Component getTooltip() {
        if (tooltipSupplier != null) return tooltipSupplier.get();
        return tooltip;
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!isActive() || !visible) return false;
        return isMouseOver(mx, my);
    }

    @Override
    public boolean isMouseOver(double mx, double my) {
        return mx >= x && mx < x + width && my >= y && my < y + height;
    }

    public void updateNarration(NarrationElementOutput builder) {}

    public void tick() {}
}
