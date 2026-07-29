package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Font.class)
public interface TextRendererAccessor {
	@Invoker("getFontStorage")
	FontStorage chat_canvas$getFontStorage(Identifier id);
}
