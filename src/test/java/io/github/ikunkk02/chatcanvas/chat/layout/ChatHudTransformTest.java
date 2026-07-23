package io.github.ikunkk02.chatcanvas.chat.layout;

import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatHudTransformTest {
	@Test
	void targetBottomAndVanillaBottomDefineOneSharedOffset() {
		ChatHudTransform transform = new ChatHudTransform(
				new PixelLayout(100, 220, 360, 180), 720, 1.0);

		assertEquals(100.0, transform.offsetX());
		assertEquals(-280.0, transform.offsetY());
		assertEquals(400, transform.layout().bottom());
		assertEquals(680, transform.vanillaBottom());
	}

	@Test
	void screenAndChatCoordinateConversionsRoundTrip() {
		ChatHudTransform transform = new ChatHudTransform(
				new PixelLayout(80, 300, 420, 160), 800, 0.75);

		double chatX = transform.screenToChatX(275.5);
		double chatY = transform.screenToChatY(440.25);
		assertEquals(275.5, transform.chatToScreenX(chatX), 0.00001);
		assertEquals(440.25, transform.chatToScreenY(chatY), 0.00001);
	}

	@Test
	void widthAndHeightRespectVanillaScaleSemantics() {
		ChatHudTransform transform = new ChatHudTransform(
				new PixelLayout(40, 400, 360, 180), 720, 0.75);

		assertEquals(360, transform.configuredWidth());
		assertEquals(480, transform.internalWrapWidth());
		assertEquals(240, transform.configuredInternalHeight());
	}

	@Test
	void configuredFontScaleParticipatesInAllInternalDimensions() {
		ChatHudTransform transform = new ChatHudTransform(
				new PixelLayout(40, 400, 360, 180), 720, 0.8, 1.25);

		assertEquals(1.0, transform.effectiveChatScale(), 0.00001);
		assertEquals(360, transform.internalWrapWidth());
		assertEquals(180, transform.configuredInternalHeight());
	}

	@Test
	void openChatUsesMessageBottomAndMessageHeightForRenderAndHitTesting() {
		PixelLayout layout = new PixelLayout(40, 400, 360, 180);
		RuntimeChatBounds bounds = RuntimeChatBounds.calculate(layout, true, 12, 3, 9);
		ChatHudTransform transform = new ChatHudTransform(layout, 720, 0.75, bounds);

		assertEquals(565, bounds.messageBottom());
		assertEquals(165, bounds.messageHeight());
		assertEquals(-115.0, transform.offsetY());
		assertEquals(220, transform.configuredInternalHeight());
		assertEquals(565.0, transform.chatToScreenY(transform.vanillaBottom()));
	}
}
