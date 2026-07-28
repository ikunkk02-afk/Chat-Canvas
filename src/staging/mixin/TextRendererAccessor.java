package io.github.ikunkk02.chatcanvas.mixin.client;


import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Font.class)
public interface FontAccessor {
	@Invoker("getObject /* FontStorage not available */")
	Object /* FontStorage not available */ chat_canvas$getObject /* FontStorage not available */(Identifier id);
}
