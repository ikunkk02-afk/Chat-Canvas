package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.layout.ChatHudTransform;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundMetrics;
import io.github.ikunkk02.chatcanvas.chat.render.ChatBackgroundDraw;
import io.github.ikunkk02.chatcanvas.chat.render.DualChatHudRenderer;
import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerQuickActionMenu;
import io.github.ikunkk02.chatcanvas.chat.command.ui.CommandClipboardPanel;
import io.github.ikunkk02.chatcanvas.chat.text.ChatCanvasTextFieldRegistry;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.chat.layout.RuntimeChatBounds;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
	@Unique
	private final PlayerQuickActionMenu chat_canvas$quickActionMenu =
			new PlayerQuickActionMenu();
	@Unique
	private final CommandClipboardPanel chat_canvas$commandClipboard =
			new CommandClipboardPanel();
	@Unique
	private boolean chat_canvas$commandInput;

	@Shadow
	protected TextFieldWidget chatField;

	@Shadow
	ChatInputSuggestor chatInputSuggestor;

	@Inject(method = "init", at = @At("RETURN"))
	private void chat_canvas$positionChatField(CallbackInfo ci) {
		chat_canvas$commandInput = chat_canvas$isCommandInput();
		chat_canvas$applyInputPlacement();
		ChatCanvasTextFieldRegistry.register(chatField);
		chat_canvas$commandClipboard.init((ChatScreen) (Object) this, chatField);
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void chat_canvas$updateInputChannel(
			DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		boolean commandInput = chat_canvas$isCommandInput();
		if (commandInput != chat_canvas$commandInput) {
			chat_canvas$commandInput = commandInput;
			chat_canvas$applyInputPlacement();
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$handlePlayerNameDoubleClick(
			double mouseX, double mouseY, int button,
			CallbackInfoReturnable<Boolean> cir) {
		if (button == 1 && DualChatHudRenderer.instance().copyCommandAt(mouseX, mouseY)) {
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$commandClipboard.mouseClicked(
				(ChatScreen) (Object) this, chatField, chatInputSuggestor,
				mouseX, mouseY, button)) {
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$quickActionMenu.mouseClicked(
				(ChatScreen) (Object) this, chatField, chatInputSuggestor,
				mouseX, mouseY, button)) {
			cir.setReturnValue(true);
			return;
		}
		if (PlayerNameDoubleClickHandler.instance().mouseClicked(
				(ChatScreen) (Object) this, chatField, chatInputSuggestor,
				mouseX, mouseY, button)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$resetDoubleClickOnScroll(
			double mouseX, double mouseY, double horizontalAmount, double verticalAmount,
			CallbackInfoReturnable<Boolean> cir) {
		PlayerNameDoubleClickHandler.instance().reset();
		if (chat_canvas$commandClipboard.mouseScrolled(verticalAmount)) {
			cir.setReturnValue(true);
			return;
		}
		if (DualChatHudRenderer.instance().scroll(mouseX, mouseY, verticalAmount)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "removed", at = @At("HEAD"))
	private void chat_canvas$resetDoubleClickOnRemoved(CallbackInfo ci) {
		PlayerNameDoubleClickHandler.instance().reset();
		ChatCanvasTextFieldRegistry.unregister(chatField);
		chat_canvas$quickActionMenu.reset((ChatScreen) (Object) this);
		chat_canvas$commandClipboard.removed();
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$handleQuickActionKeys(
			int keyCode, int scanCode, int modifiers,
			CallbackInfoReturnable<Boolean> cir) {
		if (chat_canvas$commandClipboard.keyPressed(
				keyCode, scanCode, modifiers, chatField, chatInputSuggestor)) {
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$quickActionMenu.keyPressed(
				(ChatScreen) (Object) this, chatField, chatInputSuggestor, keyCode)) {
			cir.setReturnValue(true);
		}
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
		chat_canvas$quickActionMenu.render(
				(ChatScreen) (Object) this, context, mouseX, mouseY);
		chat_canvas$commandClipboard.render(
				(ChatScreen) (Object) this, chatField, context, mouseX, mouseY, delta);
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
		RuntimeChatBounds bounds = chat_canvas$inputBounds();
		ChatBackgroundConfig background = chat_canvas$commandInput
				? ChatCanvasConfig.instance().commandSystem().background()
				: ChatCanvasConfig.instance().background();
		int left = bounds.left();
		int top = bounds.inputTop();
		int right = bounds.right();
		int bottom = bounds.inputBottom();
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

	@Unique
	private boolean chat_canvas$isCommandInput() {
		return chatField != null && chatField.getText().startsWith("/");
	}

	@Unique
	private void chat_canvas$applyInputPlacement() {
		RuntimeChatBounds bounds = chat_canvas$inputBounds();
		chatField.setX(bounds.left());
		chatField.setY(bounds.inputTop());
		chatField.setWidth(Math.max(1, bounds.messageWidth()));
		chatInputSuggestor.refresh();
	}

	@Unique
	private RuntimeChatBounds chat_canvas$inputBounds() {
		if (!chat_canvas$commandInput) {
			return ChatLayoutRuntime.currentTransform().bounds();
		}
		var client = net.minecraft.client.MinecraftClient.getInstance();
		PixelLayout layout = ChatCanvasConfig.instance().commandSystem().layout().toPixels(
				client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
		int minimumMessageHeight = Math.max(1, (int) Math.ceil(
				client.textRenderer.fontHeight
						* ChatCanvasConfig.instance().commandSystem().text().fontScale()));
		return RuntimeChatBounds.calculate(
				layout, true, chatField == null ? 12 : chatField.getHeight(),
				RuntimeChatBounds.DEFAULT_INPUT_GAP, minimumMessageHeight);
	}
}
