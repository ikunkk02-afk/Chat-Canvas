package io.github.ikunkk02.chatcanvas;

import io.github.ikunkk02.chatcanvas.voice.VoskEncodingBootstrap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = ChatCanvas.MOD_ID, dist = Dist.CLIENT)
public final class ChatCanvas {
	public static final String MOD_ID = "chatcanvas";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public ChatCanvas(IEventBus modBus, ModContainer modContainer) {
		VoskEncodingBootstrap.initialize();
		modContainer.registerExtensionPoint(IConfigScreenFactory.class,
				(container, parent) -> io.github.ikunkk02.chatcanvas.editor.EditorScreenFactory.create(parent));
		ChatCanvasClient.initialize(modBus);
		LOGGER.info("Initializing Chat Canvas");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
