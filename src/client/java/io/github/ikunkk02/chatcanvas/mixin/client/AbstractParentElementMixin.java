package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.command.ui.CommandToolPanel;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputScreenBridge;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screens.ChatScreen;
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
		if ((Object) this instanceof ChatScreen screen) {
			PlayerNameDoubleClickHandler.instance().reset();
			if (CommandToolPanel.dispatchMouseDragged(screen, mouseX, mouseY, button)) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$commandClipboardMouseReleased(
			double mouseX, double mouseY, int button,
			CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ChatScreen screen
				&& CommandToolPanel.dispatchMouseReleased(screen, button)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$commandClipboardCharTyped(
			char chr, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (!((Object) this instanceof ChatScreen screen)) return;
		if (CommandToolPanel.dispatchCharTyped(screen, chr, modifiers)) {
			cir.setReturnValue(true);
			return;
		}
		if (screen instanceof ChatCanvasInputScreenBridge bridge
				&& bridge.chat_canvas$dispatchUnicodeChar(chr, modifiers)) {
			cir.setReturnValue(true);
		}
	}
}
