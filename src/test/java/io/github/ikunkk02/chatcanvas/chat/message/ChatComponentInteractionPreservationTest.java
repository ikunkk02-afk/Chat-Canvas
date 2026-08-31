package io.github.ikunkk02.chatcanvas.chat.message;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatComponentInteractionPreservationTest {
	@Test
	void classificationAndHistoryKeepTheOriginalComponentTree() {
		MutableText accept = Text.literal("[accept]").setStyle(interactiveStyle(
				new ClickEvent.RunCommand("/tpaccept")));
		MutableText deny = Text.literal("[deny]").setStyle(interactiveStyle(
				new ClickEvent.SuggestCommand("/tpdeny")));
		MutableText original = Text.literal("PlayerB requests teleport ")
				.styled(style -> style.withItalic(true))
				.append(accept)
				.append(" ")
				.append(deny);

		DefaultMessageClassifier classifier = new DefaultMessageClassifier();
		ClassifiedMessage classified = classifier.classify(original,
				MessageContext.direct(List.of(), null, "Steve"));
		assertSame(original, classified.content());
		assertSame(accept, original.getSiblings().get(0));
		assertSame(deny, original.getSiblings().get(2));

		ChatCanvasMessage stored = new ChatCanvasMessage(
				UUID.randomUUID(), classified.channel(), classified.source(),
				classified.senderUuid(), classified.senderName(), classified.content(),
				System.currentTimeMillis(), classified.selfMessage(), false);
		ChatCanvasMessageManager manager = new ChatCanvasMessageManager(4, 4);
		assertTrue(manager.add(stored));
		assertSame(original,
				manager.history(classified.channel()).messages().getFirst().content());
	}

	@Test
	void styledTraversalRetainsIndependentStyleIdentityAndFormatting() {
		Style acceptStyle = interactiveStyle(
				new ClickEvent.RunCommand("/tpaccept"));
		Style denyStyle = interactiveStyle(
				new ClickEvent.SuggestCommand("/tpdeny"))
				.withColor(0x55AAFF);
		Text message = Text.literal("prefix ")
				.append(Text.literal("[accept]").setStyle(acceptStyle))
				.append(" ")
				.append(Text.literal("[deny]").setStyle(denyStyle));

		List<StyledRun> runs = styledRuns(message);
		Style prefix = styleFor(runs, "prefix ");
		Style accept = styleFor(runs, "[accept]");
		Style deny = styleFor(runs, "[deny]");

		assertNull(prefix.getClickEvent());
		assertNull(prefix.getHoverEvent());
		assertTrue(accept.getClickEvent() instanceof ClickEvent.RunCommand);
		assertEquals("/tpaccept", clickValue(accept.getClickEvent()));
		assertEquals("/tpaccept", accept.getInsertion());
		assertSame(acceptStyle, accept);
		assertTrue(accept.isBold());
		assertTrue(accept.isItalic());
		assertTrue(accept.isUnderlined());
		assertTrue(accept.isStrikethrough());
		assertTrue(accept.isObfuscated());

		assertTrue(deny.getClickEvent() instanceof ClickEvent.SuggestCommand);
		assertEquals("/tpdeny", clickValue(deny.getClickEvent()));
		assertSame(denyStyle, deny);
		assertFalse(accept.equals(deny));
	}

	private static Style interactiveStyle(ClickEvent clickEvent) {
		return Style.EMPTY
				.withClickEvent(clickEvent)
				.withInsertion(clickValue(clickEvent))
				.withColor(0x33DD88)
				.withBold(true)
				.withItalic(true)
				.withUnderline(true)
				.withStrikethrough(true)
				.withObfuscated(true);
	}

	private static String clickValue(ClickEvent event) {
		if (event instanceof ClickEvent.RunCommand command) return command.command();
		if (event instanceof ClickEvent.SuggestCommand command) return command.command();
		if (event instanceof ClickEvent.CopyToClipboard clipboard) return clipboard.value();
		throw new IllegalArgumentException("Unsupported test event: " + event);
	}

	private static List<StyledRun> styledRuns(Text text) {
		List<StyledRun> runs = new ArrayList<>();
		text.visit((style, value) -> {
			runs.add(new StyledRun(value, style));
			return Optional.empty();
		}, Style.EMPTY);
		return runs;
	}

	private static Style styleFor(List<StyledRun> runs, String text) {
		return runs.stream()
				.filter(run -> run.text().equals(text))
				.findFirst()
				.orElseThrow()
				.style();
	}

	private record StyledRun(String text, Style style) {
	}
}
