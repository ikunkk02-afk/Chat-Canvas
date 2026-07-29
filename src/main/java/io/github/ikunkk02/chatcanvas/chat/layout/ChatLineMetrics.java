package io.github.ikunkk02.chatcanvas.chat.layout;

public record ChatLineMetrics(
		int lineIndex,
		int renderedWidth,
		double drawX,
		double drawY,
		double lineAdvance,
		int indicatorReservation
) {
	public double localX(double chatLineX) {
		return chatLineX - drawX;
	}

	public double indicatorX() {
		return drawX + renderedWidth + (indicatorReservation > 0 ? 4.0 : 0.0);
	}
}
