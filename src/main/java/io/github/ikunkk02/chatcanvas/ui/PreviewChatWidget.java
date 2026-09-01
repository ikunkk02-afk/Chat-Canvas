package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.chat.render.ChatRenderContext;
import io.github.ikunkk02.chatcanvas.chat.render.ChatRenderEngine;
import io.github.ikunkk02.chatcanvas.chat.render.PreviewChatMessage;
import io.github.ikunkk02.chatcanvas.chat.render.PreviewChatState;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.editor.EditorPointerTarget;
import io.github.ikunkk02.chatcanvas.editor.EditorSession;
import io.github.ikunkk02.chatcanvas.editor.EditorChannel;
import io.github.ikunkk02.chatcanvas.editor.LayoutEditorMath;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerChatIdentity;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.List;

public final class PreviewChatWidget extends BaseComponent implements EditorPointerTarget {
	private static final int HANDLE_THICKNESS = 7;
	private static final int SNAP_DISTANCE = 7;
	private static final int HANDLE_SIZE = 4;

	private final EditorSession session;
	private final EditorChannel channel;
	private final Runnable changedCallback;
	private final Runnable committedCallback;
	private final ChatRenderEngine renderEngine = new ChatRenderEngine();

	private ResizeHandle hoveredHandle = ResizeHandle.NONE;
	private ResizeHandle activeHandle = ResizeHandle.NONE;
	private PixelLayout dragStartLayout;
	private double dragStartMouseX;
	private double dragStartMouseY;
	private boolean geometryChanged;
	private boolean snappedX;
	private boolean snappedY;
	private int screenWidth;
	private int screenHeight;

	public PreviewChatWidget(EditorSession session, int screenWidth, int screenHeight,
							 Runnable changedCallback, Runnable committedCallback) {
		this(session, EditorChannel.PLAYER_CHAT, screenWidth, screenHeight,
				changedCallback, committedCallback);
	}

	public PreviewChatWidget(EditorSession session, EditorChannel channel,
							 int screenWidth, int screenHeight,
							 Runnable changedCallback, Runnable committedCallback) {
		this.session = session;
		this.channel = channel;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.changedCallback = changedCallback;
		this.committedCallback = committedCallback;
		this.renderEngine.messages(previewMessages());
		PixelLayout layout = session.layout(channel);
		this.sizing(Sizing.fixed(layout.width()), Sizing.fixed(layout.height()));
		this.positioning(Positioning.absolute(layout.x(), layout.y()));
	}

	public void syncFromSession() {
		PixelLayout layout = session.layout(channel);
		if (this.width() == layout.width() && this.height() == layout.height()
				&& this.x() == layout.x() && this.y() == layout.y()) return;
		this.<PreviewChatWidget>configure(component -> {
			component.sizing(Sizing.fixed(layout.width()), Sizing.fixed(layout.height()));
			component.positioning(Positioning.absolute(layout.x(), layout.y()));
		});
	}

	public void resizeViewport(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
		syncFromSession();
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		super.update(delta, mouseX, mouseY);
		hoveredHandle = activeHandle != ResizeHandle.NONE
				? activeHandle
				: ResizeHandle.hitTest(session.layout(channel), mouseX, mouseY, HANDLE_THICKNESS);
		this.cursorStyle(cursorFor(hoveredHandle));
	}

	public boolean containsInteraction(double mouseX, double mouseY) {
		return ResizeHandle.hitTest(session.layout(channel), mouseX, mouseY, HANDLE_THICKNESS) != ResizeHandle.NONE;
	}

	@Override
	public boolean beginPointerInteraction(double mouseX, double mouseY, int button,
										   boolean shiftDown, boolean controlDown) {
		if (button != 0) return false;
		ResizeHandle handle = ResizeHandle.hitTest(session.layout(channel), mouseX, mouseY, HANDLE_THICKNESS);
		if (handle == ResizeHandle.NONE) return false;
		activeHandle = handle;
		dragStartLayout = session.layout(channel);
		dragStartMouseX = mouseX;
		dragStartMouseY = mouseY;
		geometryChanged = false;
		snappedX = false;
		snappedY = false;
		return true;
	}

