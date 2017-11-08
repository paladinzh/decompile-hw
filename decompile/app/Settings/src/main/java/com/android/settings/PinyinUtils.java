package com.android.settings;

import android.text.TextUtils;
import com.android.settings.HanziToPinyin.Token;
import java.util.HashMap;
import java.util.Locale;

public class PinyinUtils {
    public static HashMap<String, String> getPinYinMap(String src) {
        HashMap<String, String> pinYinMap = new HashMap();
        if (TextUtils.isEmpty(src)) {
            return pinYinMap;
        }
        StringBuilder fullPinYinWithSeparator = new StringBuilder();
        StringBuilder initialPinYin = new StringBuilder();
        for (Token token : HanziToPinyin.getInstance().get(src)) {
            if (2 == token.type) {
                if (TextUtils.isEmpty(fullPinYinWithSeparator)) {
                    fullPinYinWithSeparator.append(token.target);
                } else {
                    fullPinYinWithSeparator.append("-").append(token.target);
                }
                if (!TextUtils.isEmpty(token.target)) {
                    initialPinYin.append(token.target.charAt(0));
                }
            } else {
                if (TextUtils.isEmpty(fullPinYinWithSeparator)) {
                    fullPinYinWithSeparator.append(token.source);
                } else {
                    fullPinYinWithSeparator.append("-").append(token.source);
                }
                if (!TextUtils.isEmpty(token.source)) {
                    initialPinYin.append(token.source.charAt(0));
                }
            }
        }
        pinYinMap.put("full_pin_yin", fullPinYinWithSeparator.toString().toLowerCase(Locale.US));
        pinYinMap.put("initial_pin_yin", initialPinYin.toString().toLowerCase(Locale.US));
        return pinYinMap;
    }
}
