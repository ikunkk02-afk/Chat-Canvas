package io.github.ikunkk02.chatcanvas.chat.history;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;

public record ChatLogContext(Type type, String contextId, String label) {

    public enum Type {
        SINGLEPLAYER("singleplayer"),
        MULTIPLAYER("multiplayer");

        private final String prefix;

        Type(String prefix) { this.prefix = prefix; }

        @Override
        public String toString() { return prefix; }
    }

    private static final Pattern ILLEGAL_PATH_CHARS =
            Pattern.compile("[^\\w\\p{L}._-]+");

    public ChatLogContext {
        if (type == null) throw new IllegalArgumentException("type");
        if (contextId == null || contextId.isBlank())
            throw new IllegalArgumentException("contextId");
        label = label == null ? "" : label;
    }

    public static ChatLogContext singleplayer(String worldId, String label) {
        String stable = stableId("singleplayer:" + worldId);
        return new ChatLogContext(Type.SINGLEPLAYER, stable, label);
    }

    public static ChatLogContext multiplayer(String serverAddress, String label) {
        String stable = stableId("multiplayer:" + serverAddress.toLowerCase(Locale.ROOT));
        return new ChatLogContext(Type.MULTIPLAYER, stable, label);
    }

    public String directoryName() {
        String sanitized = sanitizeLabel(label);
        String hashPart = contextId.length() > 16
                ? contextId.substring(0, 16) : contextId;
        if (sanitized.isEmpty()) {
            return type.toString() + "_" + hashPart;
        }
        return type.toString() + "_" + sanitized + "_" + hashPart;
    }

    private static String sanitizeLabel(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String result = ILLEGAL_PATH_CHARS.matcher(raw.strip()).replaceAll("_");
        result = result.replace("..", "_")
                .replaceAll("_+", "_")
                .replaceAll("^[._]+|[._]+$", "");
        if (result.length() > 24) {
            result = result.substring(0, 24).replaceAll("[._]+$", "");
        }
        return result;
    }

    private static String stableId(String seed) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Override
    public String toString() {
        return type + " [" + directoryName() + "]";
    }
}
