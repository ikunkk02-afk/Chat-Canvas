package io.github.ikunkk02.chatcanvas.ui;

/**
 * Pure layout calculations shared by the editor and fixed chat overlays.
 * Keeping these calculations free of Minecraft classes makes the high GUI
 * scale bounds directly testable.
 */
public final class UiLayoutMetrics {
	private UiLayoutMetrics() {
	}

	public static EditorPanel editorPanel(int screenWidth, int screenHeight) {
		int safeWidth = Math.max(1, screenWidth);
		int safeHeight = Math.max(1, screenHeight);
		int availableWidth = Math.max(1, safeWidth - 8);
		int minimumWidth = Math.min(220, availableWidth);
		int maximumWidth = Math.min(300, availableWidth);
		int width = clamp((int) Math.round(safeWidth * 0.42), minimumWidth, maximumWidth);
		boolean compact = safeHeight < 240;
		int top = compact ? 42 : 48;
		int height = Math.max(1, safeHeight - top - 8);
		return new EditorPanel(width, height, top,
				compact ? 6 : 12,
				compact ? 4 : 8,
				compact ? 26 : 30,
				!compact);
	}

	public static Toolbar toolbar(int screenWidth) {
		int safeWidth = Math.max(1, screenWidth);
		int width = Math.min(620, Math.max(1, safeWidth - 16));
		return new Toolbar(Math.max(0, (safeWidth - width) / 2), 8, width, 32);
	}

	public static ColorPicker colorPicker(int screenWidth, int screenHeight) {
		int width = Math.min(250, Math.max(1, screenWidth - 8));
		int height = Math.min(266, Math.max(1, screenHeight - 8));
		boolean compact = width < 224 || height < 240;
		int margin = compact ? 6 : 10;
		int buttonHeight = compact ? 18 : 22;
		int buttonY = Math.max(margin, height - margin - buttonHeight);
		int previewWidth = compact ? 20 : 30;
		int previewGap = compact ? 6 : 10;
		int svX = margin;
		int svY = compact ? 18 : 24;
		int svWidth = Math.max(32, width - margin * 2 - previewGap - previewWidth);
		int recentGap = compact ? 2 : 4;
		int recentSize = compact
				? Math.max(8, Math.min(16,
						(width - margin * 2 - recentGap * 7) / 8))
				: 20;
		int recentY = compact
				? Math.max(svY + 44, buttonY - 4 - recentSize)
				: 202;
		int recentLabelY = recentY - 10;
		int hexHeight = compact ? 18 : 22;
		int hexY = compact ? recentLabelY - 3 - hexHeight : 154;
		int hexLabelY = hexY - 11;
		int hueHeight = compact ? 8 : 12;
		int hueY = compact ? hexLabelY - 3 - hueHeight : 122;
		int svHeight = compact ? Math.max(20, hueY - svY - 4) : 90;
		return new ColorPicker(width, height, compact, margin,
				svX, svY, svWidth, svHeight,
				hueY, hueHeight, hexY, hexHeight,
				recentY, recentSize, recentGap,
				buttonY, buttonHeight, previewWidth, previewGap);
	}

	public static CommandPanel commandPanel(int screenWidth, int screenHeight) {
		int width = Math.min(292, Math.max(1, screenWidth - 8));
		int availableHeight = Math.max(1, screenHeight - 8);
		int rows = clamp((availableHeight - 105) / 31, 1, 5);
		int height = Math.min(availableHeight, 105 + rows * 31);
		return new CommandPanel(width, height, rows);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	public record EditorPanel(
			int width, int height, int top, int padding, int gap,
			int footerHeight, boolean showSubtitle
	) {
		public int contentHeight(int labelHeight, int categoryHeight) {
			int labels = labelHeight * (showSubtitle ? 2 : 1);
			int gaps = gap * (showSubtitle ? 4 : 3);
			return Math.max(1, height - labels - categoryHeight
					- footerHeight - gaps - padding * 2);
		}
	}

	public record Toolbar(int x, int y, int width, int height) {
	}

	public record ColorPicker(
			int width, int height, boolean compact, int margin,
			int svX, int svY, int svWidth, int svHeight,
			int hueY, int hueHeight, int hexY, int hexHeight,
			int recentY, int recentSize, int recentGap,
			int buttonY, int buttonHeight, int previewWidth, int previewGap
	) {
		public int previewX() {
			return svX + svWidth + previewGap;
		}

		public int previewHeight() {
			return svHeight + 4 + hueHeight;
		}

		public int actionGap() {
			return 4;
		}

		public int actionAvailableWidth() {
			return Math.max(3, width - margin * 2 - actionGap() * 2);
		}

		public int restoreX() {
			return margin;
		}

		public int restoreWidth() {
			return Math.max(1, (int) Math.round(actionAvailableWidth() * 0.42));
		}

		public int cancelX() {
			return restoreX() + restoreWidth() + actionGap();
		}

		public int cancelWidth() {
			return Math.max(1, (int) Math.round(actionAvailableWidth() * 0.26));
		}

		public int confirmX() {
			return cancelX() + cancelWidth() + actionGap();
		}

		public int confirmWidth() {
			return Math.max(1, actionAvailableWidth() - restoreWidth() - cancelWidth());
		}
	}

	public record CommandPanel(int width, int height, int visibleRows) {
		public int tabGap() {
			return 4;
		}

		public int tabWidth() {
			return Math.max(1, (width - 24) / 3);
		}

		public int tabX(int index) {
			return 8 + index * (tabWidth() + tabGap());
		}

		public int toolbarPrimaryWidth(boolean split) {
			return split ? Math.max(1, (width - 20) / 2) : Math.max(1, width - 16);
		}

		public int toolbarSecondaryX() {
			return 8 + toolbarPrimaryWidth(true) + 4;
		}

		public int toolbarSecondaryWidth() {
			return Math.max(1, width - 8 - toolbarSecondaryX());
		}
	}
}
