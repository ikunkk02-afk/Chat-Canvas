package io.github.ikunkk02.chatcanvas;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chat Canvas — Forge 1.20.1
 * <p>
 * Pure client-side visual chat HUD editor. No server installation required.
 */
@Mod(ChatCanvasForge.MOD_ID)
public final class ChatCanvasForge {
    public static final String MOD_ID = "chat_canvas";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ChatCanvasForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus(); // OK for 1.20.1
            bus.addListener(this::clientSetup);
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    private void clientSetup(FMLClientSetupEvent event) {
        LOGGER.info("Chat Canvas Forge 1.20.1 initializing");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
