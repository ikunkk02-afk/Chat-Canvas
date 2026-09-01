package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.gui.components.CommandSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestions.class)
public interface ChatInputSuggestorAccessor {
	@Accessor("suggestions")
	CommandSuggestions.SuggestionsList chat_canvas$window();
}
