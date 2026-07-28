package io.github.ikunkk02.chatcanvas;

import io.github.ikunkk02.chatcanvas.compat.ChatCanvasCompat;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.editor.EditorScreenFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ChatCanvasForge.MOD_ID)
public final class ChatCanvasForge {
    public static final String MOD_ID = "chat_canvas";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ChatCanvasForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(this::clientSetup);
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    private void clientSetup(FMLClientSetupEvent event) {
        LOGGER.info("Chat Canvas Forge 1.20.1 initializing");
        ChatCanvasConfig.initialize();
        ChatCanvasCompat.initialize();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        // Tick layout, emoji, voice managers
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.screen != null) return;
        if (event.getKey() == GLFW.GLFW_KEY_K && event.getAction() == GLFW.GLFW_PRESS) {
            if (ChatCanvasConfig.instance().enabled()) {
                EditorScreenFactory.open();
            }
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
