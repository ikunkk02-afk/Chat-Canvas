package io.github.ikunkk02.chatcanvas.voice;

import io.github.ikunkk02.chatcanvas.ui.ModernUiTheme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public final class VoiceInputOverlay {
	public static final int BUTTON_SPACE = 20;
	private static final int BUTTON_WIDTH = 18;
	private static final int BUTTON_HEIGHT = 14;
	private final VoiceInputManager manager = VoiceInputManager.instance();
	private ChatScreen owner;
	private TextFieldWidget field;
	private Consumer<VoiceRecognitionResult> resultConsumer;
	private Runnable sessionStarted;
	private Consumer<String> partialConsumer;
	private Runnable sessionCancelled;
	private int buttonX;
	private int buttonY;
	private boolean installPrompt;
	private boolean transactionActive;
	private String deliveredPartial = "";
	private int modelScroll;

	public void init(ChatScreen screen, TextFieldWidget playerField,
					 Consumer<VoiceRecognitionResult> consumer,
					 Runnable started, Consumer<String> partial, Runnable cancelled) {
		owner = screen;
		field = playerField;
		resultConsumer = consumer;
		sessionStarted = started;
		partialConsumer = partial;
		sessionCancelled = cancelled;
		installPrompt = false;
		modelScroll = 0;
		transactionActive = false;
		deliveredPartial = "";
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (installPrompt) return promptClick(mouseX, mouseY, button);
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT
				|| !hit(mouseX, mouseY, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT)) return false;
		toggle();
		return true;
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }

	public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
		if (!installPrompt || owner == null) return false;
		PromptLayout layout = promptLayout();
		if (!hit(mouseX, mouseY, layout.x(), layout.cardsY(), layout.width(), layout.cardsHeight())) return false;
		modelScroll = clamp(modelScroll - (int) Math.round(verticalAmount * 24.0), 0, layout.maxScroll());
		return true;
	}

	public void keyboardPressed() { toggle(); }

	private void toggle() {
		if (manager.isBusy()) {
			manager.finish();
			return;
		}
		if (manager.state() == VoiceInputState.MODEL_MISSING || manager.selectedModel() == null) {
			installPrompt = true;
			modelScroll = 0;
			return;
		}
		startTransaction();
		if (!manager.begin(this::deliverFinal)) cancelTransaction();
	}

	public void tick() {
		if (owner == null) return;
		if (transactionActive && manager.settings().showPartialResults()) {
			String partial = manager.partial();
			if (!partial.equals(deliveredPartial)) {
				deliveredPartial = partial;
				partialConsumer.accept(partial);
			}
		}
		if (transactionActive && !manager.isBusy()
				&& manager.state() != VoiceInputState.MODEL_DOWNLOADING
				&& manager.state() != VoiceInputState.MODEL_VERIFYING
				&& manager.state() != VoiceInputState.MODEL_EXTRACTING
				&& manager.state() != VoiceInputState.MODEL_INSTALLING
				&& manager.state() != VoiceInputState.MODEL_LOADING) {
			cancelTransaction();
		}
	}

	private void startTransaction() {
		if (transactionActive) return;
		transactionActive = true;
		deliveredPartial = "";
		sessionStarted.run();
	}

	private void deliverFinal(VoiceRecognitionResult result) {
		resultConsumer.accept(result);
		transactionActive = false;
		deliveredPartial = "";
	}

	private void cancelTransaction() {
		if (!transactionActive) return;
		transactionActive = false;
		deliveredPartial = "";
		sessionCancelled.run();
	}

	public void cancel() {
		if (transactionActive && isModelOperation(manager.state())) manager.cancelModelInstall();
		manager.cancel();
		cancelTransaction();
	}

	public void dispose() {
		cancel();
		owner = null;
		field = null;
		resultConsumer = null;
		sessionStarted = null;
		partialConsumer = null;
		sessionCancelled = null;
		installPrompt = false;
		modelScroll = 0;
	}

	private static boolean isModelOperation(VoiceInputState state) {
		return state == VoiceInputState.MODEL_DOWNLOADING || state == VoiceInputState.MODEL_VERIFYING
				|| state == VoiceInputState.MODEL_EXTRACTING || state == VoiceInputState.MODEL_INSTALLING
				|| state == VoiceInputState.MODEL_LOADING;
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
		ModernUiTheme.drawFixedControl(context, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
				hovered, state == VoiceInputState.FINALIZING, true);
		if (manager.isListening()) {
			context.drawBorder(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, ModernUiTheme.DANGER);
		} else if (state == VoiceInputState.MODEL_MISSING || state == VoiceInputState.ERROR) {
			context.drawBorder(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, ModernUiTheme.WARNING);
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
		String key = statusKey(state);
		if (key == null) return;
		Text label = Text.translatable(key);
		if (manager.isListening() && manager.settings().showPartialResults()
				&& !manager.partial().isBlank()) label = label.copy().append(Text.literal(": " + manager.partial()));
		if (state == VoiceInputState.MODEL_DOWNLOADING && manager.progressTotal() > 0L) {
			long doneMiB = manager.progress() / 1_048_576L;
			long totalMiB = manager.progressTotal() / 1_048_576L;
			int percent = (int) Math.min(100L, manager.progress() * 100L / manager.progressTotal());
			label = label.copy().append(Text.literal(String.format(Locale.ROOT,
					"  %d / %d MiB (%d%%)", doneMiB, totalMiB, percent)));
		}
		int width = Math.min(Math.max(1, owner.width - 8), Math.min(280,
				MinecraftClient.getInstance().textRenderer.getWidth(label) + 20));
		int x = Math.max(4, Math.min(owner.width - width - 4, buttonX + BUTTON_WIDTH - width));
		int y = Math.max(4, field.getY() - 34);
		ModernUiTheme.drawFixedPanel(context, x, y, width, 20, false);
		String fitted = ModernUiTheme.fitText(MinecraftClient.getInstance().textRenderer,
				label, Math.max(1, width - 10));
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
				Text.literal(fitted), x + 5, y + 4, ModernUiTheme.TEXT_PRIMARY);
		if (manager.isListening() && manager.settings().showInputLevel()) {
			int meter = (int) Math.round((width - 10) * Math.min(1.0, manager.level() * 8.0));
			context.fill(x + 5, y + 16, x + 5 + meter, y + 18, ModernUiTheme.SUCCESS);
		}
	}

	private static String statusKey(VoiceInputState state) {
		return switch (state) {
			case OPENING_CHAT, WAITING_FOR_SPEECH -> "chat_canvas.voice.listening";
			case SPEAKING -> "chat_canvas.voice.speaking";
			case WAITING_FOR_ENDPOINT -> "chat_canvas.voice.waiting_endpoint";
			case FINALIZING, COMMITTING_RESULT -> "chat_canvas.voice.finalizing";
			case MODEL_DOWNLOADING -> "chat_canvas.voice.downloading";
			case MODEL_VERIFYING -> "chat_canvas.voice.verifying";
			case MODEL_EXTRACTING, MODEL_INSTALLING -> "chat_canvas.voice.installing";
			case MODEL_LOADING -> "chat_canvas.voice.loading";
			default -> null;
		};
	}

	private void renderPrompt(DrawContext context, int mouseX, int mouseY) {
		PromptLayout layout = promptLayout();
		ModernUiTheme.drawFixedPanel(context, layout.x(), layout.y(), layout.width(), layout.height(), true);
		draw(context, "chat_canvas.voice.model.choose", layout.x() + 10, layout.y() + 8, ModernUiTheme.TEXT_PRIMARY);
		List<VoiceModelDescriptor> models = manager.models();
		context.enableScissor(layout.x() + 7, layout.cardsY(),
				layout.x() + layout.width() - 7, layout.cardsBottom());
		for (int index = 0; index < models.size(); index++) {
			VoiceModelDescriptor model = models.get(index);
			int y = layout.cardsY() + index * 48 - modelScroll;
			if (y + 46 <= layout.cardsY() || y >= layout.cardsBottom()) continue;
			boolean hovered = mouseY >= layout.cardsY() && mouseY < layout.cardsBottom()
					&& hit(mouseX, mouseY, layout.x() + 8, y, layout.width() - 16, 46);
			ModernUiTheme.drawFixedControl(context, layout.x() + 8, y, layout.width() - 16,
					46, hovered, model.id().equals(manager.settings().selectedModelId()), true);
			drawModelCard(context, model, layout.x() + 13, y + 3,
					layout.width() - 26, 48);
		}
		context.disableScissor();
		button(context, mouseX, mouseY, layout.x() + 8, layout.cancelY(), layout.width() - 16,
				18, "chat_canvas.voice.model.cancel");
	}

	private void drawModelCard(DrawContext context, VoiceModelDescriptor model,
						   int x, int y, int width, int cardHeight) {
		boolean installed = manager.isModelInstalled(model.id());
		String name = Text.translatable(model.displayNameKey()).getString();
		String action = Text.translatable(installed
				? "chat_canvas.voice.model.installed" : "chat_canvas.voice.model.download").getString();
		drawFitted(context, name + "  [" + action + "]", x, y, width, ModernUiTheme.TEXT_PRIMARY);
		if (cardHeight < 26) return;
		String languages = model.languageKeys().stream().map(key -> Text.translatable(key).getString())
				.reduce((a, b) -> a + "/" + b).orElse("");
		drawFitted(context, languages + "  " + formatMiB(model.downloadSize()), x, y + 12,
				width, ModernUiTheme.TEXT_SECONDARY);
		if (cardHeight < 38) return;
		String profiles = Text.translatable(model.performanceKey()).getString() + " · "
				+ Text.translatable(model.responseKey()).getString() + " · "
				+ Text.translatable(model.accuracyKey()).getString();
		drawFitted(context, profiles, x, y + 23, width, ModernUiTheme.TEXT_SECONDARY);
		if (cardHeight < 46) return;
		String reload = Text.translatable(model.reloadPolicy() == ReloadPolicy.HOT_SWAP
				? "chat_canvas.voice.reload.hot_swap" : "chat_canvas.voice.reload.restart").getString();
		VoiceModelCapability capability = manager.modelCapability(model);
		VoicePlatformSupport.OperatingSystem currentOs = VoicePlatformSupport.current().os();
		boolean windows = model.supportsWindows() && (currentOs != VoicePlatformSupport.OperatingSystem.WINDOWS || capability.available());
		boolean android = model.supportsAndroid() && (currentOs != VoicePlatformSupport.OperatingSystem.ANDROID || capability.available());
		boolean ios = model.supportsIos() && (currentOs != VoicePlatformSupport.OperatingSystem.IOS || capability.available());
		boolean linux = model.supportsLinux() && (currentOs != VoicePlatformSupport.OperatingSystem.LINUX || capability.available());
		boolean macos = model.supportsMacOs() && (currentOs != VoicePlatformSupport.OperatingSystem.MACOS || capability.available());
		String platforms = Text.translatable("chat_canvas.voice.platform.windows").getString() + " " + mark(windows)
				+ "  " + Text.translatable("chat_canvas.voice.platform.android").getString() + " " + mark(android)
				+ "  " + Text.translatable("chat_canvas.voice.platform.ios").getString() + " " + mark(ios)
				+ "  " + Text.translatable("chat_canvas.voice.platform.linux").getString() + " " + mark(linux)
				+ "  " + Text.translatable("chat_canvas.voice.platform.macos").getString() + " " + mark(macos)
				+ "  " + reload;
		if (!capability.available() && !capability.reasonKey().isBlank()) {
			platforms += " · " + Text.translatable(capability.reasonKey()).getString();
			if (!capability.detail().isBlank()) platforms += " (" + capability.detail() + ")";
		}
		drawFitted(context, platforms, x, y + 33, width, ModernUiTheme.TEXT_SECONDARY);
	}

	private boolean promptClick(double mouseX, double mouseY, int button) {
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || owner == null) return true;
		PromptLayout layout = promptLayout();
		List<VoiceModelDescriptor> models = manager.models();
		for (int index = 0; index < models.size(); index++) {
			int y = layout.cardsY() + index * 48 - modelScroll;
			if (mouseY < layout.cardsY() || mouseY >= layout.cardsBottom()
					|| !hit(mouseX, mouseY, layout.x() + 8, y, layout.width() - 16, 46)) continue;
			VoiceModelDescriptor model = models.get(index);
			if (!manager.modelCapability(model).available()) {
				manager.installModel(model.id(), null, false);
				return true;
			}
			installPrompt = false;
			startTransaction();
			if (manager.isModelInstalled(model.id())) {
				manager.selectModel(model.id());
				if (!manager.begin(this::deliverFinal)) cancelTransaction();
			} else {
				manager.installModel(model.id(), this::deliverFinal, true);
			}
			return true;
		}
		if (hit(mouseX, mouseY, layout.x() + 8, layout.cancelY(), layout.width() - 16, 18)
				|| !hit(mouseX, mouseY, layout.x(), layout.y(), layout.width(), layout.height())) {
			installPrompt = false;
		}
		return true;
	}

	private PromptLayout promptLayout() {
		int width = Math.max(1, Math.min(390, owner.width - 8));
		int desired = 34 + manager.models().size() * 48 + 28;
		int height = Math.max(1, Math.min(desired, owner.height - 8));
		int x = Math.max(0, (owner.width - width) / 2);
		int y = Math.max(0, (owner.height - height) / 2);
		int cardsY = y + 24;
		int cancelY = y + height - 21;
		int cardsBottom = Math.max(cardsY, cancelY - 3);
		int maxScroll = Math.max(0, manager.models().size() * 48 - (cardsBottom - cardsY));
		modelScroll = clamp(modelScroll, 0, maxScroll);
		return new PromptLayout(x, y, width, height, cardsY, cardsBottom, cancelY, maxScroll);
	}

	private static void button(DrawContext context, int mouseX, int mouseY, int x, int y,
						   int width, int height, String key) {
		ModernUiTheme.drawFixedControl(context, x, y, width, height,
				hit(mouseX, mouseY, x, y, width, height), false, true);
		context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
				Text.translatable(key), x + width / 2, y + 5, ModernUiTheme.TEXT_PRIMARY);
	}

	private static void draw(DrawContext context, String key, int x, int y, int color) {
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.translatable(key), x, y, color);
	}

	private static void drawFitted(DrawContext context, String value, int x, int y, int width, int color) {
		String fitted = ModernUiTheme.fitText(MinecraftClient.getInstance().textRenderer,
				Text.literal(value), Math.max(1, width));
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(fitted), x, y, color);
	}

	private static String mark(boolean supported) { return supported ? "✓" : "—"; }
	private static String formatMiB(long bytes) { return String.format(Locale.ROOT, "%.2f MiB", bytes / 1_048_576.0); }
	private static int clamp(int value, int minimum, int maximum) { return Math.max(minimum, Math.min(maximum, value)); }
	private static boolean hit(double mx, double my, int x, int y, int width, int height) {
		return mx >= x && mx < x + width && my >= y && my < y + height;
	}

	private record PromptLayout(int x, int y, int width, int height,
							int cardsY, int cardsBottom, int cancelY, int maxScroll) {
		private int cardsHeight() { return Math.max(0, cardsBottom - cardsY); }
	}

	/** The existing emoji panel owns the first 20 pixel accessory slot. */
	private static final class EmojiOffset { private static final int TOTAL_SPACE = 20; }
}
