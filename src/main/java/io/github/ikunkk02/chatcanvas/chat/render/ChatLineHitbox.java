package io.github.ikunkk02.chatcanvas.chat.render;

record ChatLineHitbox(
		double x,
		double y,
		double width,
		double height,
		double textScale
) {
	ChatLineHitbox {
		width = Math.max(0.0, width);
		height = Math.max(0.0, height);
		textScale = Double.isFinite(textScale) && textScale > 0.0 ? textScale : 1.0;
	}

	boolean contains(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + width
				&& mouseY >= y && mouseY <= y + height;
	}

	double textX(double mouseX) {
		return (mouseX - x) / textScale;
	}
}
