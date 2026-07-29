package io.github.ikunkk02.chatcanvas.ui;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;

import java.util.ArrayList;
import java.util.List;

/**
 * A page viewport that clips its children to its bounds using GL scissor.
 * <p>
 * Only the active page (and optionally one transition target page) are
 * rendered. All other pages are skipped — they are invisible.
 * <p>
 * <b>Event dispatch:</b> this component does <em>not</em> override any
 * mouse or keyboard handler.  Events flow through owo-ui's native
 * {@code BaseParentComponent} tree dispatch.  The only override is
 * {@link #childAt(int, int)} which restricts hit‑testing to the active
 * page so that off‑screen (inactive) pages never receive input.
 * <p>
 * This provides a "double protection" against page leakage:
 * <ol>
 *   <li>Hardware scissor clip at the viewport boundary</li>
 *   <li>Render culling — only 1‑2 pages draw per frame</li>
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
     * Add a page component. Pages are drawn in insertion order (index 0
     * first).  Only the page at {@link #activePage} (and optionally the
     * page at {@link #transitionPage}) is rendered each frame.
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
     * 0-based index of a second page to render during a category
     * transition.  Set to -1 when no transition is in progress.
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
    public void draw(OwoUIGraphics context, int mouseX, int mouseY,
                     float partialTicks, float delta) {
        if (this.width <= 0 || this.height <= 0) return;

        int left   = this.x;
        int top    = this.y;
        int right  = left + this.width;
        int bottom = top  + this.height;

        if (right <= left || bottom <= top) return;

        // ── debug: viewport boundary (blue) ──
        if (DEBUG_BOUNDARIES) {
            drawDebugBoundary(context, left, top, right, bottom, 0xFF0000FF);
        }

        context.enableScissor(left, top, right, bottom);
        try {
            for (int i = 0; i < pages.size(); i++) {
                if (i != activePage && i != transitionPage) continue;
                Component page = pages.get(i);
                if (page == null) continue;

                // ── debug: active (green) / transition (yellow) ──
                if (DEBUG_BOUNDARIES) {
                    int px = page.x();
                    int py = page.y();
                    int color = (i == activePage) ? 0xFF00FF00 : 0xFFFFFF00;
                    drawDebugBoundary(context, px, py,
                            px + page.width(), py + page.height(), color);
                }

                page.draw(context, mouseX, mouseY, partialTicks, delta);
            }
        } finally {
            context.disableScissor();
        }
    }

    // ── hit-testing — restrict to active page ────────────────────

    /**
     * Restricts owo-ui's hit-test tree to the active page only.
     * <p>
     * owo-ui's {@code BaseParentComponent.onMouseDown()} iterates
     * <em>all</em> children and calls {@code child.onMouseDown()},
     * which naturally recurses into the deepest interactable control
     * (buttons, sliders, text boxes, scroll containers, etc.).
     * <p>
     * The only guard needed here is {@code childAt}: by limiting the
     * search to the active page we ensure that off‑screen (inactive)
     * pages never become the target of a mouse event.
     */
    @Override
    public Component childAt(int x, int y) {
        // Outside the viewport — not our concern.
        if (!this.isInBoundingBox(x, y)) return null;

        // During a page transition, block all interaction.
        if (transitionPage >= 0) return null;

        Component active = pages.get(activePage);
        if (active == null) return null;

        // Let the active page's own component tree resolve the
        // deepest interactable child.  Works for FlowLayout,
        // StackLayout, ScrollContainer, and any ParentComponent.
        if (active instanceof ParentComponent parent) {
            Component deepest = parent.childAt(x, y);
            if (deepest != null) return deepest;
        }

        // Fallback: the point is on the active page itself (e.g. an
        // empty area at the bottom of the scroll content).
        if (active.isInBoundingBox(x, y)) return active;

        return null;
    }

    // ── helpers ──────────────────────────────────────────────────

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void drawDebugBoundary(
            OwoUIGraphics context, int x1, int y1, int x2, int y2,
            int color) {
        // top edge
        context.fill(x1, y1, x2, y1 + 1, color);
        // bottom edge
        context.fill(x1, y2 - 1, x2, y2, color);
        // left edge
        context.fill(x1, y1, x1 + 1, y2, color);
        // right edge
        context.fill(x2 - 1, y1, x2, y2, color);
    }
}
