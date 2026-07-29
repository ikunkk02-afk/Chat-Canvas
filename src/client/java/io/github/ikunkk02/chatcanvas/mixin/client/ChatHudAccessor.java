package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatHud.class)
public interface ChatHudAccessor {
	@Invoker("refresh")
	void chat_canvas$refresh();
}
