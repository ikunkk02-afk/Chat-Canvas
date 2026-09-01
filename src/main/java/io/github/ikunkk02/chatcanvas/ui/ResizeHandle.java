package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.PixelLayout;

public enum ResizeHandle {
	NONE(false, false, false, false),
	MOVE(false, false, false, false),
	NORTH(true, false, false, false),
	SOUTH(false, true, false, false),
	WEST(false, false, true, false),
	EAST(false, false, false, true),
	NORTH_WEST(true, false, true, false),
	NORTH_EAST(true, false, false, true),
	SOUTH_WEST(false, true, true, false),
	SOUTH_EAST(false, true, false, true);

	private final boolean north;
	private final boolean south;
	private final boolean west;
	private final boolean east;

	ResizeHandle(boolean north, boolean south, boolean west, boolean east) {
		this.north = north;
		this.south = south;
		this.west = west;
		this.east = east;
	}

	public static ResizeHandle hitTest(PixelLayout layout, double mouseX, double mouseY, int thickness) {
		boolean insideExpanded = mouseX >= layout.x() - thickness && mouseX <= layout.right() + thickness
				&& mouseY >= layout.y() - thickness && mouseY <= layout.bottom() + thickness;
		if (!insideExpanded) {
			return NONE;
		}
		boolean left = Math.abs(mouseX - layout.x()) <= thickness;
		boolean right = Math.abs(mouseX - layout.right()) <= thickness;
		boolean top = Math.abs(mouseY - layout.y()) <= thickness;
		boolean bottom = Math.abs(mouseY - layout.bottom()) <= thickness;
		if (top && left) return NORTH_WEST;
		if (top && right) return NORTH_EAST;
		if (bottom && left) return SOUTH_WEST;
		if (bottom && right) return SOUTH_EAST;
		if (top) return NORTH;
		if (bottom) return SOUTH;
		if (left) return WEST;
		if (right) return EAST;
		return mouseX >= layout.x() && mouseX <= layout.right()
				&& mouseY >= layout.y() && mouseY <= layout.bottom() ? MOVE : NONE;
	}

	public boolean north() {
		return north;
	}

	public boolean south() {
		return south;
	}

	public boolean west() {
		return west;
	}

	public boolean east() {
		return east;
	}

	public boolean resizing() {
		return this != NONE && this != MOVE;
	}
}
