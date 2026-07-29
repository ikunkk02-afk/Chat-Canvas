package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatScreen.class)
public interface ChatScreenAccessor {
	@Accessor("chatField")
	EditBox chat_canvas$chatField();
}
