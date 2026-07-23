package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.layout.ChatHudTransform;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundMetrics;
import io.github.ikunkk02.chatcanvas.chat.render.ChatBackgroundDraw;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

	@WrapOperation(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V",
					ordinal = 0
			)
	)
	private void chat_canvas$drawConfiguredChatFieldBackground(
			DrawContext context, int x1, int y1, int x2, int y2, int color,
			Operation<Void> original) {
		ChatHudTransform transform = ChatLayoutRuntime.currentTransform();
		ChatBackgroundConfig background = ChatCanvasConfig.instance().background();
		int left = transform.bounds().left();
		int top = transform.bounds().inputTop();
		int right = transform.bounds().right();
		int bottom = transform.bounds().inputBottom();
		int backgroundColor = ChatBackgroundMetrics.composeBackgroundColor(
				background.inputColor(), background.inputOpacity(), 1.0);
		if (backgroundColor >>> 24 != 0) {
			context.fill(left, top, right, bottom, backgroundColor);
		}
		if (background.inputBorderEnabled()) {
			int borderColor = ChatBackgroundMetrics.composeBackgroundColor(
					background.inputBorderColor(), background.inputBorderOpacity(), 1.0);
			ChatBackgroundDraw.drawRectBorder(
					context, left, top, right, bottom, borderColor);
		}
	}
}
