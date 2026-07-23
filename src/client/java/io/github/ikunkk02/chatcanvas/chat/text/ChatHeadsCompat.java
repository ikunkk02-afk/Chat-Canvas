package io.github.ikunkk02.chatcanvas.chat.text;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;

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
	private static boolean reflectionFailed;
	private static Method getHeadData;
	private static Method getTextWidthDifference;
	private static Method codePointIndex;
	private static Method hasHeadPosition;

	private ChatHeadsCompat() {
	}

	public static boolean active() {
		return ACTIVE;
	}

	public static boolean isAtomicCodePoint(int codePoint) {
		return ACTIVE && Character.isValidCodePoint(codePoint);
	}

	public static int extraWidth(ChatHudLine.Visible line) {
		HeadGeometry geometry = geometry(line);
		return geometry == null ? 0 : geometry.width();
	}

	public static int widthBeforeCodePoint(ChatHudLine.Visible line, int codePointIndex) {
		HeadGeometry geometry = geometry(line);
		return geometry != null && geometry.insertionCodePoint() <= codePointIndex
				? geometry.width()
				: 0;
	}

	/**
	 * Converts a visual x coordinate back to the underlying OrderedText x.
	 * NaN identifies the avatar itself, which must not behave like player-name
	 * text or expose a Style.
	 */
	public static double textXAt(
			TextRenderer renderer, OrderedText text, double spacing,
			ChatHudLine.Visible line, double visualX) {
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

	private static synchronized HeadGeometry geometry(ChatHudLine.Visible line) {
		if (!ACTIVE || reflectionFailed || line == null) return null;
		try {
			resolve(line.getClass());
			Object headData = getHeadData.invoke(null, line);
			if (headData == null || !(Boolean) hasHeadPosition.invoke(headData)) return null;
			int width = ((Number) getTextWidthDifference.invoke(null, line)).intValue();
			if (width <= 0) return null;
			int insertion = Math.max(0, ((Number) codePointIndex.invoke(headData)).intValue());
			return new HeadGeometry(insertion, width);
		} catch (ReflectiveOperationException | RuntimeException ignored) {
			reflectionFailed = true;
			return null;
		}
	}

	private static void resolve(Class<?> visibleClass) throws ReflectiveOperationException {
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

	private record HeadGeometry(int insertionCodePoint, int width) {
	}
}
