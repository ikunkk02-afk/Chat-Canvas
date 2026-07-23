package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.animation.AnimationClock;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.ui.AlignmentGuideRenderer;
import io.github.ikunkk02.chatcanvas.ui.AnimatedSettingsPanel;
import io.github.ikunkk02.chatcanvas.ui.ModernUiTheme;
import io.github.ikunkk02.chatcanvas.ui.PreviewChatWidget;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class ChatCanvasEditorScreen extends BaseOwoScreen<FlowLayout> {
	private final @Nullable Screen parent;
	private final AnimationClock animationClock = new AnimationClock();

	private EditorSession session;
	private PreviewChatWidget preview;
	private AnimatedSettingsPanel settingsPanel;
	private FlowLayout toolbar;
	private ButtonComponent undoButton;
	private ButtonComponent redoButton;

	public ChatCanvasEditorScreen(@Nullable Screen parent) {
		super(Text.translatable("chat_canvas.editor.title"));
		this.parent = parent;
	}

	@Override
	protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
		if (session == null) {
			session = new EditorSession(ChatCanvasConfig.instance().layout(), width, height);
		}
		return OwoUIAdapter.create(this, Containers::verticalFlow);
	}

	@Override
	protected void build(FlowLayout root) {
		root.sizing(Sizing.fill(), Sizing.fill());
		root.allowOverflow(false);

		preview = new PreviewChatWidget(session, width, height,
				this::onGeometryChanged, this::commitCurrent);
		preview.zIndex(10);
		root.child(preview);

		settingsPanel = new AnimatedSettingsPanel(session, width, height,
				this::onGeometryChanged, this::refreshHistoryButtons,
				this::saveAndClose, this::cancelAndClose);
		root.child(settingsPanel.component());

		toolbar = buildToolbar();
		root.child(toolbar);
		refreshHistoryButtons();
	}

	private FlowLayout buildToolbar() {
		FlowLayout bar = Containers.horizontalFlow(Sizing.fixed(330), Sizing.fixed(32));
		bar.positioning(Positioning.absolute(Math.max(8, (width - 330) / 2), 10));
		bar.padding(Insets.of(5));
		bar.gap(6);
		bar.surface(ModernUiTheme.PANEL_SURFACE);
		bar.horizontalAlignment(HorizontalAlignment.RIGHT);
		bar.verticalAlignment(VerticalAlignment.CENTER);
		bar.zIndex(30);

		var title = Components.label(Text.translatable("chat_canvas.editor.title")
				.formatted(Formatting.WHITE, Formatting.BOLD));
		title.horizontalSizing(Sizing.fill(55));
		bar.child(title);

		undoButton = ModernUiTheme.button(Text.translatable("chat_canvas.action.undo"), button -> undo());
		undoButton.sizing(Sizing.fixed(72), Sizing.fixed(22));
		redoButton = ModernUiTheme.button(Text.translatable("chat_canvas.action.redo"), button -> redo());
		redoButton.sizing(Sizing.fixed(72), Sizing.fixed(22));
		bar.child(undoButton);
		bar.child(redoButton);
		return bar;
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
		renderBackground(context, mouseX, mouseY, delta);
		if (preview != null) {
			AlignmentGuideRenderer.render(context, width, height, session.layout(), preview);
		}
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		if (client != null && client.world == null) {
			renderPanoramaBackground(context, delta);
		}
		context.fill(0, 0, width, height, 0x88070A10);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_Z) {
			undo();
			return true;
		}
		if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_Y) {
			redo();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void resize(MinecraftClient client, int width, int height) {
		if (session != null) {
			session.resizeViewport(width, height);
		}
		super.resize(client, width, height);
		if (preview != null) {
			preview.resizeViewport(width, height);
		}
		if (settingsPanel != null) {
			settingsPanel.resizeViewport(width, height);
		}
		if (toolbar != null) {
			toolbar.positioning(Positioning.absolute(Math.max(8, (width - 330) / 2), 10));
		}
		animationClock.reset();
		onGeometryChanged();
	}

	@Override
	public void close() {
		cancelAndClose();
	}

	private void onGeometryChanged() {
		if (preview != null) preview.syncFromSession();
		if (settingsPanel != null) settingsPanel.syncFromSession();
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
		if (ChatCanvasConfig.instance().save(session.snapshot())) {
			returnToParent();
		}
	}

	private void cancelAndClose() {
		returnToParent();
	}

	private void returnToParent() {
		if (client != null) {
			client.setScreen(parent);
		}
	}
}
