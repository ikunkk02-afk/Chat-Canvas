package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
	@Invoker("addSelectableChild")
	<T extends net.minecraft.client.gui.narration.NarratableEntry & net.minecraft.client.gui.components.Renderable> T chat_canvas$addSelectableChild(T child);
}
