package io.github.ikunkk02.chatcanvas.chat.interaction;

import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitbox;
import io.github.ikunkk02.chatcanvas.chat.identity.PlayerNameHitboxRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Right-click context menu for player names in chat.
 * Returns an Action that the caller executes (simplified, no accessor deps).
 */
public final class PlayerQuickActionMenu {

    public enum Action { COPY_NAME, MENTION, WHISPER, NONE }

    private static final int WIDTH = 116, ROW_H = 20, PAD = 4;

    private int x, y;
    private PlayerNameHitbox target;
    private boolean visible = false;
    private Action selectedAction = Action.NONE;

    public Action mouseClicked(double mx, double my, int btn) {
        visible = false;
        selectedAction = Action.NONE;
        if (btn != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return Action.NONE;

        var hit = PlayerNameHitboxRegistry.findAt(mx, my);
        if (hit.isEmpty()) return Action.NONE;
        target = hit.get();
        x = Math.min((int) mx, Minecraft.getInstance().getWindow().getGuiScaledWidth() - WIDTH - 2);
        y = (int) my;
        visible = true;
        return Action.NONE;
    }

    public Action mouseReleased(double mx, double my, int btn) {
        if (!visible || btn != GLFW.GLFW_MOUSE_BUTTON_LEFT) { visible = false; return Action.NONE; }
        visible = false;
        int relY = (int) my - y - PAD;
        if (mx < x || mx > x + WIDTH || relY < 0) return Action.NONE;
        int row = relY / ROW_H;
        selectedAction = switch (row) {
            case 0 -> Action.COPY_NAME;
            case 1 -> Action.MENTION;
            case 2 -> Action.WHISPER;
            default -> Action.NONE;
        };
        return selectedAction;
    }

    public String resultText() {
        return switch (selectedAction) {
            case MENTION -> "@" + target.playerName() + " ";
            case WHISPER -> "/msg " + target.playerName() + " ";
            case COPY_NAME -> { Minecraft.getInstance().keyboardHandler.setClipboard(target.playerName()); yield null; }
            default -> null;
        };
    }

    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        if (!visible) return;
        ctx.fill(x, y, x + WIDTH, y + PAD * 2 + ROW_H * 3, 0xE0222230);
        ctx.renderOutline(x, y, WIDTH, PAD * 2 + ROW_H * 3, 0xFF4FC3F7);
        var font = Minecraft.getInstance().font;
        int ry = y + PAD;
        for (String key : new String[]{"copy", "mention", "whisper"}) {
            boolean hover = my >= ry && my < ry + ROW_H && mx >= x && mx < x + WIDTH;
            if (hover) ctx.fill(x, ry, x + WIDTH, ry + ROW_H, 0x504FC3F7);
            ctx.drawString(font, Component.translatable("chat_canvas.player." + key), x + 6, ry + 4, 0xFFFFFFFF);
            ry += ROW_H;
        }
    }
}
