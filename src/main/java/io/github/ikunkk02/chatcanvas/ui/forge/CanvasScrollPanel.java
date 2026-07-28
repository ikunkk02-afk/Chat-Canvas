package io.github.ikunkk02.chatcanvas.ui.forge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Scrollable vertical panel with clip rendering.
 */
public class CanvasScrollPanel extends CanvasContainer {
    private double scrollOffset;
    private double scrollVelocity;
    private final int contentHeight;
    private boolean dragging;

    public CanvasScrollPanel(int x, int y, int width, int height, int contentHeight) {
        super(x, y, width, height);
        this.contentHeight = contentHeight;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        context.enableScissor(x, y, x + width, y + height);
        context.pose().pushPose();
        context.pose().translate(0, -scrollOffset, 0);
        super.render(context, mouseX, (int)(mouseY + scrollOffset), delta);
        // Render scrollbar
        if (contentHeight > height) {
            int barHeight = Math.max(20, height * height / contentHeight);
            int barY = y + (int)(scrollOffset * height / contentHeight);
            context.fill(x + width - 4, barY, x + width, barY + barHeight, 0x80FFFFFF);
        }
        context.pose().popPose();
        context.disableScissor();
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!isActive() || !visible) return false;
        if (mx >= x + width - 6 && contentHeight > height) { dragging = true; return true; }
        return super.mouseClicked(mx, my + scrollOffset, button);
    }

    public boolean mouseReleased(double mx, double my, int button) { dragging = false; return super.mouseReleased(mx, my + scrollOffset, button); }

    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (isMouseOver(mx, my)) {
            scrollOffset = Mth.clamp(scrollOffset - dy * 20, 0, Math.max(0, contentHeight - height));
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging && contentHeight > height) {
            scrollOffset = Mth.clamp(scrollOffset + dy * contentHeight / height, 0, Math.max(0, contentHeight - height));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public void layout() { /* subclasses override */ }
}
