package io.github.ikunkk02.chatcanvas.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatHudTransform;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
	@Unique
	private boolean chat_canvas$matrixPushed;
	@Unique
	private boolean chat_canvas$scissorEnabled;

	@Inject(method = "render", at = @At("HEAD"))
	private void chat_canvas$pushLayoutTransform(DrawContext context, int currentTick,
												 int mouseX, int mouseY, boolean focused,
												 CallbackInfo ci) {
		ChatHudTransform transform = ChatLayoutRuntime.currentTransform();
		context.enableScissor(
				0,
				transform.bounds().messageTop(),
				MinecraftClient.getInstance().getWindow().getScaledWidth(),
				transform.bounds().messageBottom()
		);
		chat_canvas$scissorEnabled = true;
		context.getMatrices().push();
		context.getMatrices().translate((float) transform.offsetX(), (float) transform.offsetY(), 0.0f);
		chat_canvas$matrixPushed = true;
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void chat_canvas$popLayoutTransform(DrawContext context, int currentTick,
												int mouseX, int mouseY, boolean focused,
												CallbackInfo ci) {
		if (chat_canvas$matrixPushed) {
			context.getMatrices().pop();
			chat_canvas$matrixPushed = false;
		}
		if (chat_canvas$scissorEnabled) {
			context.disableScissor();
			chat_canvas$scissorEnabled = false;
		}
	}

	@ModifyReturnValue(method = "getWidth", at = @At("RETURN"))
	private int chat_canvas$useConfiguredWidth(int original) {
		return ChatLayoutRuntime.currentTransform().configuredWidth();
	}

	@ModifyReturnValue(method = "getHeight", at = @At("RETURN"))
	private int chat_canvas$useConfiguredHeight(int original) {
		return ChatLayoutRuntime.currentTransform().configuredInternalHeight();
	}

	@ModifyVariable(method = "toChatLineX", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double chat_canvas$screenToChatX(double screenX) {
		return ChatLayoutRuntime.currentTransform().screenToChatX(screenX);
	}

	@ModifyVariable(method = "toChatLineY", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double chat_canvas$screenToChatY(double screenY) {
		return ChatLayoutRuntime.currentTransform().screenToChatY(screenY);
	}

	@ModifyVariable(method = "mouseClicked", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double chat_canvas$queueClickX(double screenX) {
		return ChatLayoutRuntime.currentTransform().screenToChatX(screenX);
	}

	@ModifyVariable(method = "mouseClicked", at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private double chat_canvas$queueClickY(double screenY) {
		return ChatLayoutRuntime.currentTransform().screenToChatY(screenY);
	}
}
