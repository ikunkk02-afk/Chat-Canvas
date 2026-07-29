package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatInputSuggestor.class)
public interface ChatInputSuggestorAccessor {
	@Accessor("window")
	ChatInputSuggestor.SuggestionWindow chat_canvas$window();
}
