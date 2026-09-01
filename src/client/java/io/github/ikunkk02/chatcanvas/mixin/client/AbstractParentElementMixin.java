package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.command.ui.CommandToolPanel;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputScreenBridge;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface AbstractParentElementMixin {
	@Inject(method = "mouseDragged", at = @At("HEAD"))
	private void chat_canvas$resetDoubleClickOnDrag(
			MouseButtonEvent event, double deltaX, double deltaY,
			CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ChatScreen screen) {
			PlayerNameDoubleClickHandler.instance().reset();
			if (CommandToolPanel.dispatchMouseDragged(screen, event.x(), event.y(), event.button())) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$commandClipboardMouseReleased(
			MouseButtonEvent event,
			CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ChatScreen screen
				&& CommandToolPanel.dispatchMouseReleased(screen, event.button())) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$commandClipboardCharTyped(
			CharacterEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!((Object) this instanceof ChatScreen screen)) return;
		if (CommandToolPanel.dispatchCharTyped(screen, (char) event.codepoint(), 0)) {
			cir.setReturnValue(true);
			return;
		}
		if (screen instanceof ChatCanvasInputScreenBridge bridge
				&& bridge.chat_canvas$dispatchUnicodeChar((char) event.codepoint(), 0)) {
			cir.setReturnValue(true);
		}
	}
}
