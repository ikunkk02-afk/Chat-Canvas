package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.animation.AnimationClock;
import io.github.ikunkk02.chatcanvas.chat.layout.ChatLayoutRuntime;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasSettings;
import io.github.ikunkk02.chatcanvas.ui.AlignmentGuideRenderer;
import io.github.ikunkk02.chatcanvas.ui.AnimatedSettingsPanel;
import io.github.ikunkk02.chatcanvas.ui.ModernUiTheme;
import io.github.ikunkk02.chatcanvas.ui.ModernColorPickerPopup;
import io.github.ikunkk02.chatcanvas.ui.NumericScrubber;
import io.github.ikunkk02.chatcanvas.ui.NumericScrubberComponent;
import io.github.ikunkk02.chatcanvas.ui.PreviewChatWidget;
import io.github.ikunkk02.chatcanvas.ui.SingleLineLabelComponent;
import io.github.ikunkk02.chatcanvas.ui.UiLayoutMetrics;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class ChatCanvasEditorScreen extends BaseOwoScreen<FlowLayout> {
	private final @Nullable Screen parent;
	private final AnimationClock animationClock = new AnimationClock();
	private final EditorPointerCapture pointerCapture = new EditorPointerCapture();

	private EditorSession session;
	private PreviewChatWidget preview;
	private PreviewChatWidget commandPreview;
	private AnimatedSettingsPanel settingsPanel;
	private ScrollContainer<FlowLayout> toolbar;
	private ButtonComponent undoButton;
	private ButtonComponent redoButton;
	private ButtonComponent themeButton;
	private ModernColorPickerPopup colorPickerPopup;

	public ChatCanvasEditorScreen(@Nullable Screen parent) {
		super(Text.translatable("chat_canvas.editor.title"));
		this.parent = parent;
	}

	@Override
	protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
		if (session == null) {
			session = new EditorSession(ChatCanvasConfig.instance().settings(), width, height);
		}
		ModernUiTheme.setStyle(ChatCanvasConfig.instance().editorUiStyle());
		return OwoUIAdapter.create(this, Containers::verticalFlow);
	}

	@Override
	protected void build(FlowLayout root) {
		root.sizing(Sizing.fill(), Sizing.fill());
		root.allowOverflow(false);

		preview = new PreviewChatWidget(session, width, height,
				this::onGeometryChanged, this::commitCurrent);
		root.child(preview);
		commandPreview = new PreviewChatWidget(session, EditorChannel.COMMAND_SYSTEM,
				width, height, this::onGeometryChanged, this::commitCurrent);
		root.child(commandPreview);

		settingsPanel = new AnimatedSettingsPanel(session, width, height,
				this::onGeometryChanged, this::refreshHistoryButtons,
				this::saveAndClose, this::cancelAndClose,
				preview::previewState, preview::setPreviewState,
				this::openColorPicker);
		root.child(settingsPanel.component());

		toolbar = buildToolbar();
		root.child(toolbar);
		refreshHistoryButtons();
	}

	private ScrollContainer<FlowLayout> buildToolbar() {
		UiLayoutMetrics.Toolbar metrics = UiLayoutMetrics.toolbar(width);
		boolean compact = UiLayoutMetrics.layoutMode(width, height)
				== UiLayoutMetrics.LayoutMode.COMPACT;
		int barWidth = compact ? 454 : UiLayoutMetrics.TOOLBAR_PREFERRED_WIDTH;
		FlowLayout bar = Containers.horizontalFlow(
				Sizing.fixed(barWidth), Sizing.fixed(UiLayoutMetrics.TOOLBAR_HEIGHT));
		bar.padding(Insets.of(5).withLeft(16));
		bar.gap(6);
		bar.surface(ModernUiTheme.PANEL_SURFACE);
		bar.horizontalAlignment(HorizontalAlignment.RIGHT);
		bar.verticalAlignment(VerticalAlignment.CENTER);

		if (!compact) {
			bar.child(new SingleLineLabelComponent(
					Text.translatable("chat_canvas.editor.title")
							.formatted(Formatting.WHITE, Formatting.BOLD),
					0xFFFFFFFF, 120, 12));
		}
		ButtonComponent playerButton = ModernUiTheme.button(
				Text.translatable("chat_canvas.editor.channel.player"),
				button -> selectChannel(EditorChannel.PLAYER_CHAT));
		playerButton.sizing(Sizing.fixed(76), Sizing.fixed(22));
		ButtonComponent commandButton = ModernUiTheme.button(
				Text.translatable("chat_canvas.editor.channel.command"),
				button -> selectChannel(EditorChannel.COMMAND_SYSTEM));
		commandButton.sizing(Sizing.fixed(88), Sizing.fixed(22));
		bar.child(playerButton);
		bar.child(commandButton);

		ButtonComponent styleButton = ModernUiTheme.button(
				Text.translatable("chat_canvas.ui_theme").append(Text.literal(": "))
						.append(Text.translatable(ModernUiTheme.currentStyle() == EditorUiStyle.CHAT_CANVAS
								? "chat_canvas.ui_theme.chat_canvas"
								: "chat_canvas.ui_theme.vanilla")),
				button -> onSwitchTheme());
		styleButton.sizing(Sizing.fixed(160), Sizing.fixed(22));
		this.themeButton = styleButton;
		bar.child(styleButton);

		undoButton = ModernUiTheme.button(Text.translatable("chat_canvas.action.undo"), button -> undo());
		undoButton.sizing(Sizing.fixed(52), Sizing.fixed(22));
		redoButton = ModernUiTheme.button(Text.translatable("chat_canvas.action.redo"), button -> redo());
		redoButton.sizing(Sizing.fixed(52), Sizing.fixed(22));
		bar.child(undoButton);
		bar.child(redoButton);
		ScrollContainer<FlowLayout> viewport = Containers.horizontalScroll(
				Sizing.fixed(metrics.width()), Sizing.fixed(metrics.height()), bar);
		viewport.positioning(Positioning.absolute(metrics.x(), metrics.y()));
		viewport.scrollbarThiccness(1);
		viewport.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(ModernUiTheme.SCROLLBAR)));
		return viewport;
	}

	private void onSwitchTheme() {
		EditorUiStyle next = ModernUiTheme.currentStyle() == EditorUiStyle.CHAT_CANVAS
				? EditorUiStyle.VANILLA : EditorUiStyle.CHAT_CANVAS;
		ModernUiTheme.setStyle(next);
		ChatCanvasSettings cur = ChatCanvasConfig.instance().settings();
		ChatCanvasConfig.instance().save(new ChatCanvasSettings(
				cur.layout(), cur.text(), cur.background(),
				cur.playerColors(), cur.mention(), cur.commandClipboard(),
				cur.recentColors(), next, cur.enabled(), cur.playerChatEnabled(),
				cur.playerChatLayoutMode(), cur.splitMessageMaxWidthRatio(),
				cur.commandSystem()));
		// Update the theme button text to reflect the new theme.
		if (themeButton != null) {
			themeButton.setMessage(
					Text.translatable("chat_canvas.ui_theme").append(Text.literal(": "))
							.append(Text.translatable(next == EditorUiStyle.CHAT_CANVAS
									? "chat_canvas.ui_theme.chat_canvas"
									: "chat_canvas.ui_theme.vanilla")));
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		double deltaSeconds = animationClock.tick();
		if (settingsPanel != null) {
			settingsPanel.update(deltaSeconds);
			settingsPanel.syncFromSession();
		}
		if (preview != null) {
			preview.syncFromSession();
		}
		if (commandPreview != null) commandPreview.syncFromSession();
		renderBackground(context, mouseX, mouseY, delta);
		PreviewChatWidget selectedPreview = selectedPreview();
		if (selectedPreview != null) {
			AlignmentGuideRenderer.render(context, width, height, session.layout(), selectedPreview);
		}
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		if (client != null && client.world == null) {
			renderPanoramaBackground(context, delta);
		}
		context.fill(0, 0, width, height, ModernUiTheme.SCREEN_OVERLAY);
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		int keyCode = input.key();
		if (colorPickerPopup != null) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				colorPickerPopup.cancel();
				return true;
			}
			super.keyPressed(input);
			return true;
		}
		if (MinecraftClient.getInstance().isCtrlPressed() && keyCode == GLFW.GLFW_KEY_Z) {
			undo();
			return true;
		}
		if (MinecraftClient.getInstance().isCtrlPressed() && keyCode == GLFW.GLFW_KEY_Y) {
			redo();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		if (colorPickerPopup != null) {
			if (!colorPickerPopup.containsScreen(mouseX, mouseY)) {
				colorPickerPopup.cancel();
				return true;
			}
			super.mouseClicked(click, doubled);
			return true;
		}
		NumericScrubber scrubber = settingsPanel == null
				? null
				: settingsPanel.scrubberAt(mouseX, mouseY);
		if (scrubber != null) {
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
				return pointerCapture.begin(scrubber, mouseX, mouseY, button,
						MinecraftClient.getInstance().isShiftPressed(),
						MinecraftClient.getInstance().isCtrlPressed());
			}
			if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
				return scrubber.restoreDefault();
			}
		}

		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && commandPreviewCanReceive(mouseX, mouseY)) {
			selectChannel(EditorChannel.COMMAND_SYSTEM);
			return pointerCapture.begin(commandPreview, mouseX, mouseY, button,
					MinecraftClient.getInstance().isShiftPressed(),
					MinecraftClient.getInstance().isCtrlPressed());
		}
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && previewCanReceive(mouseX, mouseY)) {
			selectChannel(EditorChannel.PLAYER_CHAT);
			return pointerCapture.begin(preview, mouseX, mouseY, button,
					MinecraftClient.getInstance().isShiftPressed(),
					MinecraftClient.getInstance().isCtrlPressed());
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseDragged(Click click, double deltaX, double deltaY) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		if (colorPickerPopup != null) {
			super.mouseDragged(click, deltaX, deltaY);
			return true;
		}
		if (pointerCapture.active()) {
			return pointerCapture.drag(mouseX, mouseY, button);
		}
		return super.mouseDragged(click, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(Click click) {
		double mouseX = click.x();
		double mouseY = click.y();
		int button = click.button();
		if (colorPickerPopup != null) {
			super.mouseReleased(click);
			return true;
		}
		if (pointerCapture.active()) {
			return pointerCapture.release(mouseX, mouseY, button);
		}
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (colorPickerPopup != null) {
			return true;
		}
		NumericScrubber scrubber = settingsPanel == null
				? null
				: settingsPanel.scrubberAt(mouseX, mouseY);
		if (scrubber != null && scrubber.scroll(verticalAmount)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void tick() {
		super.tick();
		if (client != null && !client.isWindowFocused()) {
			pointerCapture.cancel();
		}
	}

	private boolean previewCanReceive(double mouseX, double mouseY) {
		if (preview == null || uiAdapter == null || !preview.containsInteraction(mouseX, mouseY)) {
			return false;
		}
		Component top = uiAdapter.rootComponent.childAt((int) Math.floor(mouseX), (int) Math.floor(mouseY));
		return top == preview || top == uiAdapter.rootComponent;
	}

	private boolean commandPreviewCanReceive(double mouseX, double mouseY) {
		if (commandPreview == null || uiAdapter == null
				|| !commandPreview.containsInteraction(mouseX, mouseY)) return false;
		Component top = uiAdapter.rootComponent.childAt(
				(int) Math.floor(mouseX), (int) Math.floor(mouseY));
		return top == commandPreview || top == uiAdapter.rootComponent;
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		pointerCapture.cancel();
		if (colorPickerPopup != null) {
			colorPickerPopup.cancel();
		}
		if (session != null) {
			session.resizeViewport(width, height);
		}
		super.resize(client, width, height);
		if (preview != null) {
			preview.resizeViewport(width, height);
		}
		if (commandPreview != null) commandPreview.resizeViewport(width, height);
		if (settingsPanel != null) {
			settingsPanel.resizeViewport(width, height);
		}
		if (toolbar != null) {
			UiLayoutMetrics.Toolbar metrics = UiLayoutMetrics.toolbar(width);
			toolbar.sizing(Sizing.fixed(metrics.width()), Sizing.fixed(metrics.height()));
			toolbar.positioning(Positioning.absolute(metrics.x(), metrics.y()));
		}
		animationClock.reset();
		onGeometryChanged();
	}

	@Override
	public void close() {
		pointerCapture.cancel();
		if (colorPickerPopup != null) {
			colorPickerPopup.cancel();
		}
		cancelAndClose();
	}

	@Override
	public void removed() {
		io.github.ikunkk02.chatcanvas.voice.VoiceInputManager.instance()
				.stopMicrophoneTest();
		pointerCapture.cancel();
		if (colorPickerPopup != null) {
			colorPickerPopup.cancel();
		}
		if (preview != null) {
			preview.dispose();
		}
		if (commandPreview != null) commandPreview.dispose();
		super.removed();
	}

	private void onGeometryChanged() {
		if (preview != null) preview.syncFromSession();
		if (commandPreview != null) commandPreview.syncFromSession();
		if (settingsPanel != null) settingsPanel.syncFromSession();
	}

	private void selectChannel(EditorChannel channel) {
		session.select(channel);
		onGeometryChanged();
	}

	private PreviewChatWidget selectedPreview() {
		return session.selectedChannel() == EditorChannel.COMMAND_SYSTEM
				? commandPreview : preview;
	}

	private void commitCurrent() {
		session.commit();
		refreshHistoryButtons();
	}

	private void undo() {
		if (session.undo()) {
			onGeometryChanged();
			refreshHistoryButtons();
		}
	}

	private void redo() {
		if (session.redo()) {
			onGeometryChanged();
			refreshHistoryButtons();
		}
	}

	private void refreshHistoryButtons() {
		if (undoButton != null) undoButton.active(session.canUndo());
		if (redoButton != null) redoButton.active(session.canRedo());
	}

	private void saveAndClose() {
		if (ChatCanvasConfig.instance().save(session.settings())) {
			ChatLayoutRuntime.applySavedSettings();
			io.github.ikunkk02.chatcanvas.chat.render.DualChatHudRenderer.instance()
					.invalidatePlayerLayouts();
			returnToParent();
		}
	}

	private void cancelAndClose() {
		pointerCapture.cancel();
		if (session != null) {
			session.apply(session.original());
		}
		returnToParent();
	}

	private void returnToParent() {
		if (client != null) {
			client.setScreen(parent);
		}
	}

	private void openColorPicker(ButtonComponent anchor, ModernColorPickerPopup.Request request) {
		pointerCapture.cancel();
		if (colorPickerPopup != null) {
			colorPickerPopup.cancel();
		}
		UiLayoutMetrics.ColorPicker metrics = UiLayoutMetrics.colorPicker(width, height);
		int[] position = colorPickerPosition(anchor, metrics);
		colorPickerPopup = new ModernColorPickerPopup(
				position[0],
				position[1],
				metrics,
				request,
				this::closeColorPicker
		);
		if (uiAdapter != null) {
			uiAdapter.rootComponent.child(colorPickerPopup);
		}
	}

	private void closeColorPicker() {
		ModernColorPickerPopup popup = colorPickerPopup;
		colorPickerPopup = null;
		if (popup != null && uiAdapter != null) {
			uiAdapter.rootComponent.removeChild(popup);
		}
	}

	private int[] colorPickerPosition(
			ButtonComponent anchor, UiLayoutMetrics.ColorPicker metrics) {
		int margin = 6;
		int maxX = Math.max(4, width - metrics.width() - 4);
		int maxY = Math.max(4, height - metrics.height() - 4);
		int anchorY = clamp(anchor.getY(), 4, maxY);
		int[][] candidates = new int[][]{
				{anchor.getX() + anchor.getWidth() + margin, anchorY},
				{anchor.getX() - metrics.width() - margin, anchorY},
				{anchor.getX(), anchor.getY() - metrics.height() - margin}
		};
		for (int[] candidate : candidates) {
			int candidateX = clamp(candidate[0], 4, maxX);
			int candidateY = clamp(candidate[1], 4, maxY);
			if (!intersectsPreview(candidateX, candidateY,
					metrics.width(), metrics.height())) {
				return new int[]{candidateX, candidateY};
			}
		}
		return new int[]{clamp(candidates[1][0], 4, maxX), anchorY};
	}

	private boolean intersectsPreview(int x, int y, int popupWidth, int popupHeight) {
		var layout = session.layout();
		return x < layout.right() && x + popupWidth > layout.x()
				&& y < layout.bottom() && y + popupHeight > layout.y();
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
