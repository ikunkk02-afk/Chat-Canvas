package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.MessageBackgroundMode;
import org.jetbrains.annotations.Nullable;

public final class ChatBackgroundMetrics {
	private ChatBackgroundMetrics() {
	}

	public static int composeBackgroundColor(int rgb, double configuredOpacity,
											 double vanillaOpacity) {
		double configured = clampUnit(configuredOpacity);
		double vanilla = clampUnit(vanillaOpacity);
		int alpha = (int) Math.round(255.0 * configured * vanilla);
		return alpha << 24 | rgb & 0xFFFFFF;
	}

	public static int wrapWidth(int originalInternalWidth, int horizontalPadding,
								double effectiveScale) {
		double scale = safeScale(effectiveScale);
		int padding = Math.max(0, horizontalPadding);
		int reserved = (int) Math.ceil(padding * 2.0 / scale);
		return Math.max(1, originalInternalWidth - reserved);
	}

	public static @Nullable ChatBackgroundBounds messageBounds(
			MessageBackgroundMode mode,
			double messageLeft,
			double messageRight,
			double alignedLineX,
			int renderedLineWidth,
			double glyphTop,
			double glyphBottom,
			double lineAdvance,
			int horizontalPadding,
			int verticalPadding,
			double effectiveScale
	) {
		MessageBackgroundMode safeMode = mode == null
				? MessageBackgroundMode.FOLLOW_TEXT
				: mode;
		if (safeMode == MessageBackgroundMode.HIDDEN) {
			return null;
		}

		double scale = safeScale(effectiveScale);
		double safeMessageLeft = finiteOr(messageLeft, 0.0);
		double safeMessageRight = Math.max(safeMessageLeft, finiteOr(messageRight, safeMessageLeft));
		double internalHorizontalPadding = Math.max(0, horizontalPadding) / scale;
		double desiredLeft;
		double desiredRight;
		if (safeMode == MessageBackgroundMode.FULL_WIDTH) {
			desiredLeft = safeMessageLeft;
			desiredRight = safeMessageRight;
		} else {
			double safeLineX = finiteOr(alignedLineX, safeMessageLeft);
			desiredLeft = safeLineX - internalHorizontalPadding;
			desiredRight = safeLineX + Math.max(0, renderedLineWidth) + internalHorizontalPadding;
		}

		int left = (int) Math.floor(Math.max(safeMessageLeft, desiredLeft));
		int right = (int) Math.ceil(Math.min(safeMessageRight, desiredRight));

		double safeGlyphTop = finiteOr(glyphTop, 0.0);
		double safeGlyphBottom = Math.max(safeGlyphTop + 1.0,
				finiteOr(glyphBottom, safeGlyphTop + 1.0));
		double internalVerticalPadding = Math.max(0, verticalPadding) / scale;
		double desiredHeight = safeGlyphBottom - safeGlyphTop + internalVerticalPadding * 2.0;
		double safeAdvance = Math.max(1.0, finiteOr(lineAdvance, 1.0));
		int requiredGap = Math.max(1, (int) Math.ceil(1.0 / scale));
		int maximumHeight = Math.max(1, (int) Math.floor(safeAdvance) - requiredGap);
		int height = Math.max(1, Math.min(maximumHeight, (int) Math.ceil(desiredHeight)));
		double glyphCenter = (safeGlyphTop + safeGlyphBottom) * 0.5;
		int top = (int) Math.floor(glyphCenter - height * 0.5);
		int bottom = top + height;

		ChatBackgroundBounds bounds = new ChatBackgroundBounds(left, top, right, bottom);
		return bounds.visible() ? bounds : null;
	}

	private static double clampUnit(double value) {
		if (Double.isNaN(value) || value == Double.NEGATIVE_INFINITY) {
			return 0.0;
		}
		if (value == Double.POSITIVE_INFINITY) {
			return 1.0;
		}
		return Math.max(0.0, Math.min(1.0, value));
	}

	private static double safeScale(double value) {
		return Double.isFinite(value) && value > 0.0 ? value : 1.0;
	}

	private static double finiteOr(double value, double fallback) {
		return Double.isFinite(value) ? value : fallback;
	}
}
