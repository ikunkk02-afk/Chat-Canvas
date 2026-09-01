package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.ChatTextAlignment;

public final class ChatTextLayout {
	public static final int MIN_INTERNAL_LINE_HEIGHT = 6;
	public static final int VERTICAL_BACKGROUND_PADDING = 2;

	private ChatTextLayout() {
	}

	public static double effectiveScale(double vanillaScale, double configuredScale) {
		double safeVanilla = Double.isFinite(vanillaScale) && vanillaScale > 0.0 ? vanillaScale : 1.0;
		double safeConfigured = Double.isFinite(configuredScale) && configuredScale > 0.0
				? configuredScale : 1.0;
		return safeVanilla * safeConfigured;
	}

	public static int internalLineHeight(int vanillaLineHeight, double configuredLineSpacing) {
		int safeVanilla = Math.max(1, vanillaLineHeight);
		double safeSpacing = Double.isFinite(configuredLineSpacing) && configuredLineSpacing > 0.0
				? configuredLineSpacing : 1.0;
		return Math.max(MIN_INTERNAL_LINE_HEIGHT, (int) Math.round(safeVanilla * safeSpacing));
	}

	public static int internalLineHeight(
			int vanillaLineHeight, double fontScale, double configuredLineSpacing) {
		double safeFontScale = safeScale(fontScale);
		int glyphHeight = (int) Math.ceil(Math.max(1, vanillaLineHeight) * safeFontScale);
		int configured = (int) Math.ceil(internalLineHeight(
				vanillaLineHeight, configuredLineSpacing) * safeFontScale);
		return Math.max(glyphHeight, configured);
	}

	/**
	 * Converts the final chat content width back into unscaled glyph units.
	 * Vanilla chat scale remains the outer HUD scale; Chat Canvas font scale is
	 * applied only around each final line anchor.
	 */
	public static int glyphWrapWidth(
			int configuredScreenWidth,
			int horizontalPadding,
			int glyphSafetyPixels,
			double vanillaChatScale,
			double fontScale) {
		double vanilla = safeScale(vanillaChatScale);
		double font = safeScale(fontScale);
		int reservedScreen = Math.max(0, horizontalPadding) * 2
				+ Math.max(0, glyphSafetyPixels) * 2;
		double availableScreen = Math.max(1.0, configuredScreenWidth - reservedScreen);
		return Math.max(1, (int) Math.floor(availableScreen / vanilla / font));
	}

	public static ChatVerticalMetrics verticalMetrics(int fontHeight, int baseLineHeight,
													  double scale, double configuredLineSpacing) {
		int safeFontHeight = Math.max(1, fontHeight);
		double safeScale = Double.isFinite(scale) && scale > 0.0 ? scale : 1.0;
		double glyphHeight = safeFontHeight * safeScale;
		double backgroundTopOffset = VERTICAL_BACKGROUND_PADDING * 0.5 * safeScale;
		double backgroundHeight = (safeFontHeight + VERTICAL_BACKGROUND_PADDING) * safeScale;
		double lineAdvance = Math.max(glyphHeight,
				internalLineHeight(baseLineHeight, configuredLineSpacing) * safeScale);
		return new ChatVerticalMetrics(
				glyphHeight,
				backgroundTopOffset,
				backgroundHeight,
				lineAdvance
		);
	}

	public static ChatLineMetrics metrics(int lineIndex, int renderedWidth, int availableWidth,
										 int indicatorReservation, ChatTextAlignment alignment,
										 double drawY, double lineAdvance) {
		return metricsWithin(
				lineIndex,
				renderedWidth,
				0.0,
				Math.max(1, availableWidth),
				indicatorReservation,
				alignment,
				drawY,
				lineAdvance
		);
	}

	public static ChatLineMetrics metricsWithin(int lineIndex, int renderedWidth,
												double contentLeft, double contentRight,
												int indicatorReservation,
												ChatTextAlignment alignment,
												double drawY, double lineAdvance) {
		int safeWidth = Math.max(0, renderedWidth);
		double safeLeft = Double.isFinite(contentLeft) ? contentLeft : 0.0;
		double safeRight = Double.isFinite(contentRight)
				? Math.max(safeLeft, contentRight)
				: safeLeft + 1.0;
		double safeAvailable = Math.max(0.0, safeRight - safeLeft);
		int safeIndicator = Math.max(0, indicatorReservation);
		double groupWidth = Math.min(safeAvailable, safeWidth + safeIndicator);
		ChatTextAlignment safeAlignment = alignment == null ? ChatTextAlignment.LEFT : alignment;
		double drawX = switch (safeAlignment) {
			case LEFT -> safeLeft;
			case CENTER -> safeLeft + (safeAvailable - groupWidth) / 2.0;
			case RIGHT -> safeRight - groupWidth;
		};
		double maximumDrawX = Math.max(safeLeft, safeRight - safeWidth - safeIndicator);
		drawX = Math.max(safeLeft, Math.min(maximumDrawX, drawX));
		return new ChatLineMetrics(lineIndex, safeWidth, drawX, drawY,
				Math.max(1.0, lineAdvance), safeIndicator);
	}

	public static int multiplyAlpha(int argb, double opacity) {
		double safeOpacity = Double.isFinite(opacity) ? Math.max(0.0, Math.min(1.0, opacity)) : 1.0;
		int alpha = argb >>> 24;
		int multiplied = (int) Math.round(alpha * safeOpacity);
		return argb & 0x00FFFFFF | multiplied << 24;
	}

	private static double safeScale(double value) {
		return Double.isFinite(value) && value > 0.0 ? value : 1.0;
	}
}
