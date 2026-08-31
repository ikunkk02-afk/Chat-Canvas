package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.command.ui.CommandToolPanel;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputScreenBridge;
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
			net.minecraft.client.gui.Click click, double deltaX, double deltaY,
			CallbackInfoReturnable<Boolean> cir) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		if ((Object) this instanceof ChatScreen screen) {
			PlayerNameDoubleClickHandler.instance().reset();
			if (CommandToolPanel.dispatchMouseDragged(screen, mouseX, mouseY, button)) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$commandClipboardMouseReleased(
			net.minecraft.client.gui.Click click,
			CallbackInfoReturnable<Boolean> cir) {
		int button = click.button();
		if ((Object) this instanceof ChatScreen screen
				&& CommandToolPanel.dispatchMouseReleased(screen, button)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$commandClipboardCharTyped(
			net.minecraft.client.input.CharInput input, CallbackInfoReturnable<Boolean> cir) {
		char chr = (char) input.codepoint();
		int modifiers = input.modifiers();
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
