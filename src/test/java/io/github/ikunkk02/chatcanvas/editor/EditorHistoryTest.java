package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.ChatTextAlignment;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.LayoutConfig;
import io.github.ikunkk02.chatcanvas.config.MessageBackgroundMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EditorHistoryTest {
	@Test
	void undoRedoAndBranchingBehaveAsOneSessionHistory() {
		LayoutConfig initial = LayoutConfig.DEFAULT;
		LayoutConfig second = new LayoutConfig(0.1, 0.2, 0.3, 0.4);
		LayoutConfig third = new LayoutConfig(0.2, 0.2, 0.3, 0.4);
		EditorHistory history = new EditorHistory(snapshot(initial, ChatTextConfig.DEFAULT));
		history.record(snapshot(second, ChatTextConfig.DEFAULT));
		history.record(snapshot(second, ChatTextConfig.DEFAULT));
		history.record(snapshot(third, ChatTextConfig.DEFAULT));
		assertEquals(3, history.size());

		assertEquals(second, history.undo().orElseThrow().layout());
		assertEquals(third, history.redo().orElseThrow().layout());
		assertEquals(second, history.undo().orElseThrow().layout());
		history.record(snapshot(new LayoutConfig(0.15, 0.2, 0.3, 0.4), ChatTextConfig.DEFAULT));
		assertFalse(history.canRedo());
	}

	@Test
	void capacityRemainsBounded() {
		EditorHistory history = new EditorHistory(snapshot(LayoutConfig.DEFAULT, ChatTextConfig.DEFAULT), 4);
		for (int i = 1; i <= 10; i++) {
			history.record(snapshot(new LayoutConfig(i / 100.0, 0.2, 0.3, 0.3),
					ChatTextConfig.DEFAULT));
		}
		assertEquals(4, history.size());
	}

	@Test
	void textAndLayoutShareOneUndoTimeline() {
		EditorSnapshot initial = snapshot(LayoutConfig.DEFAULT, ChatTextConfig.DEFAULT);
		EditorHistory history = new EditorHistory(initial);
		ChatTextConfig centered = new ChatTextConfig(
				1.25, 0.8, 0.6, ChatTextAlignment.CENTER, false);
		history.record(snapshot(LayoutConfig.DEFAULT, centered));

		assertEquals(ChatTextConfig.DEFAULT, history.undo().orElseThrow().text());
		assertEquals(centered, history.redo().orElseThrow().text());
	}

	@Test
	void backgroundSharesTheSameUndoTimeline() {
		EditorSnapshot initial = new EditorSnapshot(
				LayoutConfig.DEFAULT, ChatTextConfig.DEFAULT, ChatBackgroundConfig.DEFAULT);
		EditorHistory history = new EditorHistory(initial);
		ChatBackgroundConfig hidden = ChatBackgroundConfig.DEFAULT
				.withMessageMode(MessageBackgroundMode.HIDDEN);
		history.record(new EditorSnapshot(LayoutConfig.DEFAULT, ChatTextConfig.DEFAULT, hidden));

		assertEquals(ChatBackgroundConfig.DEFAULT, history.undo().orElseThrow().background());
		assertEquals(hidden, history.redo().orElseThrow().background());
	}

	private static EditorSnapshot snapshot(LayoutConfig layout, ChatTextConfig text) {
		return new EditorSnapshot(layout, text);
	}
}
