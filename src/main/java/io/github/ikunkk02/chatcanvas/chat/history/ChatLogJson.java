package io.github.ikunkk02.chatcanvas.chat.history;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

final class ChatLogJson {
    static final Gson GSON = new GsonBuilder().create();

    private ChatLogJson() {}
}
