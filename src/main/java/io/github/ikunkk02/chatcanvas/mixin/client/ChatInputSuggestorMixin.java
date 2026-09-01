package io.github.ikunkk02.chatcanvas.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

@Mixin(CommandSuggestions.class)
public abstract class ChatInputSuggestorMixin {
	private static final int READABLE_INPUT_COLOR = 0xE0E0E0;

	@Shadow
	@Final
	private Screen screen;

	@Shadow
	@Final
	private EditBox input;

	@Shadow
	@Final
	private boolean anchorToBottom;

	@Shadow
	private List<FormattedCharSequence> commandUsage;

	@Shadow
	private int commandUsagePosition;

	@Shadow
	private int commandUsageWidth;

	@ModifyExpressionValue(
			method = {"showSuggestions", "renderUsage"},
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/gui/screens/Screen;height:I"
			)
	)
	private int chat_canvas$anchorSuggestionsToMovedInput(int originalHeight) {
		if (!anchorToBottom) {
			return originalHeight;
		}
		// Both vanilla formulas use (screen height - 12) as the chat field's top edge.
		return input.getY() + 12;
	}

	@Inject(method = "updateUsageInfo", at = @At("RETURN"))
	private void chat_canvas$boundFullWidthMessagesToInput(CallbackInfo ci) {
		if (anchorToBottom
				&& !commandUsage.isEmpty()
				&& commandUsagePosition == 0
				&& commandUsageWidth == screen.width) {
			commandUsagePosition = input.getX();
			commandUsageWidth = input.getWidth();
		}
	}

	@ModifyReturnValue(method = "formatChat", at = @At("RETURN"))
	private FormattedCharSequence chat_canvas$useReadableBaseCommandColor(FormattedCharSequence original) {
		Integer gray = ChatFormatting.GRAY.getColor();
		if (gray == null) {
			return original;
		}
		return visitor -> original.accept((index, style, codePoint) -> {
			Style renderedStyle = style;
			if (style.getColor() != null && style.getColor().getValue() == gray) {
				renderedStyle = style.withColor(READABLE_INPUT_COLOR);
			}
			return visitor.accept(index, renderedStyle, codePoint);
		});
	}
}
