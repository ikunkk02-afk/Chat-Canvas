package io.github.ikunkk02.chatcanvas.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecentColorStoreTest {
	@Test
	void keepsUniqueMostRecentColorsWithinCapacity() {
		RecentColorStore store = new RecentColorStore(List.of(0x111111, 0x222222));
		store.add(0x111111);
		for (int index = 0; index < 10; index++) {
			store.add(index);
		}

		assertEquals(RecentColorStore.MAX_COLORS, store.colors().size());
		assertEquals(9, store.colors().getFirst());
		assertEquals(2, store.colors().getLast());
	}

	@Test
	void loadingSkipsInvalidAndDuplicateValuesWithoutReordering() {
		assertEquals(
				List.of(0x123456, 0xABCDEF),
				RecentColorStore.sanitizedCopy(List.of(0x123456, -1, 0x123456, 0xABCDEF))
		);
	}
}
