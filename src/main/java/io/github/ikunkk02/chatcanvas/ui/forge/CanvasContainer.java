package io.github.ikunkk02.chatcanvas.ui.forge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * A widget container that manages a list of children with layout.
 */
public abstract class CanvasContainer extends CanvasWidget {

    protected final List<CanvasWidget> children = new ArrayList<>();

    protected CanvasContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void addChild(CanvasWidget child) { children.add(child); }
    public void removeChild(CanvasWidget child) { children.remove(child); }
    public void clearChildren() { children.clear(); }
    public List<CanvasWidget> getChildren() { return children; }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        for (CanvasWidget child : children) {
            if (child.isVisible()) {
                child.render(context, mouseX, mouseY, delta);
            }
        }
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (!isActive() || !visible) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            CanvasWidget child = children.get(i);
            if (child.isActive() && child.mouseClicked(mx, my, button)) {
                setFocused(true);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    public boolean mouseReleased(double mx, double my, int button) {
        for (int i = children.size() - 1; i >= 0; i--) {
            CanvasWidget child = children.get(i);
            if (child.isActive() && child.mouseReleased(mx, my, button)) return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mx, double my, double delta) {
        for (CanvasWidget child : children) {
            if (child.isActive() && child.mouseScrolled(mx, my, delta)) return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (CanvasWidget child : children) {
            if (child.isActive() && child.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        for (CanvasWidget child : children) {
            if (child.isActive() && child.charTyped(chr, modifiers)) return true;
        }
        return false;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {
        for (CanvasWidget child : children) {
            child.updateNarration(builder);
        }
    }

    @Override
    public void tick() {
        for (CanvasWidget child : children) child.tick();
    }

    public abstract void layout();
}
