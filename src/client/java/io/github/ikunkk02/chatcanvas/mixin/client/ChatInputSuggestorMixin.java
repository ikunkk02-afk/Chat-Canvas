package io.github.ikunkk02.chatcanvas.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CommandSuggestions.class)
public abstract class ChatInputSuggestorMixin {
	private static final int READABLE_INPUT_COLOR = 0xE0E0E0;

	@Shadow
	@Final
	private Screen owner;

	@Shadow
	@Final
	private EditBox textField;

	@Shadow
	@Final
	private boolean chatScreenSized;

	@Shadow
	private List<FormattedCharSequence> messages;

	@Shadow
	private int x;

	@Shadow
	private int width;

	@ModifyExpressionValue(
			method = {"show", "renderMessages"},
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/gui/screen/Screen;height:I"
			)
	)
	private int chat_canvas$anchorSuggestionsToMovedInput(int originalHeight) {
		if (!chatScreenSized) {
			return originalHeight;
		}
		// Both vanilla formulas use (screen height - 12) as the chat field's top edge.
		return textField.getY() + 12;
	}

	@Inject(method = "showCommandSuggestions", at = @At("RETURN"))
	private void chat_canvas$boundFullWidthMessagesToInput(CallbackInfo ci) {
		if (chatScreenSized
				&& !messages.isEmpty()
				&& x == 0
				&& width == owner.width) {
			x = textField.getX();
			width = textField.width();
		}
	}

	@ModifyReturnValue(method = "provideRenderText", at = @At("RETURN"))
	private FormattedCharSequence chat_canvas$useReadableBaseCommandColor(FormattedCharSequence original) {
		Integer gray = ChatFormatting.GRAY.getColorValue();
		if (gray == null) {
			return original;
		}
		return visitor -> original.accept((index, style, codePoint) -> {
			Style renderedStyle = style;
			if (style.getColor() != null && style.getColor().getRgb() == gray) {
				renderedStyle = style.withColor(READABLE_INPUT_COLOR);
			}
			return visitor.accept(index, renderedStyle, codePoint);
		});
	}
}
