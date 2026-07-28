package io.github.ikunkk02.chatcanvas.chat.history;
import io.github.ikunkk02.chatcanvas.chat.message.ChatCanvasMessage;

public interface ChatLogService extends AutoCloseable {
    void record(ChatCanvasMessage message);
    void switchContext(ChatLogContext context);
    void flush();
    @Override
    void close();
}
