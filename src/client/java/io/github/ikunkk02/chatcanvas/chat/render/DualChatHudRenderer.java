package io.github.ikunkk02.chatcanvas.chat.render;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitbox;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitboxRegistry;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputMode;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputScreenBridge;
import io.github.ikunkk02.chatcanvas.chat.layout.ChannelMessageLayoutEngine;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundMetrics;
import io.github.ikunkk02.chatcanvas.chat.layout.PlayerChatLayoutStrategies;
import io.github.ikunkk02.chatcanvas.chat.layout.PlayerChatLayoutStrategy;
import io.github.ikunkk02.chatcanvas.chat.layout.RuntimeChatBounds;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasChannel;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessage;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageManager;
import io.github.ikunkk02.chatcanvas.chat.message.ChatChannelHistory;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextHitTester;
import io.github.ikunkk02.chatcanvas.chat.text.ChatHeadsCompat;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextMetrics;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextRenderer;
import io.github.ikunkk02.chatcanvas.config.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class DualChatHudRenderer {
	private static final DualChatHudRenderer INSTANCE = new DualChatHudRenderer();
	private final List<HitLine> hitLines = new ArrayList<>();
	private boolean healthy = true;
	private boolean active;

	private DualChatHudRenderer() {}

	public static DualChatHudRenderer instance() {
		return INSTANCE;
	}

	public boolean render(DrawContext context, int mouseX, int mouseY, boolean focused) {
		if (!ChatCanvasConfig.instance().enabled() || !healthy) {
			active = false;
			return false;
		}
		try {
			active = true;
			hitLines.clear();
			PlayerNameHitboxRegistry.beginFrame();
			MinecraftClient client = Minecraft.getInstance();
			if (ChatCanvasConfig.instance().playerChatEnabled()) {
				renderChannel(context, client, ChatCanvasChannel.PLAYER_CHAT,
						ChatCanvasConfig.instance().layout(),
						ChatCanvasConfig.instance().text(),
						ChatCanvasConfig.instance().background(), 0xFFFFFF, 10, 1);
			}
			CommandSystemConfig command = ChatCanvasConfig.instance().commandSystem();
			ChatCanvasMessageManager.instance().commandSystem()
					.setMaximumMessages(command.maximumMessages());
			if (command.enabled()) {
				renderChannel(context, client, ChatCanvasChannel.COMMAND_SYSTEM,
						command.layout(), command.text(), command.background(),
						command.textColor(), command.fadeSeconds(),
						(int) Math.round(command.messageSpacing()));
			}
			return true;
		} catch (Throwable throwable) {
			healthy = false;
			active = false;
			hitLines.clear();
			PlayerNameHitboxRegistry.clear();
			ChatCanvas.LOGGER.error("Chat Canvas dual chat renderer failed; restoring vanilla ChatHud", throwable);
			return false;
		}
	}

	private void renderChannel(DrawContext context, MinecraftClient client,
							   ChatCanvasChannel channel, LayoutConfig layoutConfig,
							   ChatTextConfig text, ChatBackgroundConfig background,
							   int rgb, int fadeSeconds, int messageSpacing) {
		PixelLayout box = layoutConfig.toPixels(
				client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
		int renderBottom = messageBottom(client, channel, box, text);
		int padding = background.horizontalPadding();
		int available = Math.max(1, box.width() - padding * 2 - 2);
		int lineHeight = Math.max(1, (int) Math.ceil(
				client.textRenderer.fontHeight * text.fontScale() * text.lineSpacing()));
		ChatChannelHistory history = ChatCanvasMessageManager.instance().history(channel);
		List<ChatCanvasMessage> messages = history.messages();
		boolean open = client.currentScreen instanceof ChatScreen;
		long now = System.currentTimeMillis();
		double scroll = history.scrollOffsetPixels();
		int cursorBottom = renderBottom + (int) Math.round(scroll) - padding;
		int contentLeft = box.x() + padding;
		int contentRight = contentLeft + available;
		context.enableScissor(box.x(), box.y(), box.right(), renderBottom);
		for (int messageIndex = messages.size() - 1; messageIndex >= 0; messageIndex--) {
			ChatCanvasMessage message = messages.get(messageIndex);
			float alpha = open ? 1.0f : fadeAlpha(now - message.receivedAt(), fadeSeconds);
			if (alpha <= 0.01f && scroll <= 0.0) continue;
			PlayerChatLayoutMode layoutMode = channel == ChatCanvasChannel.PLAYER_CHAT
					? ChatCanvasConfig.instance().playerChatLayoutMode()
					: PlayerChatLayoutMode.CLASSIC;
			PlayerChatLayoutStrategy strategy =
					PlayerChatLayoutStrategies.forMode(layoutMode);
			int candidateHeadWidth = channel == ChatCanvasChannel.PLAYER_CHAT
					? (int) Math.ceil(ChatHeadsCompat.channelHeadWidth(
							message, client) * text.fontScale())
					: 0;
			int headWidth = strategy.reserveHead(message.selfMessage())
					? candidateHeadWidth : 0;
			double splitRatio = channel == ChatCanvasChannel.PLAYER_CHAT
					? ChatCanvasConfig.instance().splitMessageMaxWidthRatio()
					: ChatCanvasSettings.DEFAULT_SPLIT_MESSAGE_MAX_WIDTH_RATIO;
			int wrapWidth = channel == ChatCanvasChannel.PLAYER_CHAT
					? strategy.wrapWidth(
							available, headWidth, splitRatio, message.selfMessage())
					: available;
			int visualSafety = text.shadow() ? 2 : 1;
			ChannelMessageLayoutEngine.Layout layout = ChannelMessageLayoutEngine.instance().layout(
					message, client.textRenderer, wrapWidth,
					text, lineHeight,
					messageSpacing, history.layoutEpoch(), layoutMode, splitRatio,
					visualSafety, client.getWindow().getScaleFactor());
			int messageTop = cursorBottom - layout.height();
			if (messageTop < renderBottom && cursorBottom > box.y()) {
				drawMessage(context, client, channel, message, messageIndex, layout,
						contentLeft, contentRight, messageTop, lineHeight, text, background,
						rgb, alpha, headWidth, strategy, layoutMode);
			}
			cursorBottom = messageTop;
			if (cursorBottom < box.y() && scroll <= 0.0) break;
		}
		context.disableScissor();
		int contentHeight = renderBottom + (int) Math.round(scroll) - padding - cursorBottom;
		double maximum = Math.max(0, contentHeight - (renderBottom - box.y()) + padding * 2);
		if (history.scrollOffsetPixels() > maximum) history.setScrollOffset(maximum, maximum);
	}

	private void drawMessage(DrawContext context, MinecraftClient client,
							 ChatCanvasChannel channel, ChatCanvasMessage message, int messageIndex,
							 ChannelMessageLayoutEngine.Layout layout,
							 int contentLeft, int contentRight, int top,
							 int lineHeight, ChatTextConfig text, ChatBackgroundConfig background,
							 int rgb, float alpha, int headWidth,
							 PlayerChatLayoutStrategy strategy,
							 PlayerChatLayoutMode layoutMode) {
		int backgroundColor = ChatBackgroundMetrics.composeBackgroundColor(
				background.messageColor(), background.messageOpacity(), alpha);
		int y = top;
		for (int lineIndex = 0; lineIndex < layout.lines().size(); lineIndex++) {
			ChannelMessageLayoutEngine.Line layoutLine = layout.lines().get(lineIndex);
			int textX = channel == ChatCanvasChannel.PLAYER_CHAT
					? strategy.textX(contentLeft, contentRight, layoutLine.width(),
							headWidth, message.selfMessage())
					: contentLeft;
			int lineWidth = layoutLine.width();
			int backgroundLeft = layoutMode == PlayerChatLayoutMode.SPLIT_ALIGNMENT
					&& message.selfMessage()
					? textX - background.horizontalPadding()
					: contentLeft - background.horizontalPadding();
			if (background.messageMode() != MessageBackgroundMode.HIDDEN && backgroundColor >>> 24 != 0) {
				context.fill(backgroundLeft, y - background.verticalPadding(),
						textX + lineWidth + background.horizontalPadding(),
						y + lineHeight + background.verticalPadding(), backgroundColor);
			}
			int color = ((Math.max(4, Math.min(255,
					(int) Math.round(alpha * text.textOpacity() * 255)))) << 24)
					| (rgb & 0xFFFFFF);
			context.getMatrices().push();
			context.getMatrices().translate(textX, y, 0);
			context.getMatrices().scale((float) text.fontScale(), (float) text.fontScale(), 1);
			if (channel == ChatCanvasChannel.COMMAND_SYSTEM
					&& ChatCanvasConfig.instance().commandSystem().outline()) {
				CommandSystemConfig command = ChatCanvasConfig.instance().commandSystem();
				int outlineAlpha = Math.max(0, Math.min(255, (int) Math.round(
						alpha * command.outlineOpacity() * 255)));
				int outlineColor = (outlineAlpha << 24) | command.outlineColor();
				SpacedTextRenderer.draw(context, client.textRenderer, layoutLine.text(), -1, 0,
						outlineColor, false, text.characterSpacing());
				SpacedTextRenderer.draw(context, client.textRenderer, layoutLine.text(), 1, 0,
						outlineColor, false, text.characterSpacing());
				SpacedTextRenderer.draw(context, client.textRenderer, layoutLine.text(), 0, -1,
						outlineColor, false, text.characterSpacing());
				SpacedTextRenderer.draw(context, client.textRenderer, layoutLine.text(), 0, 1,
						outlineColor, false, text.characterSpacing());
			}
			SpacedTextRenderer.draw(context, client.textRenderer, layoutLine.text(), 0, 0,
					color, text.shadow(), text.characterSpacing());
			context.getMatrices().pop();
			if (lineIndex == 0 && headWidth > 0) {
				int headX = strategy.headX(
						contentLeft, textX, headWidth, message.selfMessage());
				context.getMatrices().push();
				context.getMatrices().translate(headX, y, 0);
				context.getMatrices().scale(
						(float) text.fontScale(), (float) text.fontScale(), 1);
				ChatHeadsCompat.renderChannelHead(
						context, message, client, 0, 0, alpha);
				context.getMatrices().pop();
			}
			HitLine hit = new HitLine(channel, message, layoutLine.text(),
					textX, y, lineWidth, lineHeight,
					text.fontScale(), text.characterSpacing());
			hitLines.add(hit);
			if (channel == ChatCanvasChannel.PLAYER_CHAT
					&& layoutLine.playerNameRange() != null) {
				addPlayerNameHitbox(client, message, messageIndex, layoutLine, hit);
			}
			y += lineHeight;
		}
	}

	private void addPlayerNameHitbox(MinecraftClient client, ChatCanvasMessage message,
									 int messageIndex,
									 ChannelMessageLayoutEngine.Line layoutLine,
									 HitLine line) {
		if (message.senderName() == null) return;
		String name = message.senderName().getString();
		double start = SpacedTextMetrics.xAtCodePoint(
				client.textRenderer, layoutLine.text(), line.spacing(),
				layoutLine.playerNameRange().startCodePoint()) * line.scale();
		double end = SpacedTextMetrics.xAtCodePoint(
				client.textRenderer, layoutLine.text(), line.spacing(),
				layoutLine.playerNameRange().endCodePoint()) * line.scale();
		double width = Math.max(0.0, end - start);
		PlayerNameHitboxRegistry.add(new PlayerNameHitbox(
				message.senderUuid(), name, messageIndex,
				line.x() + start, line.y(), line.x() + start + width,
				line.y() + line.height()));
	}

	public boolean scroll(double mouseX, double mouseY, double amount) {
		if (!active) return false;
		ChatCanvasChannel channel = channelAt(mouseX, mouseY);
		if (channel == null) return false;
		ChatChannelHistory history = ChatCanvasMessageManager.instance().history(channel);
		double speed = channel == ChatCanvasChannel.COMMAND_SYSTEM
				? ChatCanvasConfig.instance().commandSystem().scrollSpeed() : 18.0;
		history.scrollBy(-amount * speed, maximumScroll(channel));
		return true;
	}

	public boolean copyCommandAt(double mouseX, double mouseY) {
		HitLine line = hitAt(mouseX, mouseY);
		if (line == null || line.channel() != ChatCanvasChannel.COMMAND_SYSTEM) return false;
		Minecraft.getInstance().keyboard.setClipboard(line.message().content().getString());
		return true;
	}

	@Nullable
	public Style styleAt(double mouseX, double mouseY) {
		HitLine line = hitAt(mouseX, mouseY);
		if (line == null) return null;
		return SpacedTextHitTester.styleAt(Minecraft.getInstance().textRenderer,
				line.text(), line.spacing(), (mouseX - line.x()) / line.scale());
	}

	public boolean active() {
		return active && healthy && ChatCanvasConfig.instance().enabled();
	}

	public void resetWorld() {
		hitLines.clear();
		ChannelMessageLayoutEngine.instance().clearWorld();
	}

	public void invalidateLayouts() {
		ChannelMessageLayoutEngine.instance().invalidateResources();
		ChatCanvasMessageManager.instance().invalidateLayouts();
	}

	public void invalidatePlayerLayouts() {
		hitLines.clear();
		PlayerNameHitboxRegistry.clear();
		ChannelMessageLayoutEngine.instance().invalidateChannel(
				ChatCanvasChannel.PLAYER_CHAT);
		ChatCanvasMessageManager.instance().invalidateLayout(
				ChatCanvasChannel.PLAYER_CHAT);
	}

	private HitLine hitAt(double x, double y) {
		for (int index = hitLines.size() - 1; index >= 0; index--) {
			HitLine line = hitLines.get(index);
			if (x >= line.x() && x <= line.x() + line.width()
					&& y >= line.y() && y <= line.y() + line.height()) return line;
		}
		return null;
	}

	private ChatCanvasChannel channelAt(double x, double y) {
		MinecraftClient client = Minecraft.getInstance();
		CommandSystemConfig command = ChatCanvasConfig.instance().commandSystem();
		if (command.enabled() && contains(command.layout().toPixels(
				client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight()), x, y)) {
			return ChatCanvasChannel.COMMAND_SYSTEM;
		}
		if (ChatCanvasConfig.instance().playerChatEnabled() && contains(
				ChatCanvasConfig.instance().layout().toPixels(
						client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight()), x, y)) {
			return ChatCanvasChannel.PLAYER_CHAT;
		}
		return null;
	}

	private double maximumScroll(ChatCanvasChannel channel) {
		MinecraftClient client = Minecraft.getInstance();
		LayoutConfig layout = channel == ChatCanvasChannel.PLAYER_CHAT
				? ChatCanvasConfig.instance().layout() : ChatCanvasConfig.instance().commandSystem().layout();
		ChatTextConfig text = channel == ChatCanvasChannel.PLAYER_CHAT
				? ChatCanvasConfig.instance().text() : ChatCanvasConfig.instance().commandSystem().text();
		ChatBackgroundConfig background = channel == ChatCanvasChannel.PLAYER_CHAT
				? ChatCanvasConfig.instance().background() : ChatCanvasConfig.instance().commandSystem().background();
		int spacing = channel == ChatCanvasChannel.PLAYER_CHAT ? 1
				: (int) Math.round(ChatCanvasConfig.instance().commandSystem().messageSpacing());
		PixelLayout box = layout.toPixels(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
		int renderBottom = messageBottom(client, channel, box, text);
		int available = Math.max(1, box.width() - background.horizontalPadding() * 2 - 2);
		int lineHeight = Math.max(1, (int) Math.ceil(
				client.textRenderer.fontHeight * text.fontScale() * text.lineSpacing()));
		int total = 0;
		ChatChannelHistory history = ChatCanvasMessageManager.instance().history(channel);
		for (ChatCanvasMessage message : history.messages()) {
			PlayerChatLayoutMode layoutMode = channel == ChatCanvasChannel.PLAYER_CHAT
					? ChatCanvasConfig.instance().playerChatLayoutMode()
					: PlayerChatLayoutMode.CLASSIC;
			PlayerChatLayoutStrategy strategy =
					PlayerChatLayoutStrategies.forMode(layoutMode);
			int candidateHeadWidth = channel == ChatCanvasChannel.PLAYER_CHAT
					? (int) Math.ceil(ChatHeadsCompat.channelHeadWidth(
							message, client) * text.fontScale())
					: 0;
			int headWidth = strategy.reserveHead(message.selfMessage())
					? candidateHeadWidth : 0;
			double splitRatio = channel == ChatCanvasChannel.PLAYER_CHAT
					? ChatCanvasConfig.instance().splitMessageMaxWidthRatio()
					: ChatCanvasSettings.DEFAULT_SPLIT_MESSAGE_MAX_WIDTH_RATIO;
			int wrapWidth = channel == ChatCanvasChannel.PLAYER_CHAT
					? strategy.wrapWidth(
							available, headWidth, splitRatio, message.selfMessage())
					: available;
			total += ChannelMessageLayoutEngine.instance().layout(message, client.textRenderer,
					wrapWidth, text, lineHeight,
					spacing, history.layoutEpoch(), layoutMode, splitRatio,
					text.shadow() ? 2 : 1,
					client.getWindow().getScaleFactor()).height();
		}
		return Math.max(0, total - (renderBottom - box.y()) + background.verticalPadding() * 2);
	}

	private static int messageBottom(MinecraftClient client, ChatCanvasChannel channel,
									 PixelLayout box, ChatTextConfig text) {
		if (!(client.currentScreen instanceof ChatCanvasInputScreenBridge bridge)) {
			return box.bottom();
		}
		TextFieldWidget field = bridge.chat_canvas$activeInputField();
		if (field == null) return box.bottom();
		boolean belongsHere = channel == ChatCanvasChannel.COMMAND_SYSTEM
				? bridge.chat_canvas$inputMode() == ChatCanvasInputMode.COMMAND
				: bridge.chat_canvas$inputMode() == ChatCanvasInputMode.PLAYER_CHAT;
		if (!belongsHere) return box.bottom();
		int minimum = Math.max(1, (int) Math.ceil(
				client.textRenderer.fontHeight * text.fontScale()));
		return RuntimeChatBounds.calculate(
				box, true, field.getHeight(), RuntimeChatBounds.DEFAULT_INPUT_GAP, minimum)
				.messageBottom();
	}

	private static boolean contains(PixelLayout box, double x, double y) {
		return x >= box.x() && x <= box.right() && y >= box.y() && y <= box.bottom();
	}

	private static float fadeAlpha(long ageMs, int seconds) {
		long duration = Math.max(1_000L, seconds * 1_000L);
		if (ageMs <= duration * .7) return 1;
		return (float) Math.max(0, 1.0 - (ageMs - duration * .7) / (duration * .3));
	}

	private record HitLine(ChatCanvasChannel channel, ChatCanvasMessage message,
						   OrderedText text, int x, int y, int width, int height,
						   double scale, double spacing) {}
}
