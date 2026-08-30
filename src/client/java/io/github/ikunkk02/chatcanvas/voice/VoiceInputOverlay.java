package io.github.ikunkk02.chatcanvas.voice;

import io.github.ikunkk02.chatcanvas.ui.ModernUiTheme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public final class VoiceInputOverlay {
	public static final int BUTTON_SPACE = 20;
	private static final int BUTTON_WIDTH = 18;
	private static final int BUTTON_HEIGHT = 14;
	private final VoiceInputManager manager = VoiceInputManager.instance();
	private ChatScreen owner;
	private TextFieldWidget field;
	private Consumer<VoiceRecognitionResult> resultConsumer;
	private int buttonX;
	private int buttonY;
	private boolean mouseHolding;
	private boolean keyboardHolding;
	private boolean installPrompt;

	public void init(ChatScreen screen, TextFieldWidget playerField,
					 Consumer<VoiceRecognitionResult> consumer) {
		owner = screen;
		field = playerField;
		resultConsumer = consumer;
		mouseHolding = false;
		keyboardHolding = false;
		installPrompt = false;
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (installPrompt) return promptClick(mouseX, mouseY, button);
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT
				|| !hit(mouseX, mouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
			return false;
		}
		if (manager.state() == VoiceInputState.MODEL_MISSING) {
			installPrompt = true;
			return true;
		}
		if (manager.isListening()) {
			manager.cancel();
			mouseHolding = false;
			return true;
		}
		mouseHolding = manager.begin(resultConsumer);
		return true;
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !mouseHolding) return false;
		mouseHolding = false;
		manager.finish();
		return true;
	}

	public void keyboardPressed() {
		if (manager.state() == VoiceInputState.MODEL_MISSING) {
			installPrompt = true;
			return;
		}
		if (keyboardHolding) {
			return;
		}
		if (manager.state() == VoiceInputState.RECOGNIZING) {
			manager.cancel();
		}
		boolean started = manager.begin(resultConsumer);
		if (started) {
			keyboardHolding = true;
		}
	}

	public void tick() {
		if (owner == null) return;
		long window = MinecraftClient.getInstance().getWindow().getHandle();
		if (mouseHolding
				&& GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT)
				== GLFW.GLFW_RELEASE) {
			mouseHolding = false;
			manager.finish();
		}
		if (keyboardHolding && !MinecraftClient.getInstance().isWindowFocused()) {
			cancel();
		}
	}

	public void keyboardReleased() {
		if (!keyboardHolding) {
			return;
		}
		keyboardHolding = false;
		var st = manager.state();
		if (st == VoiceInputState.LISTENING || st == VoiceInputState.MODEL_LOADING) {
			manager.finish();
		}
	}

	public void cancel() {
		mouseHolding = false;
		keyboardHolding = false;
		manager.cancel();
	}

	public void dispose() {
		cancel();
		owner = null;
		field = null;
		resultConsumer = null;
		installPrompt = false;
	}

	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (owner == null || field == null) return;
		buttonX = field.getX() + field.getWidth() + EmojiOffset.TOTAL_SPACE + 1;
		buttonY = field.getY() - 1;
		renderButton(context, mouseX, mouseY);
		renderStatus(context);
		if (installPrompt) renderPrompt(context, mouseX, mouseY);
	}

	private void renderButton(DrawContext context, int mouseX, int mouseY) {
		VoiceInputState state = manager.state();
		boolean hovered = hit(mouseX, mouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
		ModernUiTheme.drawFixedControl(context, buttonX, buttonY,
				BUTTON_WIDTH, BUTTON_HEIGHT, hovered,
				state == VoiceInputState.RECOGNIZING, true);
		if (state == VoiceInputState.LISTENING) {
			context.drawBorder(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
					ModernUiTheme.DANGER);
		} else if (state == VoiceInputState.MODEL_MISSING || state == VoiceInputState.ERROR) {
			context.drawBorder(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
					ModernUiTheme.WARNING);
		}
		int cx = buttonX + 9;
		context.fill(cx - 2, buttonY + 3, cx + 3, buttonY + 9, ModernUiTheme.TEXT_PRIMARY);
		context.fill(cx - 4, buttonY + 7, cx - 3, buttonY + 10, ModernUiTheme.TEXT_PRIMARY);
		context.fill(cx + 3, buttonY + 7, cx + 4, buttonY + 10, ModernUiTheme.TEXT_PRIMARY);
		context.fill(cx - 3, buttonY + 10, cx + 4, buttonY + 11, ModernUiTheme.TEXT_PRIMARY);
		context.fill(cx, buttonY + 11, cx + 1, buttonY + 13, ModernUiTheme.TEXT_PRIMARY);
	}

	private void renderStatus(DrawContext context) {
		VoiceInputState state = manager.state();
		if (state != VoiceInputState.LISTENING
				&& state != VoiceInputState.RECOGNIZING
				&& state != VoiceInputState.MODEL_LOADING
				&& state != VoiceInputState.MODEL_DOWNLOADING
				&& state != VoiceInputState.MODEL_VERIFYING
				&& state != VoiceInputState.MODEL_EXTRACTING) return;
		String key = state == VoiceInputState.LISTENING
				? "chat_canvas.voice.listening"
				: state == VoiceInputState.RECOGNIZING
				? "chat_canvas.voice.recognizing"
				: state == VoiceInputState.MODEL_DOWNLOADING
				? "chat_canvas.voice.downloading"
				: "chat_canvas.voice.loading";
		Text label = Text.translatable(key);
		if (state == VoiceInputState.LISTENING
				&& manager.settings().showPartialResults()
				&& !manager.partial().isBlank()) {
			label = Text.translatable(key).append(Text.literal(": " + manager.partial()));
		}
		int width = Math.min(Math.max(1, owner.width - 8), Math.min(240,
				MinecraftClient.getInstance().textRenderer.getWidth(label) + 20));
		int x = Math.max(4, Math.min(owner.width - width - 4,
				buttonX + BUTTON_WIDTH - width));
		int y = Math.max(4, field.getY() - 34);
		ModernUiTheme.drawFixedPanel(context, x, y, width, 20, false);
		String fitted = ModernUiTheme.fitText(MinecraftClient.getInstance().textRenderer,
				label, Math.max(1, width - 10));
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
				Text.literal(fitted), x + 5, y + 4, ModernUiTheme.TEXT_PRIMARY);
		if (state == VoiceInputState.LISTENING && manager.settings().showInputLevel()) {
			int meter = (int) Math.round((width - 10) * Math.min(1.0, manager.level() * 8.0));
			context.fill(x + 5, y + 16, x + 5 + meter, y + 18, ModernUiTheme.SUCCESS);
		}
	}

	private void renderPrompt(DrawContext context, int mouseX, int mouseY) {
		PromptLayout layout = promptLayout();
		ModernUiTheme.drawFixedPanel(context, layout.x(), layout.y(),
				layout.width(), layout.height(), true);
		draw(context, "chat_canvas.voice.model.title", layout.x() + 10,
				layout.y() + 10, ModernUiTheme.TEXT_PRIMARY);
		context.drawTextWrapped(MinecraftClient.getInstance().textRenderer,
				Text.translatable("chat_canvas.voice.model.details"),
				layout.x() + 10, layout.y() + 28, layout.width() - 20,
				ModernUiTheme.TEXT_SECONDARY);
		context.drawTextWrapped(MinecraftClient.getInstance().textRenderer,
				Text.translatable("chat_canvas.voice.model.privacy"),
				layout.x() + 10, layout.y() + 48, layout.width() - 20,
				ModernUiTheme.TEXT_SECONDARY);
		button(context, mouseX, mouseY, layout.downloadX(), layout.buttonY(),
				layout.buttonWidth(), 20, "chat_canvas.voice.model.download");
		button(context, mouseX, mouseY, layout.openX(), layout.buttonY(),
				layout.buttonWidth(), 20, "chat_canvas.voice.model.open");
		button(context, mouseX, mouseY, layout.cancelX(), layout.buttonY(),
				layout.lastButtonWidth(), 20, "chat_canvas.voice.model.cancel");
	}

	private boolean promptClick(double mouseX, double mouseY, int button) {
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || owner == null) return true;
		PromptLayout layout = promptLayout();
		if (hit(mouseX, mouseY, layout.downloadX(), layout.buttonY(),
				layout.buttonWidth(), 20)) {
			installPrompt = false;
			manager.installModel();
		} else if (hit(mouseX, mouseY, layout.openX(), layout.buttonY(),
				layout.buttonWidth(), 20)) {
			manager.openModelsDirectory();
		} else if (hit(mouseX, mouseY, layout.cancelX(), layout.buttonY(),
				layout.lastButtonWidth(), 20)
				|| !hit(mouseX, mouseY, layout.x(), layout.y(),
				layout.width(), layout.height())) {
			installPrompt = false;
		}
		return true;
	}

	private static void button(DrawContext context, int mouseX, int mouseY,
							   int x, int y, int width, int height,
							   String key) {
		ModernUiTheme.drawFixedControl(context, x, y, width, height,
				hit(mouseX, mouseY, x, y, width, height), false, true);
		String fitted = ModernUiTheme.fitText(MinecraftClient.getInstance().textRenderer,
				Text.translatable(key), Math.max(1, width - 6));
		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
				Text.literal(fitted), x + width / 2, y + 6, ModernUiTheme.TEXT_PRIMARY);
	}

	private static void draw(DrawContext context, String key, int x, int y, int color) {
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
				Text.translatable(key), x, y, color);
	}

	private static boolean hit(double mx, double my, int x, int y, int width, int height) {
		return mx >= x && mx < x + width && my >= y && my < y + height;
	}

	private PromptLayout promptLayout() {
		int width = Math.max(1, Math.min(330, owner.width - 16));
		int height = Math.max(1, Math.min(128, owner.height - 16));
		int x = Math.max(0, (owner.width - width) / 2);
		int y = Math.max(0, (owner.height - height) / 2);
		int margin = Math.min(10, Math.max(1, width / 16));
		int gap = 4;
		int available = Math.max(3, width - margin * 2 - gap * 2);
		int buttonWidth = Math.max(1, available / 3);
		int lastWidth = Math.max(1, available - buttonWidth * 2);
		int downloadX = x + margin;
		int openX = downloadX + buttonWidth + gap;
		return new PromptLayout(x, y, width, height, downloadX, openX,
				openX + buttonWidth + gap, y + height - 30, buttonWidth, lastWidth);
	}

	private record PromptLayout(int x, int y, int width, int height,
			int downloadX, int openX, int cancelX, int buttonY,
			int buttonWidth, int lastButtonWidth) {
	}

	/**
	 * The existing emoji panel owns the first 20 pixel accessory slot.
	 */
	private static final class EmojiOffset {
		private static final int TOTAL_SPACE = 20;
	}
}
