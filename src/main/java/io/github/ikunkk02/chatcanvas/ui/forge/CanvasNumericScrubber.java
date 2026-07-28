package io.github.ikunkk02.chatcanvas.ui.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Drag-to-adjust numeric scrubber with keyboard modifiers.
 * Shift = fine (×0.1), Ctrl = fast (×5), Right-click = reset to default.
 */
public class CanvasNumericScrubber extends CanvasWidget {
    private final String label;
    private final Supplier<Double> getter;
    private final Consumer<Double> setter;
    private final double min, max, step;
    private final double defaultValue;
    private final String format;
    private boolean dragging;
    private double dragStartValue;

    public CanvasNumericScrubber(int x, int y, int width, int height, String label,
                                  Supplier<Double> getter, Consumer<Double> setter,
                                  double min, double max, double step, double defaultValue, String format) {
        super(x, y, width, height);
        this.label = label;
        this.getter = getter;
        this.setter = setter;
        this.min = min;
        this.max = max;
        this.step = step;
        this.defaultValue = defaultValue;
        this.format = format;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        double value = getter.get();
        boolean hovered = isMouseOver(mouseX, mouseY) && active;

        context.fill(x, y, x + width, y + height, hovered ? 0xFF404040 : 0xFF303030);
        String text = label + ": " + String.format(format, value);
        context.drawString(Minecraft.getInstance().font, Component.literal(text), x + 4, y + (height - 8) / 2, 0xFFFFFFFF);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!isActive() || !visible) return false;
        if (isMouseOver(mx, my)) {
            if (button == 1) { setter.accept(defaultValue); return true; }
            if (button == 0) { dragging = true; dragStartValue = getter.get(); return true; }
        }
        return false;
    }

    public boolean mouseReleased(double mx, double my, int button) { dragging = false; return false; }

    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging) {
            double modifier = step;
            if (GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS)
                modifier = step * 0.1;
            else if (GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS)
                modifier = step * 5;
            setter.accept(Mth.clamp(dragStartValue - dy * modifier, min, max));
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        if (isMouseOver(mx, my) && active) {
            double modifier = step;
            if (GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS)
                modifier = step * 0.1;
            else if (GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS)
                modifier = step * 5;
            setter.accept(Mth.clamp(getter.get() - dy * modifier, min, max));
            return true;
        }
        return false;
    }

    public double getValue() { return getter.get(); }
}
