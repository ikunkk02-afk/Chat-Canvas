package io.github.ikunkk02.chatcanvas.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.ikunkk02.chatcanvas.ChatCanvas;
import io.github.ikunkk02.chatcanvas.chat.command.ui.CommandToolPanel;
import io.github.ikunkk02.chatcanvas.chat.emoji.EmojiPickerPanel;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputController;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputMode;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputScreenBridge;
import io.github.ikunkk02.chatcanvas.chat.input.ChatCanvasInputSender;
import io.github.ikunkk02.chatcanvas.chat.input.ChatInputSnapshot;
import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerNameDoubleClickHandler;
import io.github.ikunkk02.chatcanvas.chat.interaction.PlayerQuickActionMenu;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatBackgroundMetrics;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatHudTransform;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import io.github.ikunkk02.chatcanvas.chat.layout.RuntimeChatBounds;
import io.github.ikunkk02.chatcanvas.chat.render.ChatBackgroundDraw;
import io.github.ikunkk02.chatcanvas.chat.render.DualChatHudRenderer;
import io.github.ikunkk02.chatcanvas.chat.text.ChatCanvasTextFieldRegistry;
import io.github.ikunkk02.chatcanvas.chat.text.UnicodeTextNavigator;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.voice.ChatCanvasVoiceShortcutHost;
import io.github.ikunkk02.chatcanvas.voice.VoiceInputManager;
import io.github.ikunkk02.chatcanvas.voice.VoiceInputOverlay;
import io.github.ikunkk02.chatcanvas.voice.VoiceRecognitionResult;
import io.github.ikunkk02.chatcanvas.voice.VoiceTextSanitizer;
import io.github.ikunkk02.chatcanvas.voice.VoiceEncodingDiagnostics;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessageIngress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin implements ChatCanvasInputScreenBridge, ChatCanvasVoiceShortcutHost {
	@Unique
	private final PlayerQuickActionMenu chat_canvas$quickActionMenu =
			new PlayerQuickActionMenu();
	@Unique
	private final CommandToolPanel chat_canvas$commandTools =
			new CommandToolPanel();
	@Unique
	private final EmojiPickerPanel chat_canvas$emojiPicker =
			new EmojiPickerPanel();
	@Unique
	private final VoiceInputOverlay chat_canvas$voiceOverlay =
			new VoiceInputOverlay();
	@Unique
	private final ChatCanvasInputController chat_canvas$inputController =
			ChatCanvasInputController.instance();
	@Unique
	private TextFieldWidget chat_canvas$playerChatField;
	@Unique
	private ChatInputSuggestor chat_canvas$playerChatSuggestor;
	@Unique
	private ChatCanvasInputMode chat_canvas$inputMode =
			ChatCanvasInputMode.PLAYER_CHAT;
	@Unique
	private boolean chat_canvas$suppressInputUpdates;
	@Unique
	private boolean chat_canvas$inputInitialized;
	@Unique
	private boolean chat_canvas$inputHealthy;
	@Unique
	private char chat_canvas$pendingHighSurrogate;
	@Unique
	private boolean chat_canvas$suppressNextVoiceCharacter;
	@Unique
	private int chat_canvas$suppressedVoiceKeyCode;
	@Unique
	private long chat_canvas$suppressVoiceCharacterUntil;

	@Shadow
	protected TextFieldWidget chatField;

	@Shadow
	ChatInputSuggestor chatInputSuggestor;

	@Inject(method = "init", at = @At("RETURN"))
	private void chat_canvas$initializeIndependentInputs(CallbackInfo ci) {
		if (!ChatCanvasConfig.instance().enabled()) return;
		try {
			ChatScreen screen = (ChatScreen) (Object) this;
			MinecraftClient client = MinecraftClient.getInstance();
			if (!chat_canvas$inputInitialized) {
				ChatCanvasInputMode openingMode = chatField.getText().startsWith("/")
						? ChatCanvasInputMode.COMMAND
						: ChatCanvasInputMode.PLAYER_CHAT;
				chat_canvas$inputController.open(openingMode, chatField.getText());
			}
			chat_canvas$inputMode = chat_canvas$inputController.currentMode();

			chat_canvas$playerChatField = new TextFieldWidget(
					client.advanceValidatingTextRenderer,
					4, screen.height - 12, Math.max(1, screen.width - 4), 12,
					Text.translatable("chat_canvas.input.player.field"));
			chat_canvas$playerChatField.setMaxLength(256);
			chat_canvas$playerChatField.setDrawsBackground(false);
			chat_canvas$playerChatField.setFocusUnlocked(false);
			chat_canvas$playerChatField.setChangedListener(
					this::chat_canvas$onPlayerInputChanged);
			((ScreenAccessor) (Object) this)
					.chat_canvas$addSelectableChild(chat_canvas$playerChatField);

			chat_canvas$playerChatSuggestor = new ChatInputSuggestor(
					client, screen, chat_canvas$playerChatField, client.textRenderer,
					false, false, 1, 10, true, -805306368);
			chat_canvas$playerChatSuggestor.setCanLeave(false);
			chatField.setChangedListener(this::chat_canvas$onCommandInputChanged);

			ChatCanvasTextFieldRegistry.register(
					chat_canvas$playerChatField, ChatCanvasInputMode.PLAYER_CHAT);
			ChatCanvasTextFieldRegistry.register(
					chatField, ChatCanvasInputMode.COMMAND);
			chat_canvas$restoreField(
					chat_canvas$playerChatField,
					chat_canvas$inputController.snapshot(ChatCanvasInputMode.PLAYER_CHAT));
			chat_canvas$restoreField(
					chatField,
					chat_canvas$inputController.snapshot(ChatCanvasInputMode.COMMAND));
			chat_canvas$applyInputMode(chat_canvas$inputMode);
			chat_canvas$commandTools.init(screen, chatField);
			chat_canvas$emojiPicker.init(
					screen, chat_canvas$playerChatField,
					chat_canvas$playerChatSuggestor);
			chat_canvas$voiceOverlay.init(
					screen, chat_canvas$playerChatField,
					this::chat_canvas$insertVoiceResult);
			chat_canvas$inputInitialized = true;
			chat_canvas$inputHealthy = true;
		} catch (Throwable throwable) {
			chat_canvas$inputHealthy = false;
			if (chat_canvas$playerChatField != null) {
				chat_canvas$playerChatField.setVisible(false);
				chat_canvas$playerChatField.setFocused(false);
			}
			chatField.setVisible(true);
			ChatCanvas.LOGGER.error(
					"Chat Canvas independent input initialization failed; using vanilla ChatScreen",
					throwable);
		}
	}

	@Inject(method = "setInitialFocus", at = @At("RETURN"))
	private void chat_canvas$focusActiveInput(CallbackInfo ci) {
		if (chat_canvas$inputHealthy) chat_canvas$focusActiveField();
	}

	@Inject(method = "resize", at = @At("HEAD"))
	private void chat_canvas$captureBeforeResize(
			MinecraftClient client, int width, int height, CallbackInfo ci) {
		if (!chat_canvas$inputHealthy) return;
		chat_canvas$captureBothFields();
		chat_canvas$unregisterFields();
	}

	@Inject(method = "render", at = @At("HEAD"))
	private void chat_canvas$keepInputPlacementCurrent(
			DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (chat_canvas$inputHealthy) chat_canvas$applyInputPlacement();
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$handleCustomMouseInput(
			net.minecraft.client.gui.Click click, boolean doubled,
			CallbackInfoReturnable<Boolean> cir) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		if (!chat_canvas$inputHealthy) return;
		ChatScreen screen = (ChatScreen) (Object) this;
		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT
				&& DualChatHudRenderer.instance().copyCommandAt(mouseX, mouseY)) {
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT
				&& chat_canvas$voiceOverlay.mouseClicked(mouseX, mouseY, button)) {
			chat_canvas$emojiPicker.close();
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT
				&& chat_canvas$emojiPicker.mouseClicked(mouseX, mouseY, button)) {
			if (VoiceInputManager.instance().isBusy()) chat_canvas$voiceOverlay.cancel();
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$inputMode == ChatCanvasInputMode.COMMAND
				&& chat_canvas$commandTools.mouseClicked(
						screen, chatField, chatInputSuggestor,
						mouseX, mouseY, button)) {
			cir.setReturnValue(true);
			return;
		}
		ChatInputSuggestor activeSuggestor = chat_canvas$activeInputSuggestor();
		if (chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT
				&& activeSuggestor.mouseClicked(new net.minecraft.client.gui.Click(mouseX, mouseY, new net.minecraft.client.input.MouseInput(button, 0)))) {
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$quickActionMenu.mouseClicked(
				screen, chat_canvas$activeInputField(), activeSuggestor,
				mouseX, mouseY, button)) {
			cir.setReturnValue(true);
			return;
		}
		if (PlayerNameDoubleClickHandler.instance().mouseClicked(
				screen, chat_canvas$playerChatField, chat_canvas$playerChatSuggestor,
				mouseX, mouseY, button)) {
			chat_canvas$openPlayerInput();
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$routeScrollToActiveInput(
			double mouseX, double mouseY, double horizontalAmount, double verticalAmount,
			CallbackInfoReturnable<Boolean> cir) {
		if (!chat_canvas$inputHealthy) return;
		PlayerNameDoubleClickHandler.instance().reset();
		if (chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT
				&& chat_canvas$emojiPicker.mouseScrolled(
						mouseX, mouseY, horizontalAmount, verticalAmount)) {
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$inputMode == ChatCanvasInputMode.COMMAND
				&& chat_canvas$commandTools.mouseScrolled(verticalAmount)) {
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$activeInputSuggestor().mouseScrolled(verticalAmount)) {
			cir.setReturnValue(true);
			return;
		}
		if (DualChatHudRenderer.instance().scroll(mouseX, mouseY, verticalAmount)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "removed", at = @At("HEAD"))
	private void chat_canvas$saveDraftsOnRemoved(CallbackInfo ci) {
		PlayerNameDoubleClickHandler.instance().reset();
		if (chat_canvas$inputHealthy) chat_canvas$captureBothFields();
		chat_canvas$unregisterFields();
		chat_canvas$quickActionMenu.reset((ChatScreen) (Object) this);
		chat_canvas$commandTools.removed();
		chat_canvas$emojiPicker.dispose();
		chat_canvas$voiceOverlay.dispose();
		chat_canvas$pendingHighSurrogate = 0;
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$routeKeysByInputMode(
			net.minecraft.client.input.KeyInput input,
			CallbackInfoReturnable<Boolean> cir) {
		int keyCode = input.key();
		int scanCode = input.scancode();
		int modifiers = input.modifiers();
		if (!chat_canvas$inputHealthy) return;
		ChatScreen screen = (ChatScreen) (Object) this;
		TextFieldWidget activeField = chat_canvas$activeInputField();
		ChatInputSuggestor activeSuggestor = chat_canvas$activeInputSuggestor();
		if (keyCode == GLFW.GLFW_KEY_ESCAPE && VoiceInputManager.instance().isBusy()) {
			chat_canvas$voiceOverlay.cancel();
		}
		if (chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT
				&& io.github.ikunkk02.chatcanvas.ChatCanvasClient.voiceInputKey()
						.matchesKey(new net.minecraft.client.input.KeyInput(keyCode, scanCode, 0))) {
			chat_canvas$emojiPicker.close();
			chat_canvas$voiceOverlay.keyboardPressed();
			chat_canvas$suppressNextVoiceCharacter = true;
			chat_canvas$suppressedVoiceKeyCode = keyCode;
			chat_canvas$suppressVoiceCharacterUntil = System.currentTimeMillis() + 300;
			cir.setReturnValue(true);
			return;
		}

		if (chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT
				&& chat_canvas$emojiPicker.keyPressed(
						keyCode, scanCode, modifiers)) {
			if (VoiceInputManager.instance().isBusy()) chat_canvas$voiceOverlay.cancel();
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$inputMode == ChatCanvasInputMode.COMMAND
				&& chat_canvas$commandTools.keyPressed(
						keyCode, scanCode, modifiers, chatField, chatInputSuggestor)) {
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$quickActionMenu.keyPressed(
				screen, activeField, activeSuggestor, keyCode)) {
			cir.setReturnValue(true);
			return;
		}
		if (chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT
				&& activeSuggestor.keyPressed(new net.minecraft.client.input.KeyInput(keyCode, scanCode, modifiers))) {
			cir.setReturnValue(true);
			return;
		}
		if ((keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN)
				&& chat_canvas$inputMode == ChatCanvasInputMode.COMMAND
				&& activeSuggestor.keyPressed(new net.minecraft.client.input.KeyInput(keyCode, scanCode, modifiers))) {
			cir.setReturnValue(true);
			return;
		}
		if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
			ChatInputSnapshot next = chat_canvas$inputController.navigateHistory(
					chat_canvas$inputMode,
					keyCode == GLFW.GLFW_KEY_UP ? -1 : 1,
					chat_canvas$snapshot(activeField));
			chat_canvas$restoreField(activeField, next);
			activeSuggestor.setWindowActive(false);
			activeSuggestor.refresh();
			cir.setReturnValue(true);
			return;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			chat_canvas$submitActiveInput(screen);
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "insertText", at = @At("HEAD"), cancellable = true)
	private void chat_canvas$insertIntoActiveField(
			String text, boolean override, CallbackInfo ci) {
		if (!chat_canvas$inputHealthy) return;
		TextFieldWidget field = chat_canvas$activeInputField();
		if (override) field.setText(text);
		else field.write(text);
		ci.cancel();
	}

	@Override
	public void chat_canvas$voiceTick() {
		if (!chat_canvas$inputHealthy
				|| chat_canvas$inputMode != ChatCanvasInputMode.PLAYER_CHAT) {
			if (VoiceInputManager.instance().isBusy()) chat_canvas$voiceOverlay.cancel();
			return;
		}
		if (!MinecraftClient.getInstance().isWindowFocused()
				&& VoiceInputManager.instance().isBusy()) {
			chat_canvas$voiceOverlay.cancel();
			return;
		}
		chat_canvas$voiceOverlay.tick();
	}

	@Override
	public void chat_canvas$onVoiceShortcutReleased(int keyCode, int scanCode) {
		if (!chat_canvas$inputHealthy) {
			return;
		}
		if (chat_canvas$inputMode != ChatCanvasInputMode.PLAYER_CHAT) {
			return;
		}
		if (chat_canvas$voiceOverlay == null) {
			return;
		}
		KeyBinding voiceKey = io.github.ikunkk02.chatcanvas.ChatCanvasClient.voiceInputKey();
		if (voiceKey == null) {
			return;
		}
		if (!voiceKey.matchesKey(new net.minecraft.client.input.KeyInput(keyCode, scanCode, 0))) {
			return;
		}
		chat_canvas$voiceOverlay.keyboardReleased();
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void chat_canvas$renderIndependentInput(
			DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (!chat_canvas$inputHealthy) return;
		if (chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT) {
			chat_canvas$playerChatField.render(context, mouseX, mouseY, delta);
		}
		TextFieldWidget activeField = chat_canvas$activeInputField();
		context.drawTextWithShadow(
				MinecraftClient.getInstance().textRenderer,
				Text.translatable(chat_canvas$inputMode == ChatCanvasInputMode.COMMAND
						? "chat_canvas.input.mode.command"
						: "chat_canvas.input.mode.player"),
				activeField.getX(), Math.max(2, activeField.getY() - 10), 0xFFE7ECF5);
		if (chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT) {
			context.getMatrices().pushMatrix();
			context.getMatrices().translate(0.0f, 0.0f);
			chat_canvas$playerChatSuggestor.render(context, mouseX, mouseY);
			context.getMatrices().popMatrix();
		}
		PlayerNameDoubleClickHandler.instance().feedback().ifPresent(message -> {
			ChatHudTransform transform = ChatLayoutRuntime.currentTransform();
			int x = transform.bounds().left() + 2;
			int y = Math.max(2, transform.bounds().inputTop() - 12);
			context.drawText(
					MinecraftClient.getInstance().textRenderer,
					message, x, y, 0xFFFF858D, true);
		});
		chat_canvas$quickActionMenu.render(
				(ChatScreen) (Object) this, context, mouseX, mouseY);
		if (chat_canvas$inputMode == ChatCanvasInputMode.COMMAND) {
			chat_canvas$commandTools.render(
					(ChatScreen) (Object) this, chatField,
					context, mouseX, mouseY, delta);
		} else {
			chat_canvas$emojiPicker.render(context, mouseX, mouseY, delta);
			chat_canvas$voiceOverlay.render(context, mouseX, mouseY, delta);
		}
		chat_canvas$renderInputLength(context, activeField);
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
		if (!chat_canvas$inputHealthy) {
			original.call(context, x1, y1, x2, y2, color);
			return;
		}
		RuntimeChatBounds bounds = chat_canvas$inputBounds(chat_canvas$inputMode);
		ChatBackgroundConfig background =
				chat_canvas$inputMode == ChatCanvasInputMode.COMMAND
						? ChatCanvasConfig.instance().commandSystem().background()
						: ChatCanvasConfig.instance().background();
		int backgroundColor = ChatBackgroundMetrics.composeBackgroundColor(
				background.inputColor(), background.inputOpacity(), 1.0);
		if (backgroundColor >>> 24 != 0) {
			context.fill(
					bounds.left(), bounds.inputTop(),
					bounds.right(), bounds.inputBottom(), backgroundColor);
		}
		if (background.inputBorderEnabled()) {
			int borderColor = ChatBackgroundMetrics.composeBackgroundColor(
					background.inputBorderColor(),
					background.inputBorderOpacity(), 1.0);
			ChatBackgroundDraw.drawRectBorder(
					context, bounds.left(), bounds.inputTop(),
					bounds.right(), bounds.inputBottom(), borderColor);
		}
	}

	@Override
	public ChatCanvasInputMode chat_canvas$inputMode() {
		return chat_canvas$inputMode;
	}

	@Override
	public TextFieldWidget chat_canvas$activeInputField() {
		return chat_canvas$inputMode == ChatCanvasInputMode.COMMAND
				? chatField : chat_canvas$playerChatField;
	}

	@Override
	public ChatInputSuggestor chat_canvas$activeInputSuggestor() {
		return chat_canvas$inputMode == ChatCanvasInputMode.COMMAND
				? chatInputSuggestor : chat_canvas$playerChatSuggestor;
	}

	@Override
	public void chat_canvas$openPlayerInput() {
		if (!chat_canvas$inputHealthy
				|| chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT) return;
		chat_canvas$inputController.capture(
				ChatCanvasInputMode.COMMAND, chat_canvas$snapshot(chatField));
		chat_canvas$inputController.switchToPlayerChat();
		chat_canvas$applyInputMode(ChatCanvasInputMode.PLAYER_CHAT);
	}

	@Override
	public boolean chat_canvas$dispatchUnicodeChar(char character, int modifiers) {
		if (!chat_canvas$inputHealthy) return false;
		if (chat_canvas$suppressNextVoiceCharacter
				&& System.currentTimeMillis() < chat_canvas$suppressVoiceCharacterUntil) {
			// Only suppress the character corresponding to the currently bound voice key
			int keyCodeForChar = chat_canvas$suppressedVoiceKeyCode;
			char expectedChar = 0;
			if (keyCodeForChar >= GLFW.GLFW_KEY_A && keyCodeForChar <= GLFW.GLFW_KEY_Z) {
				expectedChar = (char) ('a' + (keyCodeForChar - GLFW.GLFW_KEY_A));
				if (Character.toLowerCase(character) == expectedChar) {
					chat_canvas$suppressNextVoiceCharacter = false;
					return true;
				}
			}
			// If the character doesn't match, clear suppression so normal typing works
			chat_canvas$suppressNextVoiceCharacter = false;
		}
		if (Character.isHighSurrogate(character)) {
			chat_canvas$pendingHighSurrogate = character;
			return true;
		}
		if (Character.isLowSurrogate(character)) {
			if (chat_canvas$pendingHighSurrogate == 0) return true;
			String pair = new String(new char[]{
					chat_canvas$pendingHighSurrogate, character});
			chat_canvas$pendingHighSurrogate = 0;
			if (chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT
					&& chat_canvas$emojiPicker.writeComposedText(pair)) return true;
			chat_canvas$activeInputField().write(pair);
			return true;
		}
		chat_canvas$pendingHighSurrogate = 0;
		return chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT
				&& chat_canvas$emojiPicker.charTyped(character, modifiers);
	}

	@Unique
	private void chat_canvas$onPlayerInputChanged(String text) {
		if (chat_canvas$suppressInputUpdates) return;
		ChatInputSnapshot changed = chat_canvas$snapshot(chat_canvas$playerChatField);
		if (chat_canvas$inputMode == ChatCanvasInputMode.PLAYER_CHAT
				&& text.startsWith("/")) {
			ChatInputSnapshot playerDraft =
					chat_canvas$inputController.snapshot(ChatCanvasInputMode.PLAYER_CHAT);
			chat_canvas$inputController.switchPlayerTextToCommand(changed);
			chat_canvas$emojiPicker.close();
			chat_canvas$pendingHighSurrogate = 0;
			chat_canvas$restoreField(chat_canvas$playerChatField, playerDraft);
			chat_canvas$restoreField(
					chatField,
					chat_canvas$inputController.snapshot(ChatCanvasInputMode.COMMAND));
			chat_canvas$applyInputMode(ChatCanvasInputMode.COMMAND);
			return;
		}
		chat_canvas$inputController.capture(ChatCanvasInputMode.PLAYER_CHAT, changed);
		chat_canvas$playerChatSuggestor.setWindowActive(!text.isEmpty());
		chat_canvas$playerChatSuggestor.refresh();
	}

	@Unique
	private void chat_canvas$onCommandInputChanged(String text) {
		if (chat_canvas$suppressInputUpdates) return;
		chat_canvas$inputController.capture(
				ChatCanvasInputMode.COMMAND, chat_canvas$snapshot(chatField));
		chatInputSuggestor.setWindowActive(
				chat_canvas$inputMode == ChatCanvasInputMode.COMMAND);
		chatInputSuggestor.refresh();
	}

	@Unique
	private void chat_canvas$applyInputMode(ChatCanvasInputMode mode) {
		chat_canvas$inputMode = mode;
		boolean command = mode == ChatCanvasInputMode.COMMAND;
		chatField.setVisible(command);
		chat_canvas$playerChatField.setVisible(!command);
		chatField.setFocused(false);
		chat_canvas$playerChatField.setFocused(false);
		chatInputSuggestor.setWindowActive(command);
		chat_canvas$playerChatSuggestor.setWindowActive(!command);
		if (!command) chatInputSuggestor.clearWindow();
		else chat_canvas$playerChatSuggestor.clearWindow();
		if (!command) chat_canvas$commandTools.close();
		if (command) {
			chat_canvas$emojiPicker.close();
			chat_canvas$voiceOverlay.cancel();
			chat_canvas$pendingHighSurrogate = 0;
		}
		chat_canvas$applyInputPlacement();
		chat_canvas$focusActiveField();
		chat_canvas$activeInputSuggestor().refresh();
	}

	@Unique
	private void chat_canvas$focusActiveField() {
		TextFieldWidget active = chat_canvas$activeInputField();
		if (active == null) return;
		((ParentElement) (Object) this).setFocused(active);
		active.setFocused(true);
	}

	@Unique
	private void chat_canvas$applyInputPlacement() {
		chat_canvas$placeField(
				chat_canvas$playerChatField,
				chat_canvas$inputBounds(ChatCanvasInputMode.PLAYER_CHAT),
				EmojiPickerPanel.BUTTON_SPACE + VoiceInputOverlay.BUTTON_SPACE);
		chat_canvas$placeField(
				chatField,
				chat_canvas$inputBounds(ChatCanvasInputMode.COMMAND), 0);
	}

	@Unique
	private static void chat_canvas$placeField(
			TextFieldWidget field, RuntimeChatBounds bounds, int reservedRight) {
		if (field == null) return;
		field.setX(bounds.left());
		field.setY(bounds.inputTop());
		field.setWidth(Math.max(1, bounds.messageWidth() - Math.max(0, reservedRight)));
	}

	@Unique
	private RuntimeChatBounds chat_canvas$inputBounds(ChatCanvasInputMode mode) {
		MinecraftClient client = MinecraftClient.getInstance();
		PixelLayout layout = mode == ChatCanvasInputMode.COMMAND
				? ChatCanvasConfig.instance().commandSystem().layout().toPixels(
						client.getWindow().getScaledWidth(),
						client.getWindow().getScaledHeight())
				: ChatCanvasConfig.instance().layout().toPixels(
						client.getWindow().getScaledWidth(),
						client.getWindow().getScaledHeight());
		double fontScale = mode == ChatCanvasInputMode.COMMAND
				? ChatCanvasConfig.instance().commandSystem().text().fontScale()
				: ChatCanvasConfig.instance().text().fontScale();
		TextFieldWidget field = mode == ChatCanvasInputMode.COMMAND
				? chatField : chat_canvas$playerChatField;
		int minimumMessageHeight = Math.max(1, (int) Math.ceil(
				client.textRenderer.fontHeight * fontScale));
		return RuntimeChatBounds.calculate(
				layout, true, field == null ? 12 : field.getHeight(),
				RuntimeChatBounds.DEFAULT_INPUT_GAP, minimumMessageHeight);
	}

	@Unique
	private void chat_canvas$submitActiveInput(ChatScreen screen) {
		TextFieldWidget active = chat_canvas$activeInputField();
		if (chat_canvas$inputMode == ChatCanvasInputMode.COMMAND) {
			ChatCanvasInputSender.executeCommand(
					MinecraftClient.getInstance(), screen, active.getText());
		} else {
			ChatCanvasInputSender.sendPlayerChat(
					MinecraftClient.getInstance(), screen, active.getText());
		}
		chat_canvas$restoreField(
				active, chat_canvas$inputController.snapshot(chat_canvas$inputMode));
		MinecraftClient.getInstance().setScreen(null);
	}

	@Unique
	private void chat_canvas$captureBothFields() {
		if (chat_canvas$playerChatField != null) {
			chat_canvas$inputController.capture(
					ChatCanvasInputMode.PLAYER_CHAT,
					chat_canvas$snapshot(chat_canvas$playerChatField));
		}
		if (chatField != null) {
			chat_canvas$inputController.capture(
					ChatCanvasInputMode.COMMAND,
					chat_canvas$snapshot(chatField));
		}
	}

	@Unique
	private void chat_canvas$unregisterFields() {
		ChatCanvasTextFieldRegistry.unregister(chat_canvas$playerChatField);
		ChatCanvasTextFieldRegistry.unregister(chatField);
	}

	@Unique
	private ChatInputSnapshot chat_canvas$snapshot(TextFieldWidget field) {
		if (field == null) return ChatInputSnapshot.EMPTY;
		TextFieldWidgetAccessor accessor = (TextFieldWidgetAccessor) field;
		return new ChatInputSnapshot(
				field.getText(), field.getCursor(),
				accessor.chat_canvas$selectionEnd());
	}

	@Unique
	private void chat_canvas$restoreField(
			TextFieldWidget field, ChatInputSnapshot snapshot) {
		if (field == null || snapshot == null) return;
		boolean previousSuppression = chat_canvas$suppressInputUpdates;
		chat_canvas$suppressInputUpdates = true;
		try {
			field.setText(snapshot.text());
			field.setSelectionStart(snapshot.cursor());
			field.setSelectionEnd(snapshot.selectionEnd());
		} finally {
			chat_canvas$suppressInputUpdates = previousSuppression;
		}
	}

	@Unique
	private void chat_canvas$renderInputLength(
			DrawContext context, TextFieldWidget activeField) {
		if (chat_canvas$inputMode != ChatCanvasInputMode.PLAYER_CHAT
				|| activeField == null) return;
		String text = activeField.getText();
		int graphemes = UnicodeTextNavigator.graphemeCount(text);
		Text counter = Text.translatable(
				"chat_canvas.emoji.length", graphemes, text.length(), 256);
		int color = text.length() >= 256 ? 0xFFFF6B6B
				: text.length() >= 230 ? 0xFFF6C85F : 0xFFADB6C7;
		int width = MinecraftClient.getInstance().textRenderer.getWidth(counter);
		int modeWidth = MinecraftClient.getInstance().textRenderer.getWidth(
				Text.translatable("chat_canvas.input.mode.player"));
		int counterY = activeField.getWidth() < width + modeWidth + 8
				? Math.max(2, activeField.getY() - 20)
				: Math.max(2, activeField.getY() - 10);
		context.drawTextWithShadow(
				MinecraftClient.getInstance().textRenderer, counter,
				Math.max(activeField.getX(),
						activeField.getX() + activeField.getWidth() - width),
				counterY, color);
	}

	@Unique
	private void chat_canvas$insertVoiceResult(VoiceRecognitionResult recognition) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.currentScreen != (Object) this
				|| chat_canvas$inputMode != ChatCanvasInputMode.PLAYER_CHAT
				|| chat_canvas$playerChatField == null) return;
		String value = VoiceTextSanitizer.sanitize(
				recognition.text(),
				VoiceInputManager.instance().settings().addFinalPunctuation());
		if (VoiceEncodingDiagnostics.enabled()) {
			ChatCanvas.LOGGER.debug("Voice text before UI insertion: {}", value);
			ChatCanvas.LOGGER.debug("Voice text before UI insertion code points: {}",
					VoiceEncodingDiagnostics.describeCodePoints(value));
		}
		if (value.isEmpty()) {
			ChatCanvasMessageIngress.instance().reportError(
					Text.translatable("chat_canvas.voice.error.empty"), null);
			return;
		}
		ChatInputSnapshot current = chat_canvas$snapshot(chat_canvas$playerChatField);
		chat_canvas$inputController.capture(ChatCanvasInputMode.PLAYER_CHAT, current);
		UnicodeTextNavigator.EditResult result =
				chat_canvas$inputController.insertPlayerText(value, 256);
		if (result.limitExceeded()) {
			ChatCanvasMessageIngress.instance().reportError(
					Text.translatable("chat_canvas.voice.error.too_long"), null);
			return;
		}
		chat_canvas$restoreField(chat_canvas$playerChatField,
				new ChatInputSnapshot(result.text(), result.cursor(), result.selectionEnd()));
		chat_canvas$inputController.capture(
				ChatCanvasInputMode.PLAYER_CHAT,
				new ChatInputSnapshot(result.text(), result.cursor(), result.selectionEnd()));
		chat_canvas$playerChatSuggestor.setWindowActive(!result.text().isEmpty());
		chat_canvas$playerChatSuggestor.refresh();
		chat_canvas$focusActiveField();
	}
}
