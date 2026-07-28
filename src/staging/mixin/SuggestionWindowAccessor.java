package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSuggestions.SuggestionsList.class)
public interface SuggestionWindowAccessor {
	@Accessor("area")
	ScreenRectangle chat_canvas$area();
}
