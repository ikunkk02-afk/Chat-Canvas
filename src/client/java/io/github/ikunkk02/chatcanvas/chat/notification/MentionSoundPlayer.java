package io.github.ikunkk02.chatcanvas.chat.notification;

import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import io.github.ikunkk02.chatcanvas.config.MentionSound;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public final class MentionSoundPlayer {
	public void playConfigured(MentionConfig config) {
		play(config, false);
	}

	public void test(MentionConfig config) {
		play(config, true);
	}

	private void play(MentionConfig source, boolean test) {
		MentionConfig config = source == null ? MentionConfig.DEFAULT : source.sanitized();
		if (!test && !config.soundEnabled()) return;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || config.soundVolume() <= 0.0) return;
		SoundEvent sound = resolve(config.sound());
		client.getSoundManager().play(PositionedSoundInstance.ui(
				sound, (float) config.soundPitch(), (float) config.soundVolume()));
	}

	private static SoundEvent resolve(MentionSound sound) {
		return switch (sound == null ? MentionSound.EXPERIENCE_ORB : sound) {
			case EXPERIENCE_ORB -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
			case NOTE_PLING -> SoundEvents.BLOCK_NOTE_BLOCK_PLING.value();
			case AMETHYST -> SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
			case BUTTON_CLICK -> SoundEvents.UI_BUTTON_CLICK.value();
		};
	}
}
