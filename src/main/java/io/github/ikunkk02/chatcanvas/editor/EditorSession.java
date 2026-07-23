package io.github.ikunkk02.chatcanvas.editor;

import io.github.ikunkk02.chatcanvas.config.LayoutConfig;
import io.github.ikunkk02.chatcanvas.config.PixelLayout;

public final class EditorSession {
	private final LayoutConfig original;
	private final EditorHistory history;
	private PixelLayout layout;
	private int screenWidth;
	private int screenHeight;

	public EditorSession(LayoutConfig original, int screenWidth, int screenHeight) {
		this.original = original.sanitized();
		this.screenWidth = Math.max(1, screenWidth);
		this.screenHeight = Math.max(1, screenHeight);
		this.layout = this.original.toPixels(this.screenWidth, this.screenHeight);
		this.history = new EditorHistory(snapshot());
	}

	public PixelLayout layout() {
		return layout;
	}

	public LayoutConfig original() {
		return original;
	}

	public LayoutConfig snapshot() {
		return LayoutConfig.fromPixels(layout, screenWidth, screenHeight);
	}

	public void setLayout(PixelLayout value) {
		layout = value.constrained(screenWidth, screenHeight);
	}

	public void apply(LayoutConfig value) {
		layout = value.toPixels(screenWidth, screenHeight);
	}

	public void resizeViewport(int width, int height) {
		LayoutConfig ratios = snapshot();
		screenWidth = Math.max(1, width);
		screenHeight = Math.max(1, height);
		layout = ratios.toPixels(screenWidth, screenHeight);
	}

	public void restoreDefaults() {
		apply(LayoutConfig.DEFAULT);
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
