package io.github.ikunkk02.chatcanvas.voice;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import io.github.ikunkk02.chatcanvas.chat.render.ChatBackgroundDraw;

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
		int fill = state == VoiceInputState.LISTENING ? 0xE05D2E46
				: state == VoiceInputState.RECOGNIZING ? 0xE04A5368
				: state == VoiceInputState.MODEL_MISSING || state == VoiceInputState.ERROR
				? 0xE06B4430 : hovered ? 0xD0445066 : 0xB02A3240;
		context.fill(buttonX, buttonY, buttonX + BUTTON_WIDTH, buttonY + BUTTON_HEIGHT, fill);
		ChatBackgroundDraw.drawBorder(context, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
				state == VoiceInputState.LISTENING ? 0xFFFF858D : 0xFF71809A);
		int cx = buttonX + 9;
		context.fill(cx - 2, buttonY + 3, cx + 3, buttonY + 9, 0xFFE7ECF5);
		context.fill(cx - 4, buttonY + 7, cx - 3, buttonY + 10, 0xFFE7ECF5);
		context.fill(cx + 3, buttonY + 7, cx + 4, buttonY + 10, 0xFFE7ECF5);
		context.fill(cx - 3, buttonY + 10, cx + 4, buttonY + 11, 0xFFE7ECF5);
		context.fill(cx, buttonY + 11, cx + 1, buttonY + 13, 0xFFE7ECF5);
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
		int width = Math.min(240,
				MinecraftClient.getInstance().textRenderer.getWidth(label) + 20);
		int x = Math.max(4, buttonX + BUTTON_WIDTH - width);
		int y = Math.max(4, field.getY() - 34);
		context.fill(x, y, x + width, y + 20, 0xD0202632);
		ChatBackgroundDraw.drawBorder(context, x, y, width, 20, 0xFF71809A);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
				label, x + 5, y + 4, 0xFFFFFFFF);
		if (state == VoiceInputState.LISTENING && manager.settings().showInputLevel()) {
			int meter = (int) Math.round((width - 10) * Math.min(1.0, manager.level() * 8.0));
			context.fill(x + 5, y + 16, x + 5 + meter, y + 18, 0xFF63D297);
		}
	}

	private void renderPrompt(DrawContext context, int mouseX, int mouseY) {
		int width = Math.min(330, owner.width - 16);
		int height = 128;
		int x = (owner.width - width) / 2;
		int y = Math.max(8, (owner.height - height) / 2);
		context.fill(x, y, x + width, y + height, 0xF0181D27);
		ChatBackgroundDraw.drawBorder(context, x, y, width, height, 0xFF71809A);
		draw(context, "chat_canvas.voice.model.title", x + 10, y + 10, 0xFFFFFFFF);
		draw(context, "chat_canvas.voice.model.details", x + 10, y + 28, 0xFFADB6C7);
		draw(context, "chat_canvas.voice.model.privacy", x + 10, y + 44, 0xFFADB6C7);
		button(context, x + 10, y + 82, 112, 20,
				"chat_canvas.voice.model.download");
		button(context, x + 126, y + 82, 98, 20,
				"chat_canvas.voice.model.open");
		button(context, x + 228, y + 82, width - 238, 20,
				"chat_canvas.voice.model.cancel");
	}

	private boolean promptClick(double mouseX, double mouseY, int button) {
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || owner == null) return true;
		int width = Math.min(330, owner.width - 16);
		int x = (owner.width - width) / 2;
		int y = Math.max(8, (owner.height - 128) / 2);
		if (hit(mouseX, mouseY, x + 10, y + 82, 112, 20)) {
			installPrompt = false;
			manager.installModel();
		} else if (hit(mouseX, mouseY, x + 126, y + 82, 98, 20)) {
			manager.openModelsDirectory();
		} else if (hit(mouseX, mouseY, x + 228, y + 82, width - 238, 20)
				|| !hit(mouseX, mouseY, x, y, width, 128)) {
			installPrompt = false;
		}
		return true;
	}

	private static void button(DrawContext context, int x, int y, int width, int height,
							   String key) {
		context.fill(x, y, x + width, y + height, 0xFF343D50);
		ChatBackgroundDraw.drawBorder(context, x, y, width, height, 0xFF71809A);
		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
				Text.translatable(key), x + width / 2, y + 6, 0xFFFFFFFF);
	}

	private static void draw(DrawContext context, String key, int x, int y, int color) {
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
				Text.translatable(key), x, y, color);
	}

	private static boolean hit(double mx, double my, int x, int y, int width, int height) {
		return mx >= x && mx < x + width && my >= y && my < y + height;
	}

	/**
	 * The existing emoji panel owns the first 20 pixel accessory slot.
	 */
	private static final class EmojiOffset {
		private static final int TOTAL_SPACE = 20;
	}
}
