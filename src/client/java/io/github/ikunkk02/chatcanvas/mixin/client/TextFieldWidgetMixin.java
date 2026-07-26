package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.text.ChatCanvasTextFieldRegistry;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputMode;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextHitTester;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextMetrics;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextRenderer;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiFunction;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin {
	@Shadow @Final
	private TextRenderer textRenderer;
	@Shadow
	private String text;
	@Shadow
	private int maxLength;
	@Shadow
	private boolean drawsBackground;
	@Shadow
	private boolean editable;
	@Shadow
	private int firstCharacterIndex;
	@Shadow
	private int selectionStart;
	@Shadow
	private int selectionEnd;
	@Shadow
	private int editableColor;
	@Shadow
	private int uneditableColor;
	@Shadow @Nullable
	private String suggestion;
	@Shadow
	private BiFunction<String, Integer, OrderedText> renderTextProvider;
	@Shadow
	private long lastSwitchFocusTime;

	@Shadow
	public abstract int getInnerWidth();
	@Shadow
	public abstract void setCursor(int cursor, boolean shiftKeyPressed);
	@Shadow
	private void drawSelectionHighlight(
			DrawContext context, int x1, int y1, int x2, int y2) {
		throw new AssertionError();
	}

	@Inject(method = "onClick", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$locateSpacedClick(
			double mouseX, double mouseY, CallbackInfo ci) {
		TextFieldWidget self = (TextFieldWidget) (Object) this;
		double spacing = chat_canvas$spacing(self);
		if (Double.isNaN(spacing)) return;
		int localX = MathHelper.floor(mouseX) - self.getX();
		if (drawsBackground) localX -= 4;
		String remaining = text.substring(Math.min(firstCharacterIndex, text.length()));
		String visible = SpacedTextMetrics.trimToWidth(
				textRenderer, remaining, getInnerWidth(), spacing);
		int localIndex = SpacedTextHitTester.utf16IndexAt(
				textRenderer, visible, spacing, localX);
		setCursor(firstCharacterIndex + localIndex, Screen.hasShiftDown());
		ci.cancel();
	}

	@Inject(method = "updateFirstCharacterIndex", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$scrollSpacedInput(int cursor, CallbackInfo ci) {
		TextFieldWidget self = (TextFieldWidget) (Object) this;
		double spacing = chat_canvas$spacing(self);
		if (Double.isNaN(spacing)) return;
		firstCharacterIndex = Math.min(firstCharacterIndex, text.length());
		int width = getInnerWidth();
		String visible = SpacedTextMetrics.trimToWidth(
				textRenderer, text.substring(firstCharacterIndex), width, spacing);
		int visibleEnd = firstCharacterIndex + visible.length();
		if (cursor > visibleEnd || cursor == firstCharacterIndex) {
			firstCharacterIndex = SpacedTextMetrics.firstVisibleIndex(
					textRenderer, text, cursor, width, spacing);
		} else if (cursor < firstCharacterIndex) {
			firstCharacterIndex = cursor;
		}
		firstCharacterIndex = MathHelper.clamp(firstCharacterIndex, 0, text.length());
		if (firstCharacterIndex > 0
				&& firstCharacterIndex < text.length()
				&& Character.isLowSurrogate(text.charAt(firstCharacterIndex))) {
			firstCharacterIndex--;
		}
		ci.cancel();
	}

	@Inject(method = "getCharacterX", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$getSpacedCharacterX(
			int index, CallbackInfoReturnable<Integer> cir) {
		TextFieldWidget self = (TextFieldWidget) (Object) this;
		double spacing = chat_canvas$spacing(self);
		if (Double.isNaN(spacing)) return;
		if (index > text.length()) {
			cir.setReturnValue(self.getX());
			return;
		}
		cir.setReturnValue(self.getX() + (int) Math.round(
				SpacedTextMetrics.xAtUtf16(textRenderer, text, spacing, index)));
	}

	@Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$renderSpacedInput(
			DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		TextFieldWidget self = (TextFieldWidget) (Object) this;
		double spacing = chat_canvas$spacing(self);
		if (Double.isNaN(spacing) || drawsBackground) return;
		ci.cancel();
		if (!self.isVisible()) return;

		int color = editable ? editableColor : uneditableColor;
		int first = Math.min(firstCharacterIndex, text.length());
		String visible = SpacedTextMetrics.trimToWidth(
				textRenderer, text.substring(first), getInnerWidth(), spacing);
		int cursorOffset = MathHelper.clamp(selectionStart - first, 0, visible.length());
		boolean cursorVisible = selectionStart >= first
				&& selectionStart <= first + visible.length();
		int selectionOffset = MathHelper.clamp(selectionEnd - first, 0, visible.length());
		int textX = self.getX();
		int textY = self.getY();
		OrderedText rendered = renderTextProvider.apply(visible, first);
		SpacedTextRenderer.draw(
				context, textRenderer, rendered, textX, textY, color, true, spacing);

		int cursorX = textX + chat_canvas$xAtUtf16(rendered, visible, spacing, cursorOffset);
		int selectionX = textX
				+ chat_canvas$xAtUtf16(rendered, visible, spacing, selectionOffset);
		boolean hasFollowing = selectionStart < text.length() || text.length() >= maxLength;
		if (cursorVisible && hasFollowing) cursorX--;

		if (!hasFollowing && suggestion != null) {
			int endX = textX + SpacedTextMetrics.width(textRenderer, rendered, spacing);
			SpacedTextRenderer.draw(
					context, textRenderer,
					OrderedText.styledForwardsVisitedString(suggestion, net.minecraft.text.Style.EMPTY),
					endX, textY, Colors.GRAY, true, spacing);
		}

		boolean blink = self.isFocused()
				&& (Util.getMeasuringTimeMs() - lastSwitchFocusTime) / 300L % 2L == 0L
				&& cursorVisible;
		if (blink) {
			if (hasFollowing) {
				context.fill(
						RenderLayer.getGuiOverlay(),
						cursorX, textY - 1, cursorX + 1, textY + 10,
						-3092272);
			} else {
				context.drawTextWithShadow(textRenderer, "_", cursorX, textY, color);
			}
		}
		if (selectionOffset != cursorOffset) {
			drawSelectionHighlight(
					context, cursorX, textY - 1, selectionX - 1, textY + 10);
		}
	}

	@Unique
	private int chat_canvas$xAtUtf16(
			OrderedText rendered, String visible, double spacing, int utf16Index) {
		int codePointIndex = visible.codePointCount(
				0, Math.max(0, Math.min(utf16Index, visible.length())));
		return (int) Math.round(SpacedTextMetrics.xAtCodePoint(
				textRenderer, rendered, spacing, codePointIndex));
	}

	@Unique
	private static double chat_canvas$spacing(TextFieldWidget field) {
		if (!ChatCanvasTextFieldRegistry.isChatField(field)) return Double.NaN;
		ChatCanvasInputMode mode = ChatCanvasTextFieldRegistry.modeOf(field);
		double spacing = mode == ChatCanvasInputMode.COMMAND
				? ChatCanvasConfig.instance().commandSystem().text().characterSpacing()
				: ChatCanvasConfig.instance().text().characterSpacing();
		return Math.abs(spacing) < 0.00001 ? Double.NaN : spacing;
	}
}
