package io.github.ikunkk02.chatcanvas.editor;

public final class EditorPointerCapture {
	private EditorPointerTarget target;

	public boolean begin(EditorPointerTarget candidate, double mouseX, double mouseY, int button,
						 boolean shiftDown, boolean controlDown) {
		if (target != null || candidate == null
				|| !candidate.beginPointerInteraction(mouseX, mouseY, button, shiftDown, controlDown)) {
			return false;
		}
		target = candidate;
		return true;
	}

	public boolean drag(double mouseX, double mouseY, int button) {
		return target != null && target.dragPointer(mouseX, mouseY, button);
	}

	public boolean release(double mouseX, double mouseY, int button) {
		if (target == null) return false;
		EditorPointerTarget released = target;
		target = null;
		return released.endPointerInteraction(mouseX, mouseY, button);
	}

	public void cancel() {
		if (target == null) return;
		EditorPointerTarget cancelled = target;
		target = null;
		cancelled.cancelPointerInteraction();
	}

	public boolean active() {
		return target != null;
	}
}
