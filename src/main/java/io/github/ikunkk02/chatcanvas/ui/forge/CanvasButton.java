package io.github.ikunkk02.chatcanvas.ui.forge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * A clickable button widget.
 */
public class CanvasButton extends CanvasWidget {
    private Component label;
    private final Consumer<CanvasButton> onClick;
    private final int defaultColor;
    private final int hoverColor;
    private final int textColor;

    public CanvasButton(int x, int y, int width, int height, Component label, Consumer<CanvasButton> onClick) {
        this(x, y, width, height, label, onClick, 0xFF404040, 0xFF606060, 0xFFFFFFFF);
    }

    public CanvasButton(int x, int y, int width, int height, Component label, Consumer<CanvasButton> onClick,
                        int defaultColor, int hoverColor, int textColor) {
        super(x, y, width, height);
        this.label = label;
        this.onClick = onClick;
        this.defaultColor = defaultColor;
        this.hoverColor = hoverColor;
        this.textColor = textColor;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        boolean hovered = isMouseOver(mouseX, mouseY) && active;
        int bg = hovered ? hoverColor : defaultColor;
        context.fill(x, y, x + width, y + height, bg);
        context.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, label,
                x + width / 2, y + (height - 8) / 2, textColor);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!isActive() || !visible || button != 0) return false;
        if (isMouseOver(mx, my)) {
            if (onClick != null) onClick.accept(this);
            return true;
        }
        return false;
    }

    public void setLabel(Component label) { this.label = label; }
}
