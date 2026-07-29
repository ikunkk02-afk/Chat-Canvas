package io.github.ikunkk02.chatcanvas.chat.emoji;

public enum EmojiCategory {
	RECENT("chat_canvas.emoji.category.recent"),
	SMILEYS("chat_canvas.emoji.category.smileys"),
	PEOPLE("chat_canvas.emoji.category.people"),
	ANIMALS("chat_canvas.emoji.category.animals"),
	FOOD("chat_canvas.emoji.category.food"),
	ACTIVITIES("chat_canvas.emoji.category.activities"),
	TRAVEL("chat_canvas.emoji.category.travel"),
	OBJECTS("chat_canvas.emoji.category.objects"),
	SYMBOLS("chat_canvas.emoji.category.symbols"),
	HEARTS("chat_canvas.emoji.category.hearts");

	private final String translationKey;

	EmojiCategory(String translationKey) {
		this.translationKey = translationKey;
	}

	public String translationKey() {
		return translationKey;
	}
}
