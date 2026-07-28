package io.github.ikunkk02.chatcanvas.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public final class EditorScreenFactory {
    private EditorScreenFactory() {}

    public static void open(@Nullable Screen parent) {
        Minecraft.getInstance().setScreen(new ChatCanvasEditorScreen(parent));
    }

    public static void open() {
        open(Minecraft.getInstance().screen);
    }
}
