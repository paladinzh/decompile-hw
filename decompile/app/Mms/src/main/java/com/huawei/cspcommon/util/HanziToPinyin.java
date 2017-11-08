package com.huawei.cspcommon.util;

import android.icu.text.Transliterator;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;

public class HanziToPinyin {
    static final HashMap<String, String> mMultiPinyin = new HashMap<String, String>() {
        {
            put("沈", "SHEN");
            put("曾", "ZENG");
            put("贾", "JIA");
            put("俞", "YU");
            put("儿", "ER");
            put("呵", "HE");
            put("长", "CHANG");
            put("略", "LUE");
            put("掠", "LUE");
            put("乾", "QIAN");
            put("秘", "bi");
            put("薄", "bo");
            put("种", "chong");
            put("褚", "chu");
            put("啜", "chuai");
            put("句", "gou");
            put("莞", "guan");
            put("炔", "gui");
            put("藉", "ji");
            put("圈", "juan");
            put("角", "jue");
            put("阚", "kan");
            put("陆", "lu");
            put("缪", "miao");
            put("佴", "nai");
            put("兒", "ni");
            put("乜", "nie");
            put("区", "ou");
            put("朴", "piao");
            put("繁", "po");
            put("仇", "qiu");
            put("单", "shan");
            put("盛", "sheng");
            put("折", "she");
            put("宿", "su");
            put("洗", "xian");
            put("解", "xie");
            put("员", "yun");
            put("笮", "ze");
            put("直", "zha");
            put("翟", "zhai");
            put("祭", "zhai");
            put("阿", "a");
            put("宓", "fu");
            put("那", "nuo");
            put("尉", "yu");
            put("蛾", "yi");
            put("查", "zha");
        }
    };
    private static HanziToPinyin sInstance;
    private Transliterator mAsciiTransliterator;
    private Transliterator mPinyinTransliterator;

    public static class Token {
        public String source;
        public String target;
        public int type;

        public Token(int type, String source, String target) {
            this.type = type;
            this.source = source;
            this.target = target;
        }
    }

    private HanziToPinyin() {
        try {
            this.mPinyinTransliterator = Transliterator.getInstance("Han-Latin/Names; Latin-Ascii; Any-Upper");
            this.mAsciiTransliterator = Transliterator.getInstance("Latin-Ascii");
        } catch (RuntimeException e) {
            Log.w("HanziToPinyin", "Han-Latin/Names transliterator data is missing, HanziToPinyin is disabled");
        }
    }

    public boolean hasChineseTransliterator() {
        return this.mPinyinTransliterator != null;
    }

    public static HanziToPinyin getInstance() {
        HanziToPinyin hanziToPinyin;
        synchronized (HanziToPinyin.class) {
            if (sInstance == null) {
                sInstance = new HanziToPinyin();
            }
            hanziToPinyin = sInstance;
        }
        return hanziToPinyin;
    }

    private void tokenize(char character, Token token) {
        token.source = Character.toString(character);
        if (character < '' || character == 'ß') {
            token.type = 1;
            token.target = token.source;
        } else if (character < 'ɐ' || ('Ḁ' <= character && character < 'ỿ')) {
            String str;
            token.type = 1;
            if (this.mAsciiTransliterator == null) {
                str = token.source;
            } else {
                str = this.mAsciiTransliterator.transliterate(token.source);
            }
            token.target = str;
        } else {
            token.type = 2;
            token.target = this.mPinyinTransliterator.transliterate(token.source);
            if (TextUtils.isEmpty(token.target) || TextUtils.equals(token.source, token.target)) {
                token.type = 4;
                token.target = token.source;
            }
        }
    }

    private void checkMultiPinyin(Token token) {
        if (token != null && 2 == token.type) {
            String src = token.source;
            String tgt = token.target;
            String pinyin = (String) mMultiPinyin.get(src);
            if (!(pinyin == null || pinyin.equals(tgt))) {
                token.target = pinyin;
            }
        }
    }

    public TokensWithName getTokensWithName(String input) {
        TokensWithName tokensWithName = new TokensWithName();
        if (!hasChineseTransliterator() || TextUtils.isEmpty(input)) {
            return tokensWithName;
        }
        ArrayList<Token> tokens = tokensWithName.getTokens();
        StringBuilder nameSb = new StringBuilder();
        int inputLength = input.length();
        StringBuilder sb = new StringBuilder();
        int tokenType = 1;
        Token token = new Token();
        for (int i = 0; i < inputLength; i++) {
            char character = input.charAt(i);
            if (!Character.isSpaceChar(character) && character != '.') {
                tokenize(character, token);
                if (token.type == 2) {
                    checkMultiPinyin(token);
                    if (sb.length() > 0) {
                        addNameAndTokens(nameSb, tokens, tokenType, sb);
                    }
                    if (tokens.size() > 0) {
                        nameSb.append(" ");
                    }
                    nameSb.append(token.target);
                    tokens.add(token);
                    token = new Token();
                } else {
                    if (tokenType != token.type && sb.length() > 0) {
                        addNameAndTokens(nameSb, tokens, tokenType, sb);
                    }
                    sb.append(token.target);
                }
                tokenType = token.type;
            } else if (sb.length() > 0) {
                addNameAndTokens(nameSb, tokens, tokenType, sb);
            } else {
                nameSb.append(character);
            }
        }
        if (sb.length() > 0) {
            addNameAndTokens(nameSb, tokens, tokenType, sb);
        }
        tokensWithName.setName(nameSb.toString());
        return tokensWithName;
    }

    private void addNameAndTokens(StringBuilder nameSb, ArrayList<Token> tokens, int tokenType, StringBuilder sb) {
        String str = sb.toString();
        if (tokens.size() > 0) {
            nameSb.append(" ");
        }
        nameSb.append(str);
        tokens.add(new Token(tokenType, str, str));
        sb.setLength(0);
    }
}
