package io.github.ikunkk02.chatcanvas.chat.message;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
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
	@BeforeAll
	static void bootstrapMinecraftRegistries() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void classificationAndHistoryKeepTheOriginalComponentTree() {
		MutableComponent accept = Component.literal("[accept]").setStyle(interactiveStyle(
				ClickEvent.Action.RUN_COMMAND, "/tpaccept"));
		MutableComponent deny = Component.literal("[deny]").setStyle(interactiveStyle(
				ClickEvent.Action.SUGGEST_COMMAND, "/tpdeny"));
		MutableComponent original = Component.literal("PlayerB requests teleport ")
				.withStyle(style -> style.withItalic(true))
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
				ClickEvent.Action.RUN_COMMAND, "/tpaccept");
		Style denyStyle = interactiveStyle(
				ClickEvent.Action.SUGGEST_COMMAND, "/tpdeny")
				.withColor(0x55AAFF);
		Component message = Component.literal("prefix ")
				.append(Component.literal("[accept]").setStyle(acceptStyle))
				.append(" ")
				.append(Component.literal("[deny]").setStyle(denyStyle));

		List<StyledRun> runs = styledRuns(message);
		Style prefix = styleFor(runs, "prefix ");
		Style accept = styleFor(runs, "[accept]");
		Style deny = styleFor(runs, "[deny]");

		assertNull(prefix.getClickEvent());
		assertNull(prefix.getHoverEvent());
		assertEquals(ClickEvent.Action.RUN_COMMAND, accept.getClickEvent().action());
		assertEquals("/tpaccept", ((ClickEvent.RunCommand) accept.getClickEvent()).command());
		assertEquals("/tpaccept", accept.getInsertion());
		assertSame(acceptStyle, accept);
		assertTrue(accept.isBold());
		assertTrue(accept.isItalic());
		assertTrue(accept.isUnderlined());
		assertTrue(accept.isStrikethrough());
		assertTrue(accept.isObfuscated());

		assertEquals(ClickEvent.Action.SUGGEST_COMMAND, deny.getClickEvent().action());
		assertEquals("/tpdeny", ((ClickEvent.SuggestCommand) deny.getClickEvent()).command());
		assertSame(denyStyle, deny);
		assertFalse(accept.equals(deny));
	}

	private static Style interactiveStyle(
			ClickEvent.Action action, String value) {
		ClickEvent event = switch (action) {
			case RUN_COMMAND -> new ClickEvent.RunCommand(value);
			case SUGGEST_COMMAND -> new ClickEvent.SuggestCommand(value);
			default -> throw new IllegalArgumentException("Unsupported test click action: " + action);
		};
		return Style.EMPTY
				.withClickEvent(event)
				.withInsertion(value)
				.withColor(0x33DD88)
				.withBold(true)
				.withItalic(true)
				.withUnderlined(true)
				.withStrikethrough(true)
				.withObfuscated(true);
	}

	private static List<StyledRun> styledRuns(Component text) {
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