	@Override
	public boolean dragPointer(double mouseX, double mouseY, int button) {
		if (button != 0 || activeHandle == ResizeHandle.NONE || dragStartLayout == null) {
			return false;
		}
		int totalX = (int) Math.round(mouseX - dragStartMouseX);
		int totalY = (int) Math.round(mouseY - dragStartMouseY);
		LayoutEditorMath.SnapResult result = activeHandle == ResizeHandle.MOVE
				? LayoutEditorMath.move(dragStartLayout, totalX, totalY, screenWidth, screenHeight,
						PixelLayout.DEFAULT_SAFE_MARGIN, SNAP_DISTANCE)
				: LayoutEditorMath.resize(dragStartLayout, totalX, totalY, activeHandle, screenWidth, screenHeight,
						PixelLayout.DEFAULT_SAFE_MARGIN, SNAP_DISTANCE);
		PixelLayout next = result.layout();
		snappedX = result.snappedX();
		snappedY = result.snappedY();
		if (!next.equals(session.layout(channel))) {
			session.setLayout(channel, next);
			syncFromSession();
			geometryChanged = true;
			changedCallback.run();
		}
		return true;
	}

	@Override
	public boolean endPointerInteraction(double mouseX, double mouseY, int button) {
		if (button == 0 && activeHandle != ResizeHandle.NONE) {
			clearPointerState();
			if (geometryChanged) {
				committedCallback.run();
			}
			geometryChanged = false;
			return true;
		}
		return false;
	}

	@Override
	public void cancelPointerInteraction() {
		if (activeHandle == ResizeHandle.NONE) return;
		if (geometryChanged && dragStartLayout != null) {
			session.setLayout(channel, dragStartLayout);
			syncFromSession();
			changedCallback.run();
		}
		clearPointerState();
		geometryChanged = false;
	}

	private void clearPointerState() {
		activeHandle = ResizeHandle.NONE;
		dragStartLayout = null;
		snappedX = false;
		snappedY = false;
	}

