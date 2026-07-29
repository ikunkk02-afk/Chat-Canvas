package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.util.math.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public interface SuggestionWindowAccessor {
	@Accessor("area")
	Rect2i chat_canvas$area();
}
