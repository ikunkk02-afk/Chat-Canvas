package io.github.ikunkk02.chatcanvas.editor;

public interface EditorPointerTarget {
	boolean beginPointerInteraction(double mouseX, double mouseY, int button,
									boolean shiftDown, boolean controlDown);

	boolean dragPointer(double mouseX, double mouseY, int button);

	boolean endPointerInteraction(double mouseX, double mouseY, int button);

	void cancelPointerInteraction();
}
