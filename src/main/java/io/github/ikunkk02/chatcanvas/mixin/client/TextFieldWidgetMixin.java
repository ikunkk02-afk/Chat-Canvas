package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.text.ChatCanvasTextFieldRegistry;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputMode;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextHitTester;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextMetrics;
import io.github.ikunkk02.chatcanvas.chat.text.SpacedTextRenderer;
import io.github.ikunkk02.chatcanvas.chat.text.UnicodeTextNavigator;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
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
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;

@Mixin(EditBox.class)
public abstract class TextFieldWidgetMixin {
	@Shadow @Final
	private Font font;
	@Shadow
	private String value;
	@Shadow
	private int maxLength;
	@Shadow
	private boolean bordered;
	@Shadow
	private boolean isEditable;
	@Shadow
	private int displayPos;
	@Shadow
	private int cursorPos;
	@Shadow
	private int highlightPos;
	@Shadow
	private int textColor;
	@Shadow
	private int textColorUneditable;
	@Shadow @Nullable
	private String suggestion;
	@Shadow
	private BiFunction<String, Integer, FormattedCharSequence> formatter;
	@Shadow
	private long focusedTime;

	@Shadow
	public abstract int getInnerWidth();
	@Shadow
	public abstract void moveCursorTo(int cursor, boolean shiftKeyPressed);
	@Shadow
	public abstract int getWordPosition(int wordOffset);
	@Shadow
	private void renderHighlight(
			GuiGraphics context, int x1, int y1, int x2, int y2) {
		throw new AssertionError();
	}

