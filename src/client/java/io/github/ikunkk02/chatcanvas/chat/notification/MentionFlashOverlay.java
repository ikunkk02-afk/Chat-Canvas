package io.github.ikunkk02.chatcanvas.chat.notification;

import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.Util;
import net.minecraft.util.Mth;

public final class MentionFlashOverlay {
	private long startedAtMs;
	private long endsAtMs;
	private int color;
	private double maximumOpacity;

	public void register() {
		HudLayer.EVENT.register((context, tickCounter) -> render(context));
	}

	public void trigger(MentionConfig source) {
		MentionConfig config = source.sanitized();
		if (!config.flashEnabled() || config.flashOpacity() <= 0.0) return;
		long now = Util.getMillis();
		startedAtMs = now;
		endsAtMs = now + config.flashDurationMs();
		color = config.flashColor();
		maximumOpacity = config.flashOpacity();
	}

	public void clear() {
		startedAtMs = 0L;
		endsAtMs = 0L;
		maximumOpacity = 0.0;
	}

	private void render(net.minecraft.client.gui.GuiGraphicsExtractor context) {
		Minecraft client = Minecraft.getInstance();
		if (client.world == null) return;
		if (client.screen != null && !(client.screen instanceof ChatScreen)) return;
		long now = Util.getMillis();
		if (now >= endsAtMs || endsAtMs <= startedAtMs) return;
		double duration = Math.max(1.0, endsAtMs - startedAtMs);
		double remaining = Mth.clamp((endsAtMs - now) / duration, 0.0, 1.0);
		double eased = 1.0 - Math.pow(1.0 - remaining, 2.0);
		int alpha = (int) Math.round(255.0 * Math.min(0.6, maximumOpacity) * eased);
		int argb = (alpha << 24) | (color & 0xFFFFFF);
		context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), argb);
	}
}
