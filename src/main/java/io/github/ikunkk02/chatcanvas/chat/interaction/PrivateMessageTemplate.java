package io.github.ikunkk02.chatcanvas.chat.interaction;

import io.github.ikunkk02.chatcanvas.config.MentionConfig;

public final class PrivateMessageTemplate {
	private PrivateMessageTemplate() {
	}

	public static String apply(String template, String playerName) {
		String safeTemplate = template == null || !template.contains("{player}")
				? MentionConfig.DEFAULT_PRIVATE_MESSAGE_TEMPLATE
				: template.replace('\r', ' ').replace('\n', ' ');
		return safeTemplate.replace("{player}", playerName == null ? "" : playerName);
	}
}
