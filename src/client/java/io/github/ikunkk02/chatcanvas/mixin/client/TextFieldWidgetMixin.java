package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.text.ChatCanvasTextFieldRegistry;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputMode;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextHitTester;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextMetrics;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextRenderer;
import io.github.ikunkk02.chatcanvas.chat.text.UnicodeTextNavigator;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringUtil;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiFunction;

@Mixin(EditBox.class)
public abstract class TextFieldWidgetMixin {
	@Shadow @Final
	private Font textRenderer;
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
	private BiFunction<String, Integer, FormattedCharSequence> renderTextProvider;
	@Shadow
	private long lastSwitchFocusTime;

	@Shadow
	public abstract int getInnerWidth();
	@Shadow
	public abstract void setCursor(int cursor, boolean shiftKeyPressed);
	@Shadow
	public abstract int getWordSkipPosition(int wordOffset);
	@Shadow
	private void drawSelectionHighlight(
			GuiGraphicsExtractor context, int x1, int y1, int x2, int y2) {
		throw new AssertionError();
	}

	@Inject(method = "onClick", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$locateSpacedClick(
			double mouseX, double mouseY, CallbackInfo ci) {
		EditBox self = (EditBox) (Object) this;
		double spacing = chat_canvas$spacing(self);
		if (Double.isNaN(spacing)) return;
		int localX = Mth.floor(mouseX) - self.getX();
		if (drawsBackground) localX -= 4;
		String remaining = text.substring(Math.min(firstCharacterIndex, text.length()));
		String visible = SpacedTextMetrics.trimToWidth(
				textRenderer, remaining, getInnerWidth(), spacing);
		int localIndex = SpacedTextHitTester.utf16IndexAt(
				textRenderer, visible, spacing, localX);
		setCursor(UnicodeTextNavigator.nearestGraphemeBoundary(
				text, firstCharacterIndex + localIndex), hasShiftDown());
		ci.cancel();
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$navigateUnicodeClusters(
			int keyCode, int scanCode, int modifiers,
			CallbackInfoReturnable<Boolean> cir) {
		EditBox self = (EditBox) (Object) this;
		if (!ChatCanvasTextFieldRegistry.isChatField(self)
				|| !self.isFocused()) return;
		boolean shift = hasShiftDown();
		boolean control = hasControlDown();
		if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT
				|| keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT) {
			boolean right = keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
			int target;
			if (control) {
				target = getWordSkipPosition(right ? 1 : -1);
				target = right
						? UnicodeTextNavigator.ceilGraphemeBoundary(text, target)
						: UnicodeTextNavigator.floorGraphemeBoundary(text, target);
			} else {
				target = right
						? UnicodeTextNavigator.nextGraphemeBoundary(
								text, selectionStart)
						: UnicodeTextNavigator.previousGraphemeBoundary(
								text, selectionStart);
			}
			setCursor(target, shift);
			cir.setReturnValue(true);
			return;
		}
		if (!control && editable
				&& (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE
				|| keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE)) {
			UnicodeTextNavigator.EditResult result =
					keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE
							? UnicodeTextNavigator.deletePreviousGrapheme(
									text, selectionStart, selectionEnd)
							: UnicodeTextNavigator.deleteNextGrapheme(
									text, selectionStart, selectionEnd);
			chat_canvas$applyEdit(self, result);
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$writeWholeUnicodeText(
			String value, CallbackInfo ci) {
		EditBox self = (EditBox) (Object) this;
		if (!ChatCanvasTextFieldRegistry.isChatField(self)) return;
		String sanitized = StringUtil.stripInvalidChars(
				value == null ? "" : value);
		if (!UnicodeTextNavigator.isWellFormedUtf16(sanitized)) {
			ci.cancel();
			return;
		}
		UnicodeTextNavigator.EditResult result =
				UnicodeTextNavigator.replaceSelection(
						text, selectionStart, selectionEnd,
						sanitized, maxLength);
		if (!result.limitExceeded()) chat_canvas$applyEdit(self, result);
		ci.cancel();
	}

	@ModifyVariable(method = "setText", at = @At("HEAD"), argsOnly = true)
	private String chat_canvas$truncateWholeUnicodeText(String value) {
		EditBox self = (EditBox) (Object) this;
		if (!ChatCanvasTextFieldRegistry.isChatField(self)) return value;
		return UnicodeTextNavigator.truncateAtGraphemeBoundary(value, maxLength);
	}

	@Inject(method = "updateFirstCharacterIndex", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$scrollSpacedInput(int cursor, CallbackInfo ci) {
		EditBox self = (EditBox) (Object) this;
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
		firstCharacterIndex = UnicodeTextNavigator.floorGraphemeBoundary(
				text, Mth.clamp(firstCharacterIndex, 0, text.length()));
		ci.cancel();
	}

	@Inject(method = "getCharacterX", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$getSpacedCharacterX(
			int index, CallbackInfoReturnable<Integer> cir) {
		EditBox self = (EditBox) (Object) this;
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
			GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		EditBox self = (EditBox) (Object) this;
		double spacing = chat_canvas$spacing(self);
		if (Double.isNaN(spacing) || drawsBackground) return;
		ci.cancel();
		if (!self.isVisible()) return;

		int color = editable ? editableColor : uneditableColor;
		int first = Math.min(firstCharacterIndex, text.length());
		String visible = SpacedTextMetrics.trimToWidth(
				textRenderer, text.substring(first), getInnerWidth(), spacing);
		int cursorOffset = Mth.clamp(selectionStart - first, 0, visible.length());
		boolean cursorVisible = selectionStart >= first
				&& selectionStart <= first + visible.length();
		int selectionOffset = Mth.clamp(selectionEnd - first, 0, visible.length());
		int textX = self.getX();
		int textY = self.getY();
		FormattedCharSequence rendered = renderTextProvider.apply(visible, first);
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
					FormattedCharSequence.styledForwardsVisitedString(suggestion, net.minecraft.text.Style.EMPTY),
					endX, textY, ARGB.GRAY, true, spacing);
		}

		boolean blink = self.isFocused()
				&& (Util.getMillis() - lastSwitchFocusTime) / 300L % 2L == 0L
				&& cursorVisible;
		if (blink) {
			if (hasFollowing) {
				context.fill(
						RenderPipelines.getGuiOverlay(),
						cursorX, textY - 1, cursorX + 1, textY + 10,
						-3092272);
			} else {
				context.drawString(textRenderer, "_", cursorX, textY, color);
			}
		}
		if (selectionOffset != cursorOffset) {
			drawSelectionHighlight(
					context, cursorX, textY - 1, selectionX - 1, textY + 10);
		}
	}

	@Unique
	private int chat_canvas$xAtUtf16(
			FormattedCharSequence rendered, String visible, double spacing, int utf16Index) {
		int codePointIndex = visible.codePointCount(
				0, Math.max(0, Math.min(utf16Index, visible.length())));
		return (int) Math.round(SpacedTextMetrics.xAtCodePoint(
				textRenderer, rendered, spacing, codePointIndex));
	}

	@Unique
	private static double chat_canvas$spacing(EditBox field) {
		if (!ChatCanvasTextFieldRegistry.isChatField(field)) return Double.NaN;
		ChatCanvasInputMode mode = ChatCanvasTextFieldRegistry.modeOf(field);
		double spacing = mode == ChatCanvasInputMode.COMMAND
				? ChatCanvasConfig.instance().commandSystem().text().characterSpacing()
				: ChatCanvasConfig.instance().text().characterSpacing();
		return spacing;
	}

	@Unique
	private void chat_canvas$applyEdit(
			EditBox field, UnicodeTextNavigator.EditResult result) {
		if (!result.changed()) {
			field.setCursor(result.cursor(), false);
			return;
		}
		field.setValue(result.text());
		field.setCursor(result.cursor(), false);
		field.setHighlightPos(result.selectionEnd());
	}
}
