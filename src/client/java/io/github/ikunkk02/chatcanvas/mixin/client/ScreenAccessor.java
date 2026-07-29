package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
	@Invoker("addRenderableWidget")
	<T extends GuiEventListener & TabOrderedElement> T chat_canvas$addTabOrderedElementChild(T child);
}
