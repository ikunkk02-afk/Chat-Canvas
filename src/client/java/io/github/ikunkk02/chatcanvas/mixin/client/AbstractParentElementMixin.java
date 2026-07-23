package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParentElement.class)
public interface AbstractParentElementMixin {
	@Inject(method = "mouseDragged", at = @At("HEAD"))
	private void chat_canvas$resetDoubleClickOnDrag(
			double mouseX, double mouseY, int button, double deltaX, double deltaY,
			CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ChatScreen) {
			PlayerNameDoubleClickHandler.instance().reset();
		}
	}
}
