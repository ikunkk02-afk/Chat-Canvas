package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.ChatCanvasSettings;
import io.github.ikunkk02.chatcanvas.config.ChatBackgroundConfig;
import io.github.ikunkk02.chatcanvas.config.ChatTextConfig;
import io.github.ikunkk02.chatcanvas.config.LayoutConfig;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;
import io.github.ikunkk02.chatcanvas.config.RecentColorStore;

public final class EditorSession {
	private final EditorSnapshot original;
	private final EditorHistory history;
	private final RecentColorStore recentColors;
	private PixelLayout layout;
	private ChatTextConfig text;
	private ChatBackgroundConfig background;
	private int screenWidth;
	private int screenHeight;

	public EditorSession(LayoutConfig original, int screenWidth, int screenHeight) {
		this(new ChatCanvasSettings(original, ChatTextConfig.DEFAULT), screenWidth, screenHeight);
	}

	public EditorSession(ChatCanvasSettings original, int screenWidth, int screenHeight) {
		ChatCanvasSettings safe = original.sanitized();
		this.original = new EditorSnapshot(safe.layout(), safe.text(), safe.background());
		this.screenWidth = Math.max(1, screenWidth);
		this.screenHeight = Math.max(1, screenHeight);
		this.layout = this.original.layout().toPixels(this.screenWidth, this.screenHeight);
		this.text = this.original.text();
		this.background = this.original.background();
		this.recentColors = new RecentColorStore(safe.recentColors());
		this.history = new EditorHistory(snapshot());
	}

	public PixelLayout layout() {
		return layout;
	}

	public ChatTextConfig text() {
		return text;
	}

	public ChatBackgroundConfig background() {
		return background;
	}

	public RecentColorStore recentColors() {
		return recentColors;
	}

	public EditorSnapshot original() {
		return original;
	}

	public EditorSnapshot snapshot() {
		return new EditorSnapshot(
				LayoutConfig.fromPixels(layout, screenWidth, screenHeight),
				text,
				background
		);
	}

	public ChatCanvasSettings settings() {
		EditorSnapshot snapshot = snapshot();
		return new ChatCanvasSettings(
				snapshot.layout(),
				snapshot.text(),
				snapshot.background(),
				recentColors.colors()
		);
	}

	public void setLayout(PixelLayout value) {
		layout = value.constrained(screenWidth, screenHeight);
	}

	public void apply(LayoutConfig value) {
		layout = value.toPixels(screenWidth, screenHeight);
	}

	public void setText(ChatTextConfig value) {
		text = value.sanitized();
	}

	public void setBackground(ChatBackgroundConfig value) {
		background = value.sanitized();
	}

	public void apply(EditorSnapshot value) {
		layout = value.layout().toPixels(screenWidth, screenHeight);
		text = value.text();
		background = value.background();
	}

	public void resizeViewport(int width, int height) {
		LayoutConfig ratios = snapshot().layout();
		screenWidth = Math.max(1, width);
		screenHeight = Math.max(1, height);
		layout = ratios.toPixels(screenWidth, screenHeight);
	}

	public void restoreLayoutDefaults() {
		apply(LayoutConfig.DEFAULT);
		commit();
	}

	public void restoreTextDefaults() {
		setText(ChatTextConfig.DEFAULT);
		commit();
	}

	public void restoreBackgroundDefaults() {
		setBackground(ChatBackgroundConfig.DEFAULT);
		commit();
	}

	public void commit() {
		history.record(snapshot());
	}

	public boolean undo() {
		return history.undo().map(state -> {
			apply(state);
			return true;
		}).orElse(false);
	}

	public boolean redo() {
		return history.redo().map(state -> {
			apply(state);
			return true;
		}).orElse(false);
	}

	public boolean canUndo() {
		return history.canUndo();
	}

	public boolean canRedo() {
		return history.canRedo();
	}

	public EditorHistory history() {
		return history;
	}
}
