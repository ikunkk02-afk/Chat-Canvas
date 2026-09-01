package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.editor.EditorPointerTarget;

public interface NumericScrubber extends EditorPointerTarget {
	boolean valueRegionContains(double mouseX, double mouseY);

	boolean scroll(double amount);

	boolean restoreDefault();

	void resizeViewport(int width, int height);
}
