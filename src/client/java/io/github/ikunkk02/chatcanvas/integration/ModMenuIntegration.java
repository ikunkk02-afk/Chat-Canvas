package io.github.ikunkk02.chatcanvas.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.ikunkk02.chatcanvas.editor.EditorScreenFactory;

public final class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return EditorScreenFactory::create;
	}
}
