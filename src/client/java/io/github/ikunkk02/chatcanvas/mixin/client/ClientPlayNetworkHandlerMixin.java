package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerRosterTracker;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "handlePlayerInfoUpdate", at = @At("RETURN"))
	private void chat_canvas$refreshPlayerRosterOnList(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
		PlayerRosterTracker.refresh((ClientPacketListener) (Object) this);
	}

	@Inject(method = "handlePlayerInfoRemove", at = @At("RETURN"))
	private void chat_canvas$refreshPlayerRosterOnRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
		PlayerRosterTracker.refresh((ClientPacketListener) (Object) this);
	}
}
