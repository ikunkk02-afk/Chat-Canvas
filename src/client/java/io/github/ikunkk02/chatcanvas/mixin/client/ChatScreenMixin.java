package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.layout.ChatHudTransform;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
	@Shadow
	protected TextFieldWidget chatField;

	@Shadow
	ChatInputSuggestor chatInputSuggestor;

	@Inject(method = "init", at = @At("RETURN"))
	private void chat_canvas$positionChatField(CallbackInfo ci) {
		ChatHudTransform transform = ChatLayoutRuntime.currentTransform();
		chatField.setX(transform.bounds().left());
		chatField.setY(transform.bounds().inputTop());
		chatField.setWidth(Math.max(1, transform.bounds().messageWidth()));
		chatInputSuggestor.refresh();
	}

	@ModifyArgs(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V",
					ordinal = 0
			)
	)
	private void chat_canvas$positionChatFieldBackground(Args args) {
		ChatHudTransform transform = ChatLayoutRuntime.currentTransform();
		args.set(0, transform.bounds().left());
		args.set(1, transform.bounds().inputTop());
		args.set(2, transform.bounds().right());
		args.set(3, transform.bounds().inputBottom());
	}
}
