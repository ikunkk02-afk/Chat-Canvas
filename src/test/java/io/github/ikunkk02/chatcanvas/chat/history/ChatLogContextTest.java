package io.github.ikunkk02.chatcanvas.chat.history;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatLogContextTest {

    @Test
    void singleplayer_directoryName_containsSafeLabel() {
        ChatLogContext ctx = ChatLogContext.singleplayer("My World", "My World");
        String dir = ctx.directoryName();
        assertTrue(dir.startsWith("singleplayer_"));
        assertTrue(dir.contains("My_World"));
    }

    @Test
    void multiplayer_directoryName_hidesAddress() {
        ChatLogContext ctx = ChatLogContext.multiplayer("hypixel.net", "Hypixel");
        String dir = ctx.directoryName();
        assertTrue(dir.startsWith("multiplayer_"));
        assertTrue(dir.contains("Hypixel"));
        assertFalse(dir.contains("hypixel.net"));
    }

    @Test
    void removesIllegalPathCharacters() {
        ChatLogContext ctx = ChatLogContext.singleplayer("test", "hello/..\\world:cmd");
        String dir = ctx.directoryName();
        assertFalse(dir.contains("/"));
        assertFalse(dir.contains("\\"));
        assertFalse(dir.contains(".."));
        assertFalse(dir.contains(":"));
    }

    @Test
    void nullLabelBecomesEmpty() {
        ChatLogContext ctx = ChatLogContext.singleplayer("id", null);
        String dir = ctx.directoryName();
        assertNotNull(dir);
        assertFalse(dir.contains("__"));
    }

    @Test
    void sameKeyProducesSameContextId() {
        ChatLogContext a = ChatLogContext.multiplayer("same.server.com", "Label");
        ChatLogContext b = ChatLogContext.multiplayer("same.server.com", "Label2");
        assertEquals(a.contextId(), b.contextId());
    }

    @Test
    void differentKeysHaveDifferentContextId() {
        ChatLogContext a = ChatLogContext.multiplayer("server-a.com", "A");
        ChatLogContext b = ChatLogContext.multiplayer("server-b.com", "B");
        assertNotEquals(a.contextId(), b.contextId());
    }
}
