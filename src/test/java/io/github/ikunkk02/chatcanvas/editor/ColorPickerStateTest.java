package io.github.ikunkk02.chatcanvas.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorPickerStateTest {
	@Test
	void acceptsSupportedHexFormsAndNormalizesOnRequest() {
		ColorPickerState state = new ColorPickerState(0);
		assertTrue(state.updateHexInput("6EA8FF"));
		assertEquals(0x6EA8FF, state.rgb());
		assertTrue(state.updateHexInput("#123456"));
		assertEquals(0x123456, state.rgb());
		state.normalizeHexInput();
		assertEquals("#123456", state.hexInput());
	}

	@Test
	void invalidHexNeverOverwritesTheLastValidColor() {
		ColorPickerState state = new ColorPickerState(0x6EA8FF);
		assertFalse(state.updateHexInput("#oops"));
		assertFalse(state.hexValid());
		assertEquals(0x6EA8FF, state.rgb());
	}

	@Test
	void hsvEditingUpdatesRgbAndCanRoundTripPrimaryColors() {
		ColorPickerState state = new ColorPickerState(0);
		state.setHsv(0.0f, 1.0f, 1.0f);
		assertEquals(0xFF0000, state.rgb());
		state.setRgb(0x00FF00);
		assertEquals(1.0f / 3.0f, state.hue(), 0.0001f);
		assertEquals(1.0f, state.saturation(), 0.0001f);
		assertEquals(1.0f, state.value(), 0.0001f);
	}
}
