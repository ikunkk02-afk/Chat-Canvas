package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.text.ChatCanvasTextFieldRegistry;
import io.github.ikunkk02.chatcanvas.client.SuggestionWindowPlacement;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(CommandSuggestions.class)
public abstract class ChatInputSuggestorMixin {
	private static final int READABLE_INPUT_COLOR = 0xE0E0E0;

	@Shadow @Final
	private EditBox input;

	@Shadow @Final
	private Screen screen;

	@Shadow
	private CommandSuggestions.SuggestionsList suggestions;

	@Inject(method = "showSuggestions", at = @At("RETURN"))
	private void chat_canvas$anchorSuggestionsToMovedInput(
			boolean narrateFirstSuggestion, CallbackInfo ci) {
		if (suggestions == null || !ChatCanvasTextFieldRegistry.isChatField(input)) return;
		Rect2i area = ((SuggestionWindowAccessor) suggestions).chat_canvas$area();
		area.setY(SuggestionWindowPlacement.calculateY(
				input.getY(), input.getHeight(), area.getHeight(), screen.height));
	}

	@ModifyReturnValue(method = "formatText", at = @At("RETURN"))
	private static FormattedCharSequence chat_canvas$useReadableBaseCommandColor(FormattedCharSequence original) {
		int gray = 0xAAAAAA;
		return visitor -> original.accept((index, style, codePoint) -> {
			Style renderedStyle = style;
			if (style.getColor() != null && style.getColor().getValue() == gray) {
				renderedStyle = style.withColor(READABLE_INPUT_COLOR);
			}
			return visitor.accept(index, renderedStyle, codePoint);
		});
	}
}
