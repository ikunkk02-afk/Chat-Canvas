package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerRosterTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onPlayerList", at = @At("RETURN"))
	private void chat_canvas$refreshPlayerRosterOnList(PlayerListS2CPacket packet, CallbackInfo ci) {
		PlayerRosterTracker.refresh((ClientPlayNetworkHandler) (Object) this);
	}

	@Inject(method = "onPlayerRemove", at = @At("RETURN"))
	private void chat_canvas$refreshPlayerRosterOnRemove(PlayerRemoveS2CPacket packet, CallbackInfo ci) {
		PlayerRosterTracker.refresh((ClientPlayNetworkHandler) (Object) this);
	}
}
