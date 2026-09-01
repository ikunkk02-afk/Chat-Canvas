package io.github.ikunkk02.chatcanvas.chat.emoji;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EmojiRegistry {
	private static final EmojiRegistry INSTANCE = new EmojiRegistry(defaultEntries());

	private final List<EmojiEntry> entries;
	private final Map<String, EmojiEntry> byUnicode;
	private final Map<EmojiCategory, List<EmojiEntry>> byCategory;
	private final Map<String, String> searchBlobs;

	EmojiRegistry(List<EmojiEntry> source) {
		Map<String, EmojiEntry> unique = new LinkedHashMap<>();
		for (EmojiEntry entry : source) unique.putIfAbsent(entry.unicode(), entry);
		byUnicode = Collections.unmodifiableMap(unique);
		entries = List.copyOf(unique.values());
		Map<EmojiCategory, List<EmojiEntry>> categories =
				new EnumMap<>(EmojiCategory.class);
		for (EmojiCategory category : EmojiCategory.values()) {
			categories.put(category, new ArrayList<>());
		}
		Map<String, String> blobs = new LinkedHashMap<>();
		for (EmojiEntry entry : entries) {
			categories.get(entry.category()).add(entry);
			StringBuilder search = new StringBuilder()
					.append(entry.unicode()).append(' ')
					.append(entry.chineseName()).append(' ')
					.append(entry.englishName());
			for (String keyword : entry.keywords()) search.append(' ').append(keyword);
			blobs.put(entry.unicode(), normalize(search.toString()));
		}
		Map<EmojiCategory, List<EmojiEntry>> immutable =
				new EnumMap<>(EmojiCategory.class);
		categories.forEach((category, values) ->
				immutable.put(category, List.copyOf(values)));
		byCategory = Collections.unmodifiableMap(immutable);
		searchBlobs = Collections.unmodifiableMap(blobs);
	}

	public static EmojiRegistry instance() {
		return INSTANCE;
	}

	public List<EmojiEntry> entries() {
		return entries;
	}

	public EmojiEntry find(String unicode) {
		return byUnicode.get(unicode);
	}

	public boolean contains(String unicode) {
		return byUnicode.containsKey(unicode);
	}

	public List<EmojiEntry> category(EmojiCategory category) {
		return category == null ? List.of()
				: byCategory.getOrDefault(category, List.of());
	}

	public List<EmojiEntry> search(String query) {
		String needle = normalize(query);
		if (needle.isEmpty()) return entries;
		return entries.stream()
				.filter(entry -> searchBlobs.get(entry.unicode()).contains(needle))
				.toList();
	}

	static String normalize(String value) {
		if (value == null || value.isBlank()) return "";
		return Normalizer.normalize(value, Normalizer.Form.NFKC)
				.toLowerCase(Locale.ROOT).trim();
	}

	private static EmojiEntry e(
			String unicode, String chinese, String english,
			EmojiCategory category, String... keywords) {
		return new EmojiEntry(unicode, chinese, english, List.of(keywords), category);
	}

	private static List<EmojiEntry> defaultEntries() {
		List<EmojiEntry> values = new ArrayList<>();
		EmojiCategory s = EmojiCategory.SMILEYS;
		values.add(e("😀", "笑脸", "grinning face", s, "开心", "高兴", "笑", "happy", "smile"));
		values.add(e("😃", "大眼笑脸", "grinning face with big eyes", s, "开心", "笑", "happy"));
		values.add(e("😄", "眯眼笑脸", "grinning squinting face", s, "开心", "笑", "happy"));
		values.add(e("😁", "露齿笑", "beaming face", s, "开心", "笑", "grin"));
		values.add(e("😆", "大笑", "grinning squinting face", s, "大笑", "laugh"));
		values.add(e("😅", "流汗笑脸", "grinning face with sweat", s, "尴尬", "汗", "sweat"));
		values.add(e("😂", "笑哭", "face with tears of joy", s, "笑哭", "joy", "tears"));
		values.add(e("🤣", "笑得打滚", "rolling on the floor laughing", s, "爆笑", "rofl"));
		values.add(e("😊", "微笑", "smiling face", s, "开心", "害羞", "smile"));
		values.add(e("😇", "天使笑脸", "smiling face with halo", s, "天使", "halo"));
		values.add(e("🙂", "稍微微笑", "slightly smiling face", s, "微笑", "smile"));
		values.add(e("🙃", "倒脸", "upside-down face", s, "反话", "upside down"));
		values.add(e("😉", "眨眼", "winking face", s, "眨眼", "wink"));
		values.add(e("😌", "释然", "relieved face", s, "轻松", "relieved"));
		values.add(e("😍", "花痴", "smiling face with heart-eyes", s, "喜欢", "爱", "heart eyes"));
		values.add(e("🥰", "爱心笑脸", "smiling face with hearts", s, "喜欢", "爱", "hearts"));
		values.add(e("😘", "飞吻", "face blowing a kiss", s, "亲亲", "kiss"));
		values.add(e("😋", "好吃", "face savoring food", s, "美味", "yum"));
		values.add(e("😎", "墨镜笑脸", "smiling face with sunglasses", s, "酷", "cool"));
		values.add(e("🤓", "书呆子", "nerd face", s, "眼镜", "nerd"));
		values.add(e("🤔", "思考", "thinking face", s, "想", "考虑", "think"));
		values.add(e("🤨", "挑眉", "face with raised eyebrow", s, "怀疑", "doubt"));
		values.add(e("😐", "无语", "neutral face", s, "平静", "neutral"));
		values.add(e("😑", "面无表情", "expressionless face", s, "无奈", "expressionless"));
		values.add(e("😶", "沉默", "face without mouth", s, "无话", "silent"));
		values.add(e("😏", "得意", "smirking face", s, "坏笑", "smirk"));
		values.add(e("😒", "不高兴", "unamused face", s, "无聊", "unamused"));
		values.add(e("🙄", "翻白眼", "face with rolling eyes", s, "白眼", "roll eyes"));
		values.add(e("😬", "龇牙", "grimacing face", s, "尴尬", "grimace"));
		values.add(e("😔", "忧伤", "pensive face", s, "难过", "sad"));
		values.add(e("😢", "哭", "crying face", s, "难过", "cry"));
		values.add(e("😭", "大哭", "loudly crying face", s, "伤心", "cry"));
		values.add(e("😡", "生气", "enraged face", s, "愤怒", "angry"));
		values.add(e("🤬", "咒骂", "face with symbols on mouth", s, "愤怒", "swear"));
		values.add(e("😱", "惊恐", "face screaming in fear", s, "害怕", "scream"));
		values.add(e("😨", "害怕", "fearful face", s, "恐惧", "fear"));
		values.add(e("😴", "睡觉", "sleeping face", s, "困", "sleep"));
		values.add(e("🤢", "恶心", "nauseated face", s, "不舒服", "sick"));
		values.add(e("🤮", "呕吐", "face vomiting", s, "吐", "vomit"));
		values.add(e("💀", "骷髅", "skull", s, "死亡", "死", "death"));
		values.add(e("👻", "幽灵", "ghost", s, "鬼", "spooky"));
		values.add(e("☺️", "经典微笑", "smiling face", s, "开心", "放松", "smile", "relaxed"));
		values.add(e("😗", "亲吻", "kissing face", s, "亲亲", "kiss"));
		values.add(e("😙", "眯眼亲吻", "kissing face with smiling eyes", s, "亲亲", "开心", "kiss"));
		values.add(e("😚", "闭眼亲吻", "kissing face with closed eyes", s, "亲亲", "害羞", "kiss"));
		values.add(e("🤗", "抱抱", "smiling face with open hands", s, "拥抱", "喜欢", "hug"));
		values.add(e("🤩", "星星眼", "star-struck", s, "喜欢", "兴奋", "star eyes"));
		values.add(e("😛", "吐舌", "face with tongue", s, "调皮", "搞怪", "tongue"));
		values.add(e("😜", "眨眼吐舌", "winking face with tongue", s, "调皮", "搞怪", "wink tongue"));
		values.add(e("😝", "眯眼吐舌", "squinting face with tongue", s, "鬼脸", "搞怪", "tongue"));
		values.add(e("🤪", "滑稽脸", "zany face", s, "搞怪", "疯狂", "zany", "silly"));
		values.add(e("🤭", "捂嘴笑", "face with hand over mouth", s, "偷笑", "害羞", "giggle"));
		values.add(e("🤫", "安静", "shushing face", s, "嘘", "保密", "quiet", "shush"));
		values.add(e("🤥", "说谎", "lying face", s, "鼻子", "谎话", "lie"));
		values.add(e("🤐", "闭嘴", "zipper-mouth face", s, "保密", "无语", "zipper", "silent"));
		values.add(e("🧐", "单片眼镜", "face with monocle", s, "观察", "怀疑", "monocle"));
		values.add(e("😕", "困惑", "confused face", s, "疑问", "不懂", "confused"));
		values.add(e("🙁", "稍微皱眉", "slightly frowning face", s, "不开心", "难过", "frown"));
		values.add(e("☹️", "皱眉", "frowning face", s, "不开心", "难过", "frown"));
		values.add(e("😟", "担心", "worried face", s, "忧虑", "害怕", "worried"));
		values.add(e("😳", "脸红", "flushed face", s, "害羞", "惊讶", "flushed"));
		values.add(e("😮", "张嘴惊讶", "face with open mouth", s, "震惊", "惊讶", "surprised"));
		values.add(e("😯", "安静惊讶", "hushed face", s, "震惊", "惊讶", "hushed"));
		values.add(e("😲", "目瞪口呆", "astonished face", s, "震惊", "惊讶", "astonished"));
		values.add(e("😧", "痛苦惊讶", "anguished face", s, "震惊", "害怕", "anguished"));
		values.add(e("😦", "张嘴皱眉", "frowning face with open mouth", s, "惊讶", "难过", "frown"));
		values.add(e("😰", "冷汗焦虑", "anxious face with sweat", s, "害怕", "紧张", "cold sweat"));
		values.add(e("😥", "失望流汗", "sad but relieved face", s, "失望", "冷汗", "relieved"));
		values.add(e("😓", "低头流汗", "downcast face with sweat", s, "累", "流汗", "sweat"));
		values.add(e("🤯", "爆炸头", "exploding head", s, "震惊", "爆炸", "mind blown"));
		values.add(e("😤", "鼻孔冒气", "face with steam from nose", s, "生气", "得意", "steam"));
		values.add(e("😠", "愤怒", "angry face", s, "生气", "不满", "angry"));
		values.add(e("🥺", "恳求", "pleading face", s, "委屈", "可怜", "please", "puppy eyes"));
		values.add(e("😞", "失望", "disappointed face", s, "难过", "沮丧", "disappointed"));
		values.add(e("😖", "纠结", "confounded face", s, "痛苦", "困扰", "confounded"));
		values.add(e("😣", "忍耐", "persevering face", s, "坚持", "痛苦", "persevere"));
		values.add(e("😩", "疲惫", "weary face", s, "累", "沮丧", "weary"));
		values.add(e("😫", "累坏了", "tired face", s, "疲劳", "困", "tired"));
		values.add(e("🥱", "打哈欠", "yawning face", s, "困", "无聊", "yawn"));
		values.add(e("😪", "困倦", "sleepy face", s, "困", "疲惫", "sleepy"));
		values.add(e("🤤", "流口水", "drooling face", s, "睡觉", "好吃", "drool"));
		values.add(e("😵", "晕倒", "dizzy face", s, "晕", "眼冒金星", "dizzy"));
		values.add(e("🥴", "晕乎乎", "woozy face", s, "晕", "迷糊", "woozy"));
		values.add(e("🤕", "头部受伤", "face with head-bandage", s, "生病", "受伤", "bandage"));
		values.add(e("🤒", "发烧", "face with thermometer", s, "生病", "体温", "fever"));
		values.add(e("😷", "戴口罩", "face with medical mask", s, "生病", "口罩", "mask"));
		values.add(e("🤧", "打喷嚏", "sneezing face", s, "生病", "感冒", "sneeze"));
		values.add(e("🥵", "热脸", "hot face", s, "炎热", "流汗", "hot"));
		values.add(e("🥶", "冰冻脸", "cold face", s, "寒冷", "结冰", "cold", "freeze"));
		values.add(e("😈", "微笑恶魔", "smiling face with horns", s, "恶魔", "坏", "devil"));
		values.add(e("👿", "愤怒恶魔", "angry face with horns", s, "恶魔", "生气", "devil"));
		values.add(e("🤑", "钱眼", "money-mouth face", s, "金钱", "发财", "money"));
		values.add(e("🥳", "派对脸", "partying face", s, "庆祝", "生日", "party"));
		values.add(e("🤠", "牛仔脸", "cowboy hat face", s, "牛仔", "帽子", "cowboy"));
		values.add(e("🤡", "小丑脸", "clown face", s, "小丑", "搞怪", "clown"));
		values.add(e("🥸", "伪装脸", "disguised face", s, "伪装", "眼镜", "disguise"));

		EmojiCategory p = EmojiCategory.PEOPLE;
		values.add(e("👍", "赞", "thumbs up", p, "好", "同意", "yes", "like"));
		values.add(e("👎", "踩", "thumbs down", p, "不好", "不同意", "no", "dislike"));
		values.add(e("👏", "鼓掌", "clapping hands", p, "掌声", "clap"));
		values.add(e("🙌", "举手庆祝", "raising hands", p, "庆祝", "hooray"));
		values.add(e("🤝", "握手", "handshake", p, "合作", "deal"));
		values.add(e("🙏", "祈祷", "folded hands", p, "谢谢", "拜托", "pray", "thanks"));
		values.add(e("💪", "力量", "flexed biceps", p, "加油", "强壮", "strong"));
		values.add(e("👀", "眼睛", "eyes", p, "看", "关注", "look"));

		EmojiCategory h = EmojiCategory.HEARTS;
		values.add(e("❤️", "红心", "red heart", h, "爱心", "爱", "heart", "love"));
		values.add(e("💔", "心碎", "broken heart", h, "伤心", "broken heart"));
		values.add(e("💕", "两颗心", "two hearts", h, "爱心", "love"));
		values.add(e("💖", "闪亮的心", "sparkling heart", h, "爱心", "sparkle", "love"));

		EmojiCategory a = EmojiCategory.ACTIVITIES;
		values.add(e("🎉", "派对礼花", "party popper", a, "庆祝", "派对", "party"));
		values.add(e("🎊", "五彩纸屑", "confetti ball", a, "庆祝", "confetti"));

		EmojiCategory y = EmojiCategory.SYMBOLS;
		values.add(e("🔥", "火", "fire", y, "热门", "燃烧", "hot"));
		values.add(e("✨", "闪光", "sparkles", y, "亮", "sparkle"));
		values.add(e("⭐", "星星", "star", y, "收藏", "star"));
		values.add(e("✅", "勾选", "check mark button", y, "正确", "完成", "check", "done"));
		values.add(e("❌", "叉号", "cross mark", y, "错误", "取消", "wrong", "cancel"));
		values.add(e("⚠️", "警告", "warning", y, "注意", "危险", "warning"));
		values.add(e("❓", "问号", "question mark", y, "疑问", "question"));
		values.add(e("❗", "感叹号", "exclamation mark", y, "重要", "注意", "important"));

		EmojiCategory n = EmojiCategory.ANIMALS;
		values.add(e("🐶", "狗", "dog face", n, "小狗", "dog"));
		values.add(e("🐱", "猫", "cat face", n, "小猫", "cat"));
		values.add(e("🐷", "猪", "pig face", n, "小猪", "pig"));
		values.add(e("🐔", "鸡", "chicken", n, "小鸡", "chicken"));

		EmojiCategory f = EmojiCategory.FOOD;
		values.add(e("🍎", "苹果", "red apple", f, "水果", "apple"));
		values.add(e("🍞", "面包", "bread", f, "食物", "bread"));
		values.add(e("🍖", "肉", "meat on bone", f, "食物", "meat"));
		values.add(e("🍗", "鸡腿", "poultry leg", f, "食物", "chicken"));

		EmojiCategory t = EmojiCategory.TRAVEL;
		values.add(e("🚗", "汽车", "car", t, "车辆", "car"));
		values.add(e("🚲", "自行车", "bicycle", t, "骑行", "bike"));
		values.add(e("✈️", "飞机", "airplane", t, "飞行", "plane"));
		values.add(e("🚀", "火箭", "rocket", t, "太空", "rocket"));
		values.add(e("🏠", "房子", "house", t, "家", "home"));
		values.add(e("🌍", "地球", "globe showing Europe-Africa", t, "世界", "world"));

		EmojiCategory o = EmojiCategory.OBJECTS;
		values.add(e("⚔️", "交叉剑", "crossed swords", o, "战斗", "剑", "sword"));
		values.add(e("🛡️", "盾牌", "shield", o, "防御", "shield"));
		values.add(e("🏹", "弓箭", "bow and arrow", o, "弓", "箭", "bow"));
		values.add(e("⛏️", "镐", "pick", o, "挖矿", "镐子", "pickaxe"));
		return List.copyOf(values);
	}
}
