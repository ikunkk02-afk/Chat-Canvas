package io.github.ikunkk02.chatcanvas.voice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VoiceTextSanitizerTest {
	@Test
	void removesControlCharactersAndVoskSpacesBetweenHanCharacters() {
		assertEquals("我们去下界 12 blocks",
				VoiceTextSanitizer.sanitize(
						" \u0000我 们   去 下 界  12   blocks \n", false));
	}

	@Test
	void punctuationIsConservativeAndOptional() {
		assertEquals("大家好。", VoiceTextSanitizer.sanitize("大家好", true));
		assertEquals("大家好！", VoiceTextSanitizer.sanitize("大家好！", true));
		assertEquals("@Steve hello", VoiceTextSanitizer.sanitize("@Steve hello", false));
	}
}
