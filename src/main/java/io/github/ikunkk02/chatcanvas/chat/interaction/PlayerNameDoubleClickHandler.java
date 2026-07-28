package io.github.ikunkk02.chatcanvas.chat.interaction;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitbox;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitboxRegistry;
import io.github.ikunkk02.chatcanvas.config.ChatCanvasConfig;
import io.github.ikunkk02.chatcanvas.config.MentionConfig;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

/**
 * Double-click a player name in chat → insert "@PlayerName " into the input field.
 * Inline click tracking (no accessor dependencies).
 */
public final class PlayerNameDoubleClickHandler {
    private static final PlayerNameDoubleClickHandler INSTANCE = new PlayerNameDoubleClickHandler();

    private PlayerNameHitbox lastHit;
    private long lastClickMs;
    private Component feedback;
    private long feedbackUntilMs;

    private PlayerNameDoubleClickHandler() {}

    public static PlayerNameDoubleClickHandler instance() { return INSTANCE; }

    public boolean mouseClicked(ChatScreen screen, EditBox input, double mx, double my, int button) {
        MentionConfig config = ChatCanvasConfig.instance().mention();
        if (!config.doubleClickEnabled() || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            reset(); return false;
        }
        Optional<PlayerNameHitbox> hit = PlayerNameHitboxRegistry.findAt(mx, my);
        if (hit.isEmpty()) { reset(); return false; }
        if (input.isMouseOver(mx, my)) { reset(); return false; }

        PlayerNameHitbox target = hit.get();
        long now = System.currentTimeMillis();
        if (lastHit != null && lastHit.playerName().equals(target.playerName()) && now - lastClickMs < 500) {
            insertAt(input, "@" + target.playerName() + " ");
            reset();
            return true;
        }
        lastHit = target;
        lastClickMs = now;
        return true;
    }

    private static void insertAt(EditBox field, String text) {
        if (field == null) return;
        int pos = Math.min(field.getCursorPosition(), field.getValue().length());
        field.setValue(field.getValue().substring(0, pos) + text + field.getValue().substring(pos));
        field.setCursorPosition(pos + text.length());
    }

    public void reset() { lastHit = null; lastClickMs = 0; }

    public Component currentFeedback() {
        return feedback != null && System.currentTimeMillis() < feedbackUntilMs ? feedback : null;
    }
}
