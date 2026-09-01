package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.ui.ResizeHandle;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class LayoutEditorMathTest {
	private static final PixelLayout START = new PixelLayout(200, 150, 300, 180);

	@Test
	void movingSnapsToCenterAndStaysVisible() {
		LayoutEditorMath.SnapResult result = LayoutEditorMath.move(
				START, 149, 110, 1000, 700, 12, 7);
		assertEquals(350, result.layout().x());
		assertEquals(260, result.layout().y());
		assertTrue(result.snappedX());
		assertTrue(result.snappedY());
	}

	@Test
	void allEightResizeHandlesMoveOnlyTheirOwnedEdges() {
		for (ResizeHandle handle : EnumSet.of(
				ResizeHandle.NORTH, ResizeHandle.SOUTH, ResizeHandle.WEST, ResizeHandle.EAST,
				ResizeHandle.NORTH_WEST, ResizeHandle.NORTH_EAST,
				ResizeHandle.SOUTH_WEST, ResizeHandle.SOUTH_EAST)) {
			PixelLayout result = LayoutEditorMath.resize(
					START, 20, 30, handle, 1000, 700, 12, 7).layout();
			if (!handle.west()) assertEquals(START.x(), result.x());
			if (!handle.east()) assertEquals(START.right(), result.right());
			if (!handle.north()) assertEquals(START.y(), result.y());
			if (!handle.south()) assertEquals(START.bottom(), result.bottom());
			if (handle.west()) assertEquals(START.x() + 20, result.x());
			if (handle.east()) assertEquals(START.right() + 20, result.right());
			if (handle.north()) assertEquals(START.y() + 30, result.y());
			if (handle.south()) assertEquals(START.bottom() + 30, result.bottom());
		}
	}

	@Test
	void resizingHonorsMinimumsAndScreenMargins() {
		PixelLayout result = LayoutEditorMath.resize(
				START, 1000, 1000, ResizeHandle.SOUTH_EAST,
				700, 500, 12, 7).layout();
		assertEquals(688, result.right());
		assertEquals(488, result.bottom());

		PixelLayout minimum = LayoutEditorMath.resize(
				START, -1000, -1000, ResizeHandle.SOUTH_EAST,
				1000, 700, 12, 7).layout();
		assertEquals(PixelLayout.DEFAULT_MIN_WIDTH, minimum.width());
		assertEquals(PixelLayout.DEFAULT_MIN_HEIGHT, minimum.height());
	}

	@Test
	void cornersWinHitTestingOverEdges() {
		assertEquals(ResizeHandle.NORTH_WEST, ResizeHandle.hitTest(START, 200, 150, 6));
		assertEquals(ResizeHandle.NORTH_EAST, ResizeHandle.hitTest(START, 500, 150, 6));
		assertEquals(ResizeHandle.SOUTH_WEST, ResizeHandle.hitTest(START, 200, 330, 6));
		assertEquals(ResizeHandle.SOUTH_EAST, ResizeHandle.hitTest(START, 500, 330, 6));
		assertEquals(ResizeHandle.MOVE, ResizeHandle.hitTest(START, 300, 220, 6));
	}

	@Test
	void everyEdgeAndOutsideRegionAreUnambiguous() {
		assertEquals(ResizeHandle.NORTH, ResizeHandle.hitTest(START, 350, 150, 7));
		assertEquals(ResizeHandle.SOUTH, ResizeHandle.hitTest(START, 350, 330, 7));
		assertEquals(ResizeHandle.WEST, ResizeHandle.hitTest(START, 200, 240, 7));
		assertEquals(ResizeHandle.EAST, ResizeHandle.hitTest(START, 500, 240, 7));
		assertEquals(ResizeHandle.NONE, ResizeHandle.hitTest(START, 191, 240, 7));
		assertEquals(ResizeHandle.NONE, ResizeHandle.hitTest(START, 350, 339, 7));
	}
}
