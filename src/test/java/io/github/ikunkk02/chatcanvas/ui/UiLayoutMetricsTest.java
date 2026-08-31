package io.github.ikunkk02.chatcanvas.ui;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiLayoutMetricsTest {
	private static final List<int[]> VIEWPORTS = List.of(
			new int[]{1280, 800},
			new int[]{960, 540},
			new int[]{854, 534},
			new int[]{640, 360},
			new int[]{534, 300},
			new int[]{480, 270},
			new int[]{427, 240},
			new int[]{342, 256},
			new int[]{320, 180}
	);

	@Test
	void editorSurfacesRemainInsideEveryTargetViewport() {
		for (int[] viewport : VIEWPORTS) {
			int width = viewport[0];
			int height = viewport[1];
			UiLayoutMetrics.EditorPanel panel = UiLayoutMetrics.editorPanel(width, height);
			UiLayoutMetrics.Toolbar toolbar = UiLayoutMetrics.toolbar(width);
			assertTrue(panel.width() <= Math.max(1,
					width - UiLayoutMetrics.EDITOR_SAFE_MARGIN * 2));
			assertTrue(panel.width() <= UiLayoutMetrics.EDITOR_PANEL_MAX_WIDTH);
			assertTrue(panel.height() <= UiLayoutMetrics.EDITOR_PANEL_MAX_HEIGHT);
			assertTrue(panel.top() + panel.height()
					<= height - UiLayoutMetrics.EDITOR_SAFE_MARGIN);
			assertTrue(panel.contentHeight(9, 24) >= 1);
			assertTrue(toolbar.x() >= 0);
			assertTrue(toolbar.x() + toolbar.width() <= width);
			assertTrue(toolbar.y() + toolbar.height() <= panel.top());
		}
	}

	@Test
	void normalPanelKeepsStablePreferredSizeOnLargeScreens() {
		for (int[] viewport : List.of(
				new int[]{960, 540},
				new int[]{1280, 800},
				new int[]{2560, 1600})) {
			UiLayoutMetrics.EditorPanel panel = UiLayoutMetrics.editorPanel(
					viewport[0], viewport[1]);
			assertEquals(UiLayoutMetrics.LayoutMode.NORMAL, panel.mode());
			assertEquals(UiLayoutMetrics.EDITOR_PANEL_PREFERRED_WIDTH, panel.width());
			assertEquals(UiLayoutMetrics.EDITOR_PANEL_PREFERRED_HEIGHT, panel.height());
		}
	}

	@Test
	void compactModeOnlyActivatesWhenLogicalGuiSpaceIsTight() {
		assertEquals(UiLayoutMetrics.LayoutMode.NORMAL,
				UiLayoutMetrics.layoutMode(640, 360));
		assertEquals(UiLayoutMetrics.LayoutMode.COMPACT,
				UiLayoutMetrics.layoutMode(534, 300));
		assertEquals(UiLayoutMetrics.LayoutMode.COMPACT,
				UiLayoutMetrics.layoutMode(480, 270));
		assertEquals(UiLayoutMetrics.LayoutMode.COMPACT,
				UiLayoutMetrics.layoutMode(342, 256));
	}

	@Test
	void physicalResolutionAndGuiScaleMatrixMapsToBoundedLogicalLayouts() {
		List<int[]> physicalCases = List.of(
				new int[]{1920, 1080, 2}, new int[]{1920, 1080, 3},
				new int[]{1920, 1080, 4},
				new int[]{2560, 1600, 2}, new int[]{2560, 1600, 3},
				new int[]{2560, 1600, 4}, new int[]{2560, 1600, 6},
				new int[]{1600, 900, 2}, new int[]{1600, 900, 3},
				new int[]{1600, 900, 4},
				new int[]{1280, 720, 2}, new int[]{1280, 720, 3},
				new int[]{1280, 720, 4},
				new int[]{1024, 768, 2}, new int[]{1024, 768, 3},
				new int[]{1024, 768, 4});
		for (int[] testCase : physicalCases) {
			int logicalWidth = (int) Math.ceil(testCase[0] / (double) testCase[2]);
			int logicalHeight = (int) Math.ceil(testCase[1] / (double) testCase[2]);
			UiLayoutMetrics.EditorPanel panel = UiLayoutMetrics.editorPanel(
					logicalWidth, logicalHeight);
			assertTrue(panel.width() <= UiLayoutMetrics.EDITOR_PANEL_MAX_WIDTH);
			assertTrue(panel.height() <= UiLayoutMetrics.EDITOR_PANEL_MAX_HEIGHT);
			assertTrue(panel.top() + panel.height()
					<= logicalHeight - UiLayoutMetrics.EDITOR_SAFE_MARGIN);
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
