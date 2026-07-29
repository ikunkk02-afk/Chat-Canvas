package io.github.ikunkk02.chatcanvas.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EditorPointerCaptureTest {
	@Test
	void capturedTargetKeepsReceivingAbsoluteCoordinatesOutsideItsBounds() {
		EditorPointerCapture capture = new EditorPointerCapture();
		RecordingTarget target = new RecordingTarget();

		assertTrue(capture.begin(target, 120, 80, 0, false, false));
		assertTrue(capture.drag(900, 700, 0));
		assertEquals(900, target.lastX);
		assertEquals(700, target.lastY);
		assertTrue(capture.release(950, 720, 0));
		assertFalse(capture.active());
		assertEquals(1, target.releaseCount);
	}

	@Test
	void cancellingClearsCaptureExactlyOnce() {
		EditorPointerCapture capture = new EditorPointerCapture();
		RecordingTarget target = new RecordingTarget();

		assertTrue(capture.begin(target, 10, 10, 0, true, false));
		capture.cancel();
		capture.cancel();

		assertFalse(capture.active());
		assertEquals(1, target.cancelCount);
		assertFalse(capture.drag(20, 20, 0));
	}

	private static final class RecordingTarget implements EditorPointerTarget {
		double lastX;
		double lastY;
		int releaseCount;
		int cancelCount;

		@Override
		public boolean beginPointerInteraction(double mouseX, double mouseY, int button,
											   boolean shiftDown, boolean controlDown) {
			lastX = mouseX;
			lastY = mouseY;
			return button == 0;
		}

		@Override
		public boolean dragPointer(double mouseX, double mouseY, int button) {
			lastX = mouseX;
			lastY = mouseY;
			return true;
		}

		@Override
		public boolean endPointerInteraction(double mouseX, double mouseY, int button) {
			lastX = mouseX;
			lastY = mouseY;
			releaseCount++;
			return true;
		}

		@Override
		public void cancelPointerInteraction() {
			cancelCount++;
		}
	}
}
