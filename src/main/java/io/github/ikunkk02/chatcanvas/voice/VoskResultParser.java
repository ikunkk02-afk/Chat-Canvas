package io.github.ikunkk02.chatcanvas.voice;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Parses the Java Unicode strings returned by Vosk directly. No byte or
 * platform-charset conversion belongs in this layer.
 */
public final class VoskResultParser {
	private final Gson gson;

	public VoskResultParser(Gson gson) {
		this.gson = gson;
	}

	public String parsePartial(String json) {
		if (json == null || json.isBlank()) return "";
		try {
			VoskPartialResponse response =
					gson.fromJson(json, VoskPartialResponse.class);
			return response != null && response.partial() != null
					? response.partial() : "";
		} catch (JsonParseException | IllegalStateException ignored) {
			return "";
		}
	}

	public String parseResult(String json) {
		return parseFinal(json);
	}

	public String parseFinal(String json) {
		if (json == null || json.isBlank()) return "";
		try {
			VoskFinalResponse response =
					gson.fromJson(json, VoskFinalResponse.class);
			return response != null && response.text() != null
					? response.text() : "";
		} catch (JsonParseException | IllegalStateException ignored) {
			return "";
		}
	}

	private record VoskPartialResponse(String partial) {
	}

	private record VoskFinalResponse(String text) {
	}
}
