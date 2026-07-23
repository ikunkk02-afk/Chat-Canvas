package io.github.ikunkk02.chatcanvas.ui;

import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.editor.EditorSession;
import io.github.ikunkk02.chatcanvas.editor.LayoutEditorMath;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class PreviewChatWidget extends BaseComponent {
	private static final int HANDLE_THICKNESS = 6;
	private static final int SNAP_DISTANCE = 7;
	private static final int CONTENT_PADDING = 10;

	private final EditorSession session;
	private final Runnable changedCallback;
	private final Runnable committedCallback;
	private final List<OrderedText> wrappedLines = new ArrayList<>();

	private ResizeHandle hoveredHandle = ResizeHandle.NONE;
	private ResizeHandle activeHandle = ResizeHandle.NONE;
	private PixelLayout dragStartLayout;
	private double dragStartMouseX;
	private double dragStartMouseY;
	private int cachedWrapWidth = -1;
	private boolean geometryChanged;
	private boolean snappedX;
	private boolean snappedY;
	private int screenWidth;
	private int screenHeight;

	public PreviewChatWidget(EditorSession session, int screenWidth, int screenHeight,
							 Runnable changedCallback, Runnable committedCallback) {
		this.session = session;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.changedCallback = changedCallback;
		this.committedCallback = committedCallback;
		PixelLayout layout = session.layout();
		this.sizing(Sizing.fixed(layout.width()), Sizing.fixed(layout.height()));
		this.positioning(Positioning.absolute(layout.x(), layout.y()));
	}

	public void syncFromSession() {
		PixelLayout layout = session.layout();
		if (this.width != layout.width() || this.height != layout.height()) {
			this.width = layout.width();
			this.height = layout.height();
			cachedWrapWidth = -1;
		}
		this.moveTo(layout.x(), layout.y());
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
				: ResizeHandle.hitTest(session.layout(), mouseX, mouseY, HANDLE_THICKNESS);
		this.cursorStyle(cursorFor(hoveredHandle));
	}

	@Override
	public boolean onMouseDown(double mouseX, double mouseY, int button) {
		if (button != 0) return super.onMouseDown(mouseX, mouseY, button);
		ResizeHandle handle = ResizeHandle.hitTest(session.layout(), mouseX, mouseY, HANDLE_THICKNESS);
		if (handle == ResizeHandle.NONE) return super.onMouseDown(mouseX, mouseY, button);
		activeHandle = handle;
		dragStartLayout = session.layout();
		dragStartMouseX = mouseX;
		dragStartMouseY = mouseY;
		geometryChanged = false;
		snappedX = false;
		snappedY = false;
		return true;
	}

	@Override
	public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
		if (button != 0 || activeHandle == ResizeHandle.NONE || dragStartLayout == null) {
			return super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
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
		if (!next.equals(session.layout())) {
			session.setLayout(next);
			syncFromSession();
			geometryChanged = true;
			changedCallback.run();
		}
		return true;
	}

	@Override
	public boolean onMouseUp(double mouseX, double mouseY, int button) {
		if (button == 0 && activeHandle != ResizeHandle.NONE) {
			activeHandle = ResizeHandle.NONE;
			dragStartLayout = null;
			snappedX = false;
			snappedY = false;
			if (geometryChanged) {
				committedCallback.run();
			}
			geometryChanged = false;
			return true;
		}
		return super.onMouseUp(mouseX, mouseY, button);
	}

	@Override
	public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
		PixelLayout layout = session.layout();
		ModernUiTheme.shadow(context, layout.x(), layout.y(), layout.width(), layout.height());
		ModernUiTheme.roundedRect(context, layout.x(), layout.y(), layout.width(), layout.height(), 6, 0xD9151820);
		ModernUiTheme.border(context, layout.x(), layout.y(), layout.width(), layout.height(), 0xAA63718A);

		drawHandleHighlight(context, layout, hoveredHandle);
		rebuildWrappedTextIfNeeded(layout.width() - CONTENT_PADDING * 2);
		context.enableScissor(
				layout.x() + CONTENT_PADDING,
				layout.y() + CONTENT_PADDING,
				layout.right() - CONTENT_PADDING,
				layout.bottom() - CONTENT_PADDING
		);
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		int textY = layout.bottom() - CONTENT_PADDING - renderer.fontHeight;
		for (int index = wrappedLines.size() - 1; index >= 0 && textY >= layout.y() + CONTENT_PADDING; index--) {
			context.drawText(renderer, wrappedLines.get(index), layout.x() + CONTENT_PADDING, textY, 0xFFF1F3F7, true);
			textY -= renderer.fontHeight + 2;
		}
		context.disableScissor();
	}

	private void rebuildWrappedTextIfNeeded(int wrapWidth) {
		if (wrapWidth == cachedWrapWidth) return;
		cachedWrapWidth = wrapWidth;
		wrappedLines.clear();
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		for (Text message : previewMessages()) {
			wrappedLines.addAll(renderer.wrapLines(message, Math.max(20, wrapWidth)));
		}
	}

	private List<Text> previewMessages() {
		MutableText steve = Text.literal("Steve: ").formatted(Formatting.AQUA)
				.append(Text.translatable("chat_canvas.preview.steve").formatted(Formatting.WHITE));
		MutableText alex = Text.literal("Alex: ").formatted(Formatting.LIGHT_PURPLE)
				.append(Text.translatable("chat_canvas.preview.alex").formatted(Formatting.WHITE));
		MutableText shouyun = Text.translatable("chat_canvas.preview.shouyun_name").formatted(Formatting.YELLOW)
				.append(Text.literal(": ").formatted(Formatting.YELLOW))
				.append(Text.literal("@Steve ").formatted(Formatting.GOLD))
				.append(Text.translatable("chat_canvas.preview.shouyun_body").formatted(Formatting.WHITE));
		MutableText system = Text.translatable("chat_canvas.preview.system").formatted(Formatting.GREEN, Formatting.ITALIC);
		return List.of(steve, alex, shouyun, system);
	}

	private void drawHandleHighlight(OwoUIDrawContext context, PixelLayout layout, ResizeHandle handle) {
		if (handle == ResizeHandle.NONE) return;
		int color = activeHandle != ResizeHandle.NONE ? 0xFF8EB8FF : 0xCC70A7FF;
		if (handle == ResizeHandle.MOVE) {
			ModernUiTheme.border(context, layout.x(), layout.y(), layout.width(), layout.height(), color);
			return;
		}
		if (handle.north()) context.fill(layout.x() + 5, layout.y() - 1, layout.right() - 5, layout.y() + 2, color);
		if (handle.south()) context.fill(layout.x() + 5, layout.bottom() - 2, layout.right() - 5, layout.bottom() + 1, color);
		if (handle.west()) context.fill(layout.x() - 1, layout.y() + 5, layout.x() + 2, layout.bottom() - 5, color);
		if (handle.east()) context.fill(layout.right() - 2, layout.y() + 5, layout.right() + 1, layout.bottom() - 5, color);
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
}
