package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.command.ui.CommandToolPanel;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputScreenBridge;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.input.CharInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParentElement.class)
public interface AbstractParentElementMixin {
	@Inject(method = "mouseDragged", at = @At("HEAD"))
	private void chat_canvas$resetDoubleClickOnDrag(
			Click click, double deltaX, double deltaY,
			CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ChatScreen screen) {
			double mouseX = click.x();
			double mouseY = click.y();
			int button = click.button();
			PlayerNameDoubleClickHandler.instance().reset();
			if (CommandToolPanel.dispatchMouseDragged(screen, mouseX, mouseY, button)) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$commandClipboardMouseReleased(
			Click click,
			CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ChatScreen screen
				&& CommandToolPanel.dispatchMouseReleased(screen, click.button())) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$commandClipboardCharTyped(
			CharInput input, CallbackInfoReturnable<Boolean> cir) {
		if (!((Object) this instanceof ChatScreen screen)) return;
		if (!input.isValidChar()) return;
		int codepoint = input.codepoint();
		int modifiers = input.modifiers();
		if (CommandToolPanel.dispatchCharTyped(screen, codepoint, modifiers)) {
			cir.setReturnValue(true);
			return;
		}
		if (screen instanceof ChatCanvasInputScreenBridge bridge
				&& bridge.chat_canvas$dispatchUnicodeChar(codepoint, modifiers)) {
			cir.setReturnValue(true);
		}
	}
}
