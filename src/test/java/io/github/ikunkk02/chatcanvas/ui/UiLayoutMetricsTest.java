package io.github.ikunkk02.chatcanvas.ui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UiLayoutMetricsTest {
	private static final List<int[]> VIEWPORTS = List.of(
			new int[]{960, 540},
			new int[]{640, 360},
			new int[]{480, 270},
			new int[]{427, 240},
			new int[]{320, 180}
	);

	@Test
	void editorSurfacesRemainInsideEveryTargetViewport() {
		for (int[] viewport : VIEWPORTS) {
			int width = viewport[0];
			int height = viewport[1];
			UiLayoutMetrics.EditorPanel panel = UiLayoutMetrics.editorPanel(width, height);
			UiLayoutMetrics.Toolbar toolbar = UiLayoutMetrics.toolbar(width);
			assertTrue(panel.width() <= width - 8);
			assertTrue(panel.top() + panel.height() <= height);
			assertTrue(panel.contentHeight(9, 24) >= 1);
			assertTrue(toolbar.x() >= 0);
			assertTrue(toolbar.x() + toolbar.width() <= width);
			assertTrue(toolbar.y() + toolbar.height() <= panel.top());
		}
	}

	@Test
	void popupsRemainInsideEveryTargetViewport() {
		for (int[] viewport : VIEWPORTS) {
			int width = viewport[0];
			int height = viewport[1];
			UiLayoutMetrics.ColorPicker picker = UiLayoutMetrics.colorPicker(width, height);
			UiLayoutMetrics.CommandPanel command = UiLayoutMetrics.commandPanel(width, height);
			assertTrue(picker.width() <= width - 8);
			assertTrue(picker.height() <= height - 8);
			assertTrue(picker.svWidth() > 0);
			assertTrue(picker.svHeight() > 0);
			assertTrue(picker.svX() + picker.svWidth() <= picker.previewX());
			assertTrue(picker.previewX() + picker.previewWidth()
					<= picker.width() - picker.margin());
			assertTrue(picker.svY() + picker.svHeight() < picker.hueY());
			assertTrue(picker.hueY() + picker.hueHeight() < picker.hexY());
			assertTrue(picker.hexY() + picker.hexHeight() < picker.recentY());
			assertTrue(picker.restoreX() + picker.restoreWidth() < picker.cancelX());
			assertTrue(picker.cancelX() + picker.cancelWidth() < picker.confirmX());
			assertTrue(picker.confirmX() + picker.confirmWidth()
					<= picker.width() - picker.margin());
			assertTrue(picker.buttonY() + picker.buttonHeight() <= picker.height());
			assertTrue(command.width() <= width - 8);
			assertTrue(command.height() <= height - 8);
			assertTrue(command.visibleRows() >= 1);
			assertTrue(88 + command.visibleRows() * 31 <= command.height());
			assertTrue(command.tabX(0) + command.tabWidth() < command.tabX(1));
			assertTrue(command.tabX(1) + command.tabWidth() < command.tabX(2));
			assertTrue(command.tabX(2) + command.tabWidth() <= command.width() - 8);
			assertTrue(command.toolbarPrimaryWidth(true) + 8
					< command.toolbarSecondaryX());
			assertTrue(command.toolbarSecondaryX() + command.toolbarSecondaryWidth()
					<= command.width() - 8);
		}
	}
}