	@Inject(method = "onClick", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$locateSpacedClick(
			double mouseX, double mouseY, CallbackInfo ci) {
		EditBox self = (EditBox) (Object) this;
		double spacing = chat_canvas$spacing(self);
		if (Double.isNaN(spacing)) return;
		int localX = Mth.floor(mouseX) - self.getX();
		if (bordered) localX -= 4;
		String remaining = value.substring(Math.min(displayPos, value.length()));
		String visible = SpacedTextMetrics.trimToWidth(
				font, remaining, getInnerWidth(), spacing);
		int localIndex = SpacedTextHitTester.utf16IndexAt(
				font, visible, spacing, localX);
		moveCursorTo(UnicodeTextNavigator.nearestGraphemeBoundary(
				value, displayPos + localIndex), Screen.hasShiftDown());
		ci.cancel();
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$navigateUnicodeClusters(
			int keyCode, int scanCode, int modifiers,
			CallbackInfoReturnable<Boolean> cir) {
		EditBox self = (EditBox) (Object) this;
		if (!ChatCanvasTextFieldRegistry.isChatField(self)
				|| !self.isFocused()) return;
		boolean shift = Screen.hasShiftDown();
		boolean control = Screen.hasControlDown();
		if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT
				|| keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT) {
			boolean right = keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
			int target;
			if (control) {
				target = getWordPosition(right ? 1 : -1);
				target = right
						? UnicodeTextNavigator.ceilGraphemeBoundary(value, target)
						: UnicodeTextNavigator.floorGraphemeBoundary(value, target);
			} else {
				target = right
						? UnicodeTextNavigator.nextGraphemeBoundary(
								value, cursorPos)
						: UnicodeTextNavigator.previousGraphemeBoundary(
								value, cursorPos);
			}
			moveCursorTo(target, shift);
			cir.setReturnValue(true);
			return;
		}
		if (!control && isEditable
				&& (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE
				|| keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE)) {
			UnicodeTextNavigator.EditResult result =
					keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE
							? UnicodeTextNavigator.deletePreviousGrapheme(
									value, cursorPos, highlightPos)
							: UnicodeTextNavigator.deleteNextGrapheme(
									value, cursorPos, highlightPos);
			chat_canvas$applyEdit(self, result);
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "insertText", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$writeWholeUnicodeText(
			String insertedText, CallbackInfo ci) {
		EditBox self = (EditBox) (Object) this;
		if (!ChatCanvasTextFieldRegistry.isChatField(self)) return;
		String sanitized = StringUtil.filterText(
				insertedText == null ? "" : insertedText);
		if (!UnicodeTextNavigator.isWellFormedUtf16(sanitized)) {
			ci.cancel();
			return;
		}
		UnicodeTextNavigator.EditResult result =
				UnicodeTextNavigator.replaceSelection(
						this.value, cursorPos, highlightPos,
						sanitized, maxLength);
		if (!result.limitExceeded()) chat_canvas$applyEdit(self, result);
		ci.cancel();
	}

	@ModifyVariable(method = "setValue", at = @At("HEAD"), argsOnly = true)
	private String chat_canvas$truncateWholeUnicodeText(String value) {
		EditBox self = (EditBox) (Object) this;
		if (!ChatCanvasTextFieldRegistry.isChatField(self)) return value;
		return UnicodeTextNavigator.truncateAtGraphemeBoundary(value, maxLength);
	}

	@Inject(method = "scrollTo", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$scrollSpacedInput(int cursor, CallbackInfo ci) {
		EditBox self = (EditBox) (Object) this;
		double spacing = chat_canvas$spacing(self);
		if (Double.isNaN(spacing)) return;
		displayPos = Math.min(displayPos, value.length());
		int width = getInnerWidth();
		String visible = SpacedTextMetrics.trimToWidth(
				font, value.substring(displayPos), width, spacing);
		int visibleEnd = displayPos + visible.length();
		if (cursor > visibleEnd || cursor == displayPos) {
			displayPos = SpacedTextMetrics.firstVisibleIndex(
					font, value, cursor, width, spacing);
		} else if (cursor < displayPos) {
			displayPos = cursor;
		}
		displayPos = UnicodeTextNavigator.floorGraphemeBoundary(
				value, Mth.clamp(displayPos, 0, value.length()));
		ci.cancel();
	}

	@Inject(method = "getScreenX", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$getSpacedCharacterX(
			int index, CallbackInfoReturnable<Integer> cir) {
		EditBox self = (EditBox) (Object) this;
		double spacing = chat_canvas$spacing(self);
		if (Double.isNaN(spacing)) return;
		if (index > value.length()) {
			cir.setReturnValue(self.getX());
			return;
		}
		cir.setReturnValue(self.getX() + (int) Math.round(
				SpacedTextMetrics.xAtUtf16(font, value, spacing, index)));
	}

	@Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$renderSpacedInput(
			GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		EditBox self = (EditBox) (Object) this;
		double spacing = chat_canvas$spacing(self);
		if (Double.isNaN(spacing) || bordered) return;
		ci.cancel();
		if (!self.isVisible()) return;

		int color = isEditable ? textColor : textColorUneditable;
		int first = Math.min(displayPos, value.length());
		String visible = SpacedTextMetrics.trimToWidth(
				font, value.substring(first), getInnerWidth(), spacing);
		int cursorOffset = Mth.clamp(cursorPos - first, 0, visible.length());
		boolean cursorVisible = cursorPos >= first
				&& cursorPos <= first + visible.length();
		int selectionOffset = Mth.clamp(highlightPos - first, 0, visible.length());
		int textX = self.getX();
		int textY = self.getY();
		FormattedCharSequence rendered = formatter.apply(visible, first);
		SpacedTextRenderer.draw(
				context, font, rendered, textX, textY, color, true, spacing);

		int cursorX = textX + chat_canvas$xAtUtf16(rendered, visible, spacing, cursorOffset);
		int selectionX = textX
				+ chat_canvas$xAtUtf16(rendered, visible, spacing, selectionOffset);
		boolean hasFollowing = cursorPos < value.length() || value.length() >= maxLength;
		if (cursorVisible && hasFollowing) cursorX--;

		if (!hasFollowing && suggestion != null) {
			int endX = textX + SpacedTextMetrics.width(font, rendered, spacing);
			SpacedTextRenderer.draw(
					context, font,
					FormattedCharSequence.forward(suggestion, net.minecraft.network.chat.Style.EMPTY),
					endX, textY, CommonColors.GRAY, true, spacing);
		}

		boolean blink = self.isFocused()
				&& (Util.getMillis() - focusedTime) / 300L % 2L == 0L
				&& cursorVisible;
		if (blink) {
			if (hasFollowing) {
				context.fill(
						RenderType.guiOverlay(),
						cursorX, textY - 1, cursorX + 1, textY + 10,
						-3092272);
			} else {
				context.drawString(font, "_", cursorX, textY, color);
			}
		}
		if (selectionOffset != cursorOffset) {
			renderHighlight(
					context, cursorX, textY - 1, selectionX - 1, textY + 10);
		}
	}

	@Unique
	private int chat_canvas$xAtUtf16(
			FormattedCharSequence rendered, String visible, double spacing, int utf16Index) {
		int codePointIndex = visible.codePointCount(
				0, Math.max(0, Math.min(utf16Index, visible.length())));
		return (int) Math.round(SpacedTextMetrics.xAtCodePoint(
				font, rendered, spacing, codePointIndex));
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
			field.moveCursorTo(result.cursor(), false);
			return;
		}
		field.setValue(result.text());
		field.moveCursorTo(result.cursor(), false);
		field.setHighlightPos(result.selectionEnd());
	}
}
