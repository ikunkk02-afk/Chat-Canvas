package io.github.ikunkk02.chatcanvas.chat.text;

import io.github.ikunkk02.chatcanvas.ChatCanvas;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasChannel;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.util.FormattedCharSequence;

import java.lang.reflect.Method;

/**
 * Soft compatibility marker for Chat Heads. The spaced pipeline treats every
 * Unicode code point as one indivisible layout atom, so Chat Heads' injected
 * font element is visited and rendered exactly once without relying on a
 * version-specific private-use character.
 */
public final class ChatHeadsCompat {
	private static final boolean ACTIVE =
			FabricLoader.getInstance().isModLoaded("chat_heads")
					|| FabricLoader.getInstance().isModLoaded("chat-heads");
	private static boolean visibleReflectionFailed;
	private static boolean channelReflectionFailed;
	private static Method getHeadData;
	private static Method getTextWidthDifference;
	private static Method codePointIndex;
	private static Method hasHeadPosition;
	private static Method renderChatHead;
	private static Method channelHeadWidth;

	private ChatHeadsCompat() {
	}

	public static boolean active() {
		return ACTIVE;
	}

	public static boolean isAtomicCodePoint(int codePoint) {
		return ACTIVE && Character.isValidCodePoint(codePoint);
	}

	public static boolean channelAdapterAvailable() {
		if (!ACTIVE || channelReflectionFailed) return false;
		try {
			resolveChannelApi();
			return true;
		} catch (ReflectiveOperationException | RuntimeException failure) {
			failChannelAdapter(failure);
			return false;
		}
	}

	public static int channelHeadWidth(
			ChatCanvasMessage message, Minecraft client) {
		if (!channelMessageSupported(message, client)
				|| !channelAdapterAvailable()) {
			return 0;
		}
		try {
			return Math.max(0, ((Number) channelHeadWidth.invoke(null)).intValue());
		} catch (ReflectiveOperationException | RuntimeException failure) {
			failChannelAdapter(failure);
			return 0;
		}
	}

	public static boolean renderChannelHead(
			GuiGraphicsExtractor context,
			ChatCanvasMessage message,
			Minecraft client,
			int x,
			int y,
			float opacity
	) {
		if (!channelMessageSupported(message, client)
				|| !channelAdapterAvailable()) {
			return false;
		}
		PlayerListEntry player = client.getNetworkHandler()
				.getPlayerListEntry(message.senderUuid());
		if (player == null) return false;
		try {
			renderChatHead.invoke(
					null, context, x, y, player,
					Math.max(0.0f, Math.min(1.0f, opacity)));
			return true;
		} catch (ReflectiveOperationException | RuntimeException failure) {
			failChannelAdapter(failure);
			return false;
		}
	}

	public static int extraWidth(GuiMessage.Visible line) {
		HeadGeometry geometry = geometry(line);
		return geometry == null ? 0 : geometry.width();
	}

	public static int widthBeforeCodePoint(GuiMessage.Visible line, int codePointIndex) {
		HeadGeometry geometry = geometry(line);
		return geometry != null && geometry.insertionCodePoint() <= codePointIndex
				? geometry.width()
				: 0;
	}

	/**
	 * Converts a visual x coordinate back to the underlying FormattedCharSequence x.
	 * NaN identifies the avatar itself, which must not behave like player-name
	 * text or expose a Style.
	 */
	public static double textXAt(
			Font renderer, FormattedCharSequence text, double spacing,
			GuiMessage.Visible line, double visualX) {
		HeadGeometry geometry = geometry(line);
		if (geometry == null) return visualX;
		double insertionX = SpacedTextMetrics.xAtCodePoint(
				renderer, text, spacing, geometry.insertionCodePoint());
		if (visualX >= insertionX && visualX < insertionX + geometry.width()) {
			return Double.NaN;
		}
		return visualX >= insertionX + geometry.width()
				? visualX - geometry.width()
				: visualX;
	}

	private static synchronized HeadGeometry geometry(GuiMessage.Visible line) {
		if (!ACTIVE || visibleReflectionFailed || line == null) return null;
		try {
			resolveVisibleApi(line.getClass());
			Object headData = getHeadData.invoke(null, line);
			if (headData == null || !(Boolean) hasHeadPosition.invoke(headData)) return null;
			int width = ((Number) getTextWidthDifference.invoke(null, line)).intValue();
			if (width <= 0) return null;
			int insertion = Math.max(0, ((Number) codePointIndex.invoke(headData)).intValue());
			return new HeadGeometry(insertion, width);
		} catch (ReflectiveOperationException | RuntimeException ignored) {
			visibleReflectionFailed = true;
			return null;
		}
	}

	private static void resolveVisibleApi(Class<?> visibleClass)
			throws ReflectiveOperationException {
		if (getHeadData != null) return;
		Class<?> chatHeads = Class.forName("dzwdz.chat_heads.ChatHeads");
		for (Method method : chatHeads.getMethods()) {
			if (method.getParameterCount() != 1
					|| !method.getParameterTypes()[0].isAssignableFrom(visibleClass)) {
				continue;
			}
			if (method.getName().equals("getHeadData")) getHeadData = method;
			if (method.getName().equals("getTextWidthDifference")) {
				getTextWidthDifference = method;
			}
		}
		if (getHeadData == null || getTextWidthDifference == null) {
			throw new NoSuchMethodException("Chat Heads visible-line API");
		}
		Class<?> headDataClass = getHeadData.getReturnType();
		codePointIndex = headDataClass.getMethod("codePointIndex");
		hasHeadPosition = headDataClass.getMethod("hasHeadPosition");
	}

	private static synchronized void resolveChannelApi()
			throws ReflectiveOperationException {
		if (renderChatHead != null && channelHeadWidth != null) return;
		Class<?> chatHeads = Class.forName("dzwdz.chat_heads.ChatHeads");
		renderChatHead = chatHeads.getMethod(
				"renderChatHead",
				GuiGraphicsExtractor.class,
				int.class,
				int.class,
				PlayerListEntry.class,
				float.class);
		channelHeadWidth = chatHeads.getMethod("headWidth");
	}

	private static boolean channelMessageSupported(
			ChatCanvasMessage message, Minecraft client) {
		return ACTIVE
				&& message != null
				&& message.channel() == ChatCanvasChannel.PLAYER_CHAT
				&& message.senderUuid() != null
				&& client != null
				&& client.getNetworkHandler() != null
				&& client.getNetworkHandler()
				.getPlayerListEntry(message.senderUuid()) != null;
	}

	private static synchronized void failChannelAdapter(Throwable failure) {
		if (channelReflectionFailed) return;
		channelReflectionFailed = true;
		ChatCanvas.LOGGER.warn(
				"Chat Heads channel adapter unavailable; falling back to text-only chat",
				failure);
	}

	private record HeadGeometry(int insertionCodePoint, int width) {
	}
}
