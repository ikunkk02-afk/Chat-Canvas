package io.github.ikunkk02.chatcanvas.config;

import java.util.LinkedHashSet;
import java.util.Set;

public record CommandClipboardConfig(
		boolean enabled,
		boolean showPanelButton,
		CommandInsertMode insertMode,
		boolean allowDuplicates,
		boolean sensitiveWarning,
		int maxCommands,
		Set<String> hiddenPresetIds
) {
	public static final int MIN_COMMANDS = 20;
	public static final int MAX_COMMANDS = 1000;
	public static final CommandClipboardConfig DEFAULT = new CommandClipboardConfig(
			true, true, CommandInsertMode.REPLACE_INPUT, false, true, 200, Set.of());

	public CommandClipboardConfig {
		insertMode = insertMode == null ? CommandInsertMode.REPLACE_INPUT : insertMode;
		maxCommands = Math.max(MIN_COMMANDS, Math.min(MAX_COMMANDS, maxCommands));
		hiddenPresetIds = hiddenPresetIds == null
				? Set.of()
				: Set.copyOf(new LinkedHashSet<>(hiddenPresetIds.stream()
						.filter(value -> value != null && !value.isBlank()).toList()));
	}

	public CommandClipboardConfig sanitized() {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds);
	}

	public CommandClipboardConfig withEnabled(boolean value) {
		return new CommandClipboardConfig(value, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds);
	}

	public CommandClipboardConfig withShowPanelButton(boolean value) {
		return new CommandClipboardConfig(enabled, value, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds);
	}

	public CommandClipboardConfig withInsertMode(CommandInsertMode value) {
		return new CommandClipboardConfig(enabled, showPanelButton, value, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds);
	}

	public CommandClipboardConfig withAllowDuplicates(boolean value) {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, value,
				sensitiveWarning, maxCommands, hiddenPresetIds);
	}

	public CommandClipboardConfig withSensitiveWarning(boolean value) {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				value, maxCommands, hiddenPresetIds);
	}

	public CommandClipboardConfig withMaxCommands(int value) {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, value, hiddenPresetIds);
	}

	public CommandClipboardConfig withPresetHidden(String id, boolean hidden) {
		LinkedHashSet<String> values = new LinkedHashSet<>(hiddenPresetIds);
		if (hidden) values.add(id);
		else values.remove(id);
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, values);
	}
}
