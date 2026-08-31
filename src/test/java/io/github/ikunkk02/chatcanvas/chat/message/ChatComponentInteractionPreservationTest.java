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
				TestAction.RUN_COMMAND, "/tpaccept"));
		MutableText deny = Text.literal("[deny]").setStyle(interactiveStyle(
				TestAction.SUGGEST_COMMAND, "/tpdeny"));
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
				TestAction.RUN_COMMAND, "/tpaccept");
		Style denyStyle = interactiveStyle(
				TestAction.SUGGEST_COMMAND, "/tpdeny")
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
		assertEquals(TestAction.RUN_COMMAND, ((TestClickEvent) accept.getClickEvent()).action());
		assertEquals("/tpaccept", clickValue(accept.getClickEvent()));
		assertEquals("/tpaccept", accept.getInsertion());
		assertSame(acceptStyle, accept);
		assertTrue(accept.isBold());
		assertTrue(accept.isItalic());
		assertTrue(accept.isUnderlined());
		assertTrue(accept.isStrikethrough());
		assertTrue(accept.isObfuscated());

		assertEquals(TestAction.SUGGEST_COMMAND, ((TestClickEvent) deny.getClickEvent()).action());
		assertEquals("/tpdeny", clickValue(deny.getClickEvent()));
		assertSame(denyStyle, deny);
		assertFalse(accept.equals(deny));
	}

	private static Style interactiveStyle(
			TestAction action, String value) {
		return Style.EMPTY
				.withClickEvent(clickEvent(action, value))
				.withInsertion(value)
				.withColor(0x33DD88)
				.withBold(true)
				.withItalic(true)
				.withUnderline(true)
				.withStrikethrough(true)
				.withObfuscated(true);
	}

	private static ClickEvent clickEvent(TestAction action, String value) {
		return new TestClickEvent(action, value);
	}

	private static String clickValue(ClickEvent event) {
		if (event instanceof TestClickEvent testEvent) return testEvent.value();
		if (event instanceof ClickEvent.RunCommand command) return command.command();
		if (event instanceof ClickEvent.SuggestCommand command) return command.command();
		if (event instanceof ClickEvent.CopyToClipboard copy) return copy.value();
		throw new IllegalArgumentException("Unsupported test event: " + event);
	}

	private enum TestAction {
		RUN_COMMAND,
		SUGGEST_COMMAND,
		COPY_TO_CLIPBOARD
	}

	private record TestClickEvent(TestAction action, String value) implements ClickEvent {
		@Override
		public Action getAction() {
			return null;
		}
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
