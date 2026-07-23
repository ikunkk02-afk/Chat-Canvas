package io.github.ikunkk02.chatcanvas.ui;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;

import java.util.ArrayList;
import java.util.List;

/**
 * A page viewport that clips its children to its bounds using GL scissor.
 * <p>
 * Only the active page (and optionally one transition target page) are rendered.
 * All other pages are completely skipped — they are invisible and do not
 * participate in hit-testing, scrolling, or keyboard focus.
 * <p>
 * This provides a "double protection" against page leakage:
 * <ol>
 *   <li>Hardware scissor clip at the viewport boundary</li>
 *   <li>Render culling — only 1-2 pages draw per frame</li>
 * </ol>
 */
public final class ClippedPageViewport extends FlowLayout {

    /**
     * Set to {@code true} during development to render coloured boundary
     * rectangles. Always {@code false} in production builds.
     */
    public static final boolean DEBUG_BOUNDARIES = false;

    private final List<Component> pages = new ArrayList<>();
    private int activePage = 0;
    private int transitionPage = -1;

    public ClippedPageViewport(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL);
        this.allowOverflow(false);
    }

    // ── page management ──────────────────────────────────────────

    /**
     * Add a page component. Pages are drawn in insertion order (index 0 first).
     * Only the page at {@link #activePage} (and optionally the page at
     * {@link #transitionPage}) is rendered each frame.
     */
    public void addPage(Component page) {
        pages.add(page);
        super.child(page);
    }

    /** Replace all pages. */
    public void setPages(List<Component> newPages) {
        pages.clear();
        this.clearChildren();
        for (Component page : newPages) {
            addPage(page);
        }
    }

    /** 0-based index of the single page that should render when idle. */
    public void setActivePage(int index) {
        this.activePage = clamp(index, 0, pages.size() - 1);
    }

    /**
     * 0-based index of a second page to render during a category transition.
     * Set to -1 when no transition is in progress.
     */
    public void setTransitionPage(int index) {
        if (index < 0 || index >= pages.size()) {
            this.transitionPage = -1;
        } else {
            this.transitionPage = index;
        }
    }

    public int activePage() {
        return activePage;
    }

    public int pageCount() {
        return pages.size();
    }

    // ── draw ─────────────────────────────────────────────────────

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY,
                     float partialTicks, float delta) {
        if (this.width <= 0 || this.height <= 0) return;

        int left   = this.x;
        int top    = this.y;
        int right  = left + this.width;
        int bottom = top  + this.height;

        if (right <= left || bottom <= top) return;

        // ── debug: viewport boundary ──
        if (DEBUG_BOUNDARIES) {
            drawDebugBoundary(context, left, top, right, bottom, 0xFF0000FF); // blue
        }

        context.enableScissor(left, top, right, bottom);
        try {
            for (int i = 0; i < pages.size(); i++) {
                if (i != activePage && i != transitionPage) continue;
                Component page = pages.get(i);
                if (page == null) continue;

                // ── debug: active / transition page boundaries ──
                if (DEBUG_BOUNDARIES) {
                    int px = page.x();
                    int py = page.y();
                    int color = (i == activePage) ? 0xFF00FF00  // green
                                                  : 0xFFFFFF00; // yellow
                    drawDebugBoundary(context, px, py, px + page.width(), py + page.height(), color);
                }

                page.draw(context, mouseX, mouseY, partialTicks, delta);
            }
        } finally {
            context.disableScissor();
        }
    }

    // ── input filtering — only active page receives events ───────

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (!containsScreenPoint(mouseX, mouseY)) return false;
        Component page = pages.get(activePage);
        return page != null && page.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseUp(double mouseX, double mouseY, int button) {
        if (!containsScreenPoint(mouseX, mouseY)) return false;
        Component page = pages.get(activePage);
        return page != null && page.onMouseUp(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        if (!containsScreenPoint(mouseX, mouseY)) return false;
        Component page = pages.get(activePage);
        return page != null && page.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (!containsScreenPoint(mouseX, mouseY)) return false;
        Component page = pages.get(activePage);
        return page != null && page.onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        Component page = pages.get(activePage);
        return page != null && page.onKeyPress(keyCode, scanCode, modifiers);
    }

    // ── hit-testing (childAt) — restrict to active page ─────────

    @Override
    public Component childAt(int x, int y) {
        if (!isInBoundingBox(x, y)) return null;
        Component page = pages.get(activePage);
        if (page != null && page.isInBoundingBox(x, y)) {
            // Delegate to page so its own childAt() resolves correctly
            if (page instanceof FlowLayout flow) {
                Component hit = flow.childAt(x, y);
                if (hit != null) return hit;
            }
            return page;
        }
        return null;
    }

    // ── layout ───────────────────────────────────────────────────

    // ── helpers ──────────────────────────────────────────────────

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void drawDebugBoundary(
            OwoUIDrawContext context, int x1, int y1, int x2, int y2, int color) {
        // top edge
        context.fill(x1, y1, x2, y1 + 1, color);
        // bottom edge
        context.fill(x1, y2 - 1, x2, y2, color);
        // left edge
        context.fill(x1, y1, x1 + 1, y2, color);
        // right edge
        context.fill(x2 - 1, y1, x2, y2, color);
    }

    // ── internal: test whether screen coordinate falls in viewport ─

    private boolean containsScreenPoint(double screenX, double screenY) {
        return screenX >= this.x
            && screenX <  this.x + this.width
            && screenY >= this.y
            && screenY <  this.y + this.height;
    }
}
