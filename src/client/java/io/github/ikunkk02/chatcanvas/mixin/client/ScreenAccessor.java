package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
	@Invoker("addSelectableChild")
	<T extends GuiEventListener & Selectable> T chat_canvas$addSelectableChild(T child);
}
