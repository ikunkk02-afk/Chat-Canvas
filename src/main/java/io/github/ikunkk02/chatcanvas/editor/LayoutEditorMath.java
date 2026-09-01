package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.ui.ResizeHandle;

public final class LayoutEditorMath {
	private LayoutEditorMath() {
	}

	public static SnapResult move(PixelLayout start, int deltaX, int deltaY,
								  int screenWidth, int screenHeight, int margin, int snapDistance) {
		int x = start.x() + deltaX;
		int y = start.y() + deltaY;
		int leftTarget = margin;
		int rightTarget = screenWidth - margin - start.width();
		int centerTarget = (screenWidth - start.width()) / 2;
		boolean snappedX = false;
		if (near(x, leftTarget, snapDistance)) {
			x = leftTarget;
			snappedX = true;
		} else if (near(x, rightTarget, snapDistance)) {
			x = rightTarget;
			snappedX = true;
		} else if (near(x, centerTarget, snapDistance)) {
			x = centerTarget;
			snappedX = true;
		}

		int topTarget = margin;
		int bottomTarget = screenHeight - margin - start.height();
		int middleTarget = (screenHeight - start.height()) / 2;
		boolean snappedY = false;
		if (near(y, topTarget, snapDistance)) {
			y = topTarget;
			snappedY = true;
		} else if (near(y, bottomTarget, snapDistance)) {
			y = bottomTarget;
			snappedY = true;
		} else if (near(y, middleTarget, snapDistance)) {
			y = middleTarget;
			snappedY = true;
		}

		return new SnapResult(
				new PixelLayout(x, y, start.width(), start.height())
						.constrained(screenWidth, screenHeight, margin),
				snappedX,
				snappedY
		);
	}

	public static SnapResult resize(PixelLayout start, int deltaX, int deltaY, ResizeHandle handle,
									int screenWidth, int screenHeight, int margin, int snapDistance) {
		if (!handle.resizing()) {
			return new SnapResult(start.constrained(screenWidth, screenHeight, margin), false, false);
		}
		int left = start.x();
		int right = start.right();
		int top = start.y();
		int bottom = start.bottom();
		if (handle.west()) left += deltaX;
		if (handle.east()) right += deltaX;
		if (handle.north()) top += deltaY;
		if (handle.south()) bottom += deltaY;

		boolean snappedX = false;
		boolean snappedY = false;
		if (handle.west()) {
			int snapped = snapEdge(left, margin, screenWidth / 2, screenWidth - margin, snapDistance);
			snappedX = snapped != left;
			left = snapped;
		}
		if (handle.east()) {
			int snapped = snapEdge(right, margin, screenWidth / 2, screenWidth - margin, snapDistance);
			snappedX = snapped != right;
			right = snapped;
		}
		if (handle.north()) {
			int snapped = snapEdge(top, margin, screenHeight / 2, screenHeight - margin, snapDistance);
			snappedY = snapped != top;
			top = snapped;
		}
		if (handle.south()) {
			int snapped = snapEdge(bottom, margin, screenHeight / 2, screenHeight - margin, snapDistance);
			snappedY = snapped != bottom;
			bottom = snapped;
		}

		int minWidth = Math.min(PixelLayout.DEFAULT_MIN_WIDTH, Math.max(1, screenWidth - margin * 2));
		int minHeight = Math.min(PixelLayout.DEFAULT_MIN_HEIGHT, Math.max(1, screenHeight - margin * 2));
		if (handle.west()) left = clamp(left, margin, right - minWidth);
		if (handle.east()) right = clamp(right, left + minWidth, screenWidth - margin);
		if (handle.north()) top = clamp(top, margin, bottom - minHeight);
		if (handle.south()) bottom = clamp(bottom, top + minHeight, screenHeight - margin);

		PixelLayout result = new PixelLayout(left, top, right - left, bottom - top)
				.constrained(screenWidth, screenHeight, margin);
		return new SnapResult(result, snappedX, snappedY);
	}

	private static int snapEdge(int value, int first, int second, int third, int distance) {
		if (near(value, first, distance)) return first;
		if (near(value, second, distance)) return second;
		if (near(value, third, distance)) return third;
		return value;
	}

	private static boolean near(int value, int target, int distance) {
		return Math.abs(value - target) <= distance;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	public record SnapResult(PixelLayout layout, boolean snappedX, boolean snappedY) {
	}
}
