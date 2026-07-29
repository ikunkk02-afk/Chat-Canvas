package io.github.ikunkk02.chatcanvas.mixin.client;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EditBox.class)
public interface TextFieldWidgetAccessor {
	@Accessor("maxLength")
	int chat_canvas$maxLength();

	@Accessor("selectionEnd")
	int chat_canvas$selectionEnd();
}
