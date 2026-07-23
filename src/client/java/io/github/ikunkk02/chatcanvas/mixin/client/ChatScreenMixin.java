package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.layout.ChatHudTransform;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundMetrics;
import io.github.ikunkk02.chatcanvas.chat.render.ChatBackgroundDraw;
import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$handlePlayerNameDoubleClick(
			double mouseX, double mouseY, int button,
			CallbackInfoReturnable<Boolean> cir) {
		if (PlayerNameDoubleClickHandler.instance().mouseClicked(
				(ChatScreen) (Object) this, chatField, chatInputSuggestor,
				mouseX, mouseY, button)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"))
	private void chat_canvas$resetDoubleClickOnScroll(
			double mouseX, double mouseY, double horizontalAmount, double verticalAmount,
			CallbackInfoReturnable<Boolean> cir) {
		PlayerNameDoubleClickHandler.instance().reset();
	}

	@Inject(method = "removed", at = @At("HEAD"))
	private void chat_canvas$resetDoubleClickOnRemoved(CallbackInfo ci) {
		PlayerNameDoubleClickHandler.instance().reset();
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void chat_canvas$drawMentionFeedback(
			DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		PlayerNameDoubleClickHandler.instance().feedback().ifPresent(message -> {
			ChatHudTransform transform = ChatLayoutRuntime.currentTransform();
			int x = transform.bounds().left() + 2;
			int y = Math.max(2, transform.bounds().inputTop() - 12);
			context.drawText(
					net.minecraft.client.MinecraftClient.getInstance().textRenderer,
					message, x, y, 0xFFFF858D, true);
		});
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
