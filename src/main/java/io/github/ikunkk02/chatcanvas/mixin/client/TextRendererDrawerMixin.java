package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.text.SpacedDrawingContext;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class TextRendererDrawerMixin {
	@Shadow
	float x;

	@Unique
	private int chat_canvas$glyphIndex;

	@Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("RETURN"))
	private void chat_canvas$applyCharacterSpacing(
			int sourceIndex, Style style, int codePoint,
			CallbackInfoReturnable<Boolean> cir) {
		if (!SpacedDrawingContext.active()) return;
		x += (float) SpacedDrawingContext.extraAdvance(chat_canvas$glyphIndex++);
	}
}
