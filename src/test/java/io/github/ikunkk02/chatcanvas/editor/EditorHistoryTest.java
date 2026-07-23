package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.LayoutConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EditorHistoryTest {
	@Test
	void undoRedoAndBranchingBehaveAsOneSessionHistory() {
		LayoutConfig initial = LayoutConfig.DEFAULT;
		LayoutConfig second = new LayoutConfig(0.1, 0.2, 0.3, 0.4);
		LayoutConfig third = new LayoutConfig(0.2, 0.2, 0.3, 0.4);
		EditorHistory history = new EditorHistory(initial);
		history.record(second);
		history.record(second);
		history.record(third);
		assertEquals(3, history.size());

		assertEquals(second, history.undo().orElseThrow());
		assertEquals(third, history.redo().orElseThrow());
		assertEquals(second, history.undo().orElseThrow());
		history.record(new LayoutConfig(0.15, 0.2, 0.3, 0.4));
		assertFalse(history.canRedo());
	}

	@Test
	void capacityRemainsBounded() {
		EditorHistory history = new EditorHistory(LayoutConfig.DEFAULT, 4);
		for (int i = 1; i <= 10; i++) {
			history.record(new LayoutConfig(i / 100.0, 0.2, 0.3, 0.3));
		}
		assertEquals(4, history.size());
	}
}
