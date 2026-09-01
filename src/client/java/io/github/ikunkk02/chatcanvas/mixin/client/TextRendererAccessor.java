package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;

/** Retained as a named hook for integrations; 26.1 exposes no FontStorage accessor. */
@Mixin(Font.class)
public interface TextRendererAccessor {
}
