package com.huawei.systemmanager.comparator;

import com.huawei.systemmanager.comparator.HanziToPinyin.Token;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class CompareUtils {
    private static final String TAG = "CompareUtils";

    public static String convertHanziToPinyin(String str) {
        ArrayList<Token> tokenList = HanziToPinyin.getInstance().get(str);
        StringBuffer sb = new StringBuffer();
        for (Token token : tokenList) {
            if (2 == token.type) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(token.target);
                sb.append(' ');
                sb.append(token.source);
            } else {
                HwLog.d(TAG, "the input string is not PINYIN type");
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(token.source);
            }
        }
        return sb.toString();
    }
}
