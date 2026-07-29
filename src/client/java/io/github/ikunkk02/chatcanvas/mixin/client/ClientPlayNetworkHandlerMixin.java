package io.github.ikunkk02.chatcanvas.mixin.client;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerRosterTracker;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientGamePacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onPlayerList", at = @At("RETURN"))
	private void chat_canvas$refreshPlayerRosterOnList(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
		PlayerRosterTracker.refresh((ClientGamePacketListener) (Object) this);
	}

	@Inject(method = "onPlayerRemove", at = @At("RETURN"))
	private void chat_canvas$refreshPlayerRosterOnRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
		PlayerRosterTracker.refresh((ClientGamePacketListener) (Object) this);
	}
}
