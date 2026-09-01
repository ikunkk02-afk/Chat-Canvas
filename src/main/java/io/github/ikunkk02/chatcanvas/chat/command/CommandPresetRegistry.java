package io.github.ikunkk02.chatcanvas.chat.command;

import java.util.List;

public final class CommandPresetRegistry {
	public record Preset(String id, String titleKey, String command, String categoryKey) {
	}

	private static final List<Preset> PRESETS = List.of(
			p("gamemode_survival", "/gamemode survival @s"),
			p("gamemode_creative", "/gamemode creative @s"),
			p("gamemode_spectator", "/gamemode spectator @s"),
			p("time_day", "/time set day"),
			p("time_night", "/time set night"),
			p("weather_clear", "/weather clear"),
			p("weather_rain", "/weather rain"),
			p("keep_inventory_on", "/gamerule keepInventory true"),
			p("keep_inventory_off", "/gamerule keepInventory false"),
			p("difficulty_peaceful", "/difficulty peaceful"),
			p("difficulty_easy", "/difficulty easy"),
			p("difficulty_normal", "/difficulty normal"),
			p("difficulty_hard", "/difficulty hard"),
			p("spawnpoint", "/spawnpoint"),
			p("kill_self", "/kill @s")
	);

	private CommandPresetRegistry() {
	}

	public static List<Preset> all() {
		return PRESETS;
	}

	private static Preset p(String id, String command) {
		return new Preset(id, "chat_canvas.command.preset." + id, command,
				"chat_canvas.command.category.preset");
	}
}
