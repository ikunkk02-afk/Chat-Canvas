package io.github.ikunkk02.chatcanvas.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {
	@Invoker("rescaleChat")
	void chat_canvas$rescaleChat();
}