	@Override
	public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
		PixelLayout layout = session.layout(channel);
		Font renderer = Minecraft.getInstance().font;
		renderEngine.render(new ChatRenderContext(
				context,
				renderer,
				layout.x(),
				layout.y(),
				layout.width(),
				layout.height(),
				1.0f,
				renderEngine.state() == PreviewChatState.OPEN ? 1.0f : 0.0f,
				Component.translatable("chat_canvas.preview.input_placeholder"),
				session.text(channel),
				session.background(channel),
				session.playerColors(),
				session.mention(),
				Component.translatable("chat_canvas.preview.shouyun_name").getString(),
				channel == EditorChannel.PLAYER_CHAT
						? session.playerChatLayoutMode()
						: io.github.ikunkk02.chatcanvas.config.PlayerChatLayoutMode.CLASSIC,
				session.splitMessageMaxWidthRatio(),
				Minecraft.getInstance().options.textBackgroundOpacity().get()
		));
		drawEditorAssist(context, layout);
	}

	private List<PreviewChatMessage> previewMessages() {
		String shouyunName = Component.translatable("chat_canvas.preview.shouyun_name").getString();
		MutableComponent steve = Component.literal("Steve: ").withStyle(ChatFormatting.WHITE)
				.append(Component.literal("@" + shouyunName + " ").withStyle(ChatFormatting.WHITE))
				.append(Component.translatable("chat_canvas.preview.steve").withStyle(ChatFormatting.WHITE));
		MutableComponent alex = Component.literal("Alex: ").withStyle(ChatFormatting.WHITE)
				.append(Component.translatable("chat_canvas.preview.alex").withStyle(ChatFormatting.WHITE));
		MutableComponent shouyun = Component.literal(shouyunName).withStyle(ChatFormatting.WHITE)
				.append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
				.append(Component.translatable("chat_canvas.preview.shouyun_body").withStyle(ChatFormatting.WHITE));
		MutableComponent system = Component.translatable("chat_canvas.preview.system").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC);
		return List.of(
				new PreviewChatMessage(steve, previewIdentity("Steve")),
				new PreviewChatMessage(alex, previewIdentity("Alex")),
				new PreviewChatMessage(shouyun, previewIdentity(shouyunName), true),
				new PreviewChatMessage(system)
		);
	}

	private static PlayerChatIdentity previewIdentity(String name) {
		return new PlayerChatIdentity(
				UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)),
				name,
				true
		);
	}

	private void drawEditorAssist(OwoUIDrawContext context, PixelLayout layout) {
		if (hoveredHandle == ResizeHandle.NONE && activeHandle == ResizeHandle.NONE) return;
		boolean vanilla = ModernUiTheme.currentStyle()
				== io.github.ikunkk02.chatcanvas.editor.EditorUiStyle.VANILLA;
		int color = vanilla
				? activeHandle != ResizeHandle.NONE ? 0xFF8EB8FF : 0xCC70A7FF
				: activeHandle != ResizeHandle.NONE ? ModernUiTheme.ACCENT : ModernUiTheme.ACCENT_MUTED;
		context.fill(layout.x(), layout.y(), layout.right(), layout.y() + 1, color);
		context.fill(layout.x(), layout.bottom() - 1, layout.right(), layout.bottom(), color);
		context.fill(layout.x(), layout.y(), layout.x() + 1, layout.bottom(), color);
		context.fill(layout.right() - 1, layout.y(), layout.right(), layout.bottom(), color);

		int centerX = layout.x() + layout.width() / 2;
		int centerY = layout.y() + layout.height() / 2;
		drawHandle(context, layout.x(), layout.y(), color);
		drawHandle(context, centerX, layout.y(), color);
		drawHandle(context, layout.right(), layout.y(), color);
		drawHandle(context, layout.x(), centerY, color);
		drawHandle(context, layout.right(), centerY, color);
		drawHandle(context, layout.x(), layout.bottom(), color);
		drawHandle(context, centerX, layout.bottom(), color);
		drawHandle(context, layout.right(), layout.bottom(), color);
	}

	private void drawHandle(OwoUIDrawContext context, int centerX, int centerY, int color) {
		int half = HANDLE_SIZE / 2;
		int background = ModernUiTheme.currentStyle()
				== io.github.ikunkk02.chatcanvas.editor.EditorUiStyle.VANILLA
				? 0xE6151820 : ModernUiTheme.PANEL_ELEVATED;
		context.fill(centerX - half, centerY - half, centerX - half + HANDLE_SIZE,
				centerY - half + HANDLE_SIZE, background);
		context.fill(centerX - half + 1, centerY - half + 1, centerX - half + HANDLE_SIZE - 1,
				centerY - half + HANDLE_SIZE - 1, color);
	}

	private static CursorStyle cursorFor(ResizeHandle handle) {
		return switch (handle) {
			case MOVE -> CursorStyle.MOVE;
			case NORTH, SOUTH -> CursorStyle.VERTICAL_RESIZE;
			case WEST, EAST -> CursorStyle.HORIZONTAL_RESIZE;
			case NORTH_WEST, SOUTH_EAST -> CursorStyle.NWSE_RESIZE;
			case NORTH_EAST, SOUTH_WEST -> CursorStyle.NESW_RESIZE;
			default -> CursorStyle.NONE;
		};
	}

	public boolean dragging() {
		return activeHandle != ResizeHandle.NONE;
	}

	public boolean snappedX() {
		return snappedX;
	}

	public boolean snappedY() {
		return snappedY;
	}

	public PreviewChatState previewState() {
		return renderEngine.state();
	}

	public void setPreviewState(PreviewChatState state) {
		renderEngine.state(state);
	}

	public void dispose() {
		renderEngine.clearCache();
	}
}
