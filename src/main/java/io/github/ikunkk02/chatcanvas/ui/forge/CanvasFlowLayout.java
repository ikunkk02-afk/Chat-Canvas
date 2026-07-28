package io.github.ikunkk02.chatcanvas.ui.forge;

/**
 * Vertical flow layout container — stacks children vertically with spacing.
 */
public class CanvasFlowLayout extends CanvasContainer {
    private final int spacing;
    private int currentY;

    public CanvasFlowLayout(int x, int y, int width, int spacing) {
        super(x, y, width, 1);
        this.spacing = spacing;
        this.currentY = y;
    }

    @Override
    public void addChild(CanvasWidget child) {
        child.setPosition(x, currentY);
        currentY += child.height + spacing;
        height = currentY - y;
        super.addChild(child);
    }

    @Override
    public void clearChildren() {
        super.clearChildren();
        currentY = y;
        height = 1;
    }

    @Override
    public void layout() {
        currentY = y;
        for (CanvasWidget child : children) {
            child.setPosition(x, currentY);
            currentY += child.height + spacing;
        }
        height = Math.max(1, currentY - y);
    }
}
