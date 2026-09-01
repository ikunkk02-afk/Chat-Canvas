package io.github.ikunkk02.chatcanvas;

import io.github.ikunkk02.chatcanvas.voice.VoskEncodingBootstrap;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

/**
 * Runs before Minecraft classes are initialized, so JNA cannot cache the
 * Windows platform charset before the Vosk UTF-8 contract is established.
 */
public final class ChatCanvasPreLaunch implements PreLaunchEntrypoint {
	@Override
	public void onPreLaunch() {
		VoskEncodingBootstrap.initialize();
	}
}
