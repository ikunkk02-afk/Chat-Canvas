package io.github.ikunkk02.chatcanvas.config;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public record CommandClipboardConfig(
		boolean enabled,
		boolean showPanelButton,
		CommandInsertMode insertMode,
		boolean allowDuplicates,
		boolean sensitiveWarning,
		int maxCommands,
		Set<String> hiddenPresetIds,
		boolean recordRecentCommands,
		int maxRecentCommands,
		boolean clearRecentOnDisconnect,
		Set<String> excludedCommandNames
) {
	public static final int MIN_COMMANDS = 20;
	public static final int MAX_COMMANDS = 1000;
	public static final int MIN_RECENT_COMMANDS = 10;
	public static final int MAX_RECENT_COMMANDS = 200;
	public static final Set<String> DEFAULT_EXCLUDED_COMMAND_NAMES = Set.of(
			"login", "register", "password", "passwd", "token", "auth");
	public static final CommandClipboardConfig DEFAULT = new CommandClipboardConfig(
			true, true, CommandInsertMode.REPLACE_INPUT, false, true, 200, Set.of(),
			true, 100, false, DEFAULT_EXCLUDED_COMMAND_NAMES);

	public CommandClipboardConfig(
			boolean enabled,
			boolean showPanelButton,
			CommandInsertMode insertMode,
			boolean allowDuplicates,
			boolean sensitiveWarning,
			int maxCommands,
			Set<String> hiddenPresetIds
	) {
		this(enabled, showPanelButton, insertMode, allowDuplicates, sensitiveWarning,
				maxCommands, hiddenPresetIds, true, 100, false,
				DEFAULT_EXCLUDED_COMMAND_NAMES);
	}

	public CommandClipboardConfig {
		insertMode = insertMode == null ? CommandInsertMode.REPLACE_INPUT : insertMode;
		maxCommands = Math.max(MIN_COMMANDS, Math.min(MAX_COMMANDS, maxCommands));
		maxRecentCommands = Math.max(MIN_RECENT_COMMANDS,
				Math.min(MAX_RECENT_COMMANDS, maxRecentCommands));
		hiddenPresetIds = hiddenPresetIds == null
				? Set.of()
				: Set.copyOf(new LinkedHashSet<>(hiddenPresetIds.stream()
						.filter(value -> value != null && !value.isBlank()).toList()));
		excludedCommandNames = normalizeCommandNames(excludedCommandNames);
	}

	public CommandClipboardConfig sanitized() {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds, recordRecentCommands,
				maxRecentCommands, clearRecentOnDisconnect, excludedCommandNames);
	}

	public CommandClipboardConfig withEnabled(boolean value) {
		return new CommandClipboardConfig(value, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds, recordRecentCommands,
				maxRecentCommands, clearRecentOnDisconnect, excludedCommandNames);
	}

	public CommandClipboardConfig withShowPanelButton(boolean value) {
		return new CommandClipboardConfig(enabled, value, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds, recordRecentCommands,
				maxRecentCommands, clearRecentOnDisconnect, excludedCommandNames);
	}

	public CommandClipboardConfig withInsertMode(CommandInsertMode value) {
		return new CommandClipboardConfig(enabled, showPanelButton, value, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds, recordRecentCommands,
				maxRecentCommands, clearRecentOnDisconnect, excludedCommandNames);
	}

	public CommandClipboardConfig withAllowDuplicates(boolean value) {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, value,
				sensitiveWarning, maxCommands, hiddenPresetIds, recordRecentCommands,
				maxRecentCommands, clearRecentOnDisconnect, excludedCommandNames);
	}

	public CommandClipboardConfig withSensitiveWarning(boolean value) {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				value, maxCommands, hiddenPresetIds, recordRecentCommands,
				maxRecentCommands, clearRecentOnDisconnect, excludedCommandNames);
	}

	public CommandClipboardConfig withMaxCommands(int value) {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, value, hiddenPresetIds, recordRecentCommands,
				maxRecentCommands, clearRecentOnDisconnect, excludedCommandNames);
	}

	public CommandClipboardConfig withPresetHidden(String id, boolean hidden) {
		LinkedHashSet<String> values = new LinkedHashSet<>(hiddenPresetIds);
		if (hidden) values.add(id);
		else values.remove(id);
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, values, recordRecentCommands,
				maxRecentCommands, clearRecentOnDisconnect, excludedCommandNames);
	}

	public CommandClipboardConfig withRecordRecentCommands(boolean value) {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds, value,
				maxRecentCommands, clearRecentOnDisconnect, excludedCommandNames);
	}

	public CommandClipboardConfig withMaxRecentCommands(int value) {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds, recordRecentCommands,
				value, clearRecentOnDisconnect, excludedCommandNames);
	}

	public CommandClipboardConfig withClearRecentOnDisconnect(boolean value) {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds, recordRecentCommands,
				maxRecentCommands, value, excludedCommandNames);
	}

	public CommandClipboardConfig withExcludedCommandNames(Set<String> values) {
		return new CommandClipboardConfig(enabled, showPanelButton, insertMode, allowDuplicates,
				sensitiveWarning, maxCommands, hiddenPresetIds, recordRecentCommands,
				maxRecentCommands, clearRecentOnDisconnect, values);
	}

	private static Set<String> normalizeCommandNames(Set<String> values) {
		Set<String> source = values == null
				? DEFAULT_EXCLUDED_COMMAND_NAMES : values;
		LinkedHashSet<String> normalized = new LinkedHashSet<>();
		for (String value : source) {
			if (value == null) continue;
			String command = value.strip().toLowerCase(Locale.ROOT);
			while (command.startsWith("/")) command = command.substring(1);
			if (!command.isBlank()) normalized.add(command);
		}
		return Set.copyOf(normalized);
	}
}
