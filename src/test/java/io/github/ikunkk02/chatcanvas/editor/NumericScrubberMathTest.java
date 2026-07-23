package io.github.ikunkk02.chatcanvas.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumericScrubberMathTest {
	@Test
	void normalFineAndFastSensitivityUseTotalDisplacement() {
		assertEquals(6, NumericScrubberMath.valueDelta(12, NumericScrubberMath.Sensitivity.NORMAL));
		assertEquals(2, NumericScrubberMath.valueDelta(12, NumericScrubberMath.Sensitivity.FINE));
		assertEquals(30, NumericScrubberMath.valueDelta(12, NumericScrubberMath.Sensitivity.FAST));
		assertEquals(-6, NumericScrubberMath.valueDelta(-12, NumericScrubberMath.Sensitivity.NORMAL));
	}

	@Test
	void controlTakesPriorityWhenBothModifiersAreHeld() {
		assertEquals(NumericScrubberMath.Sensitivity.NORMAL,
				NumericScrubberMath.Sensitivity.fromModifiers(false, false));
		assertEquals(NumericScrubberMath.Sensitivity.FINE,
				NumericScrubberMath.Sensitivity.fromModifiers(true, false));
		assertEquals(NumericScrubberMath.Sensitivity.FAST,
				NumericScrubberMath.Sensitivity.fromModifiers(true, true));
	}
}
