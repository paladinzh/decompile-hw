package com.huawei.mms.util;

import android.content.res.Resources;
import android.text.TextUtils;
import java.util.Locale;

public class HighLightMatchUtils {
    public static int indexOfWordPrefix(CharSequence text, char[] prefix) {
        if (prefix == null || text == null) {
            return -1;
        }
        int textLength = text.length();
        int prefixLength = prefix.length;
        if (prefixLength == 0 || textLength < prefixLength) {
            return -1;
        }
        int i = 0;
        while (i < textLength) {
            while (i < textLength && !Character.isLetterOrDigit(text.charAt(i))) {
                i++;
            }
            if (i + prefixLength > textLength) {
                return -1;
            }
            int j = 0;
            while (j < prefixLength && Character.toLowerCase(text.charAt(i + j)) == prefix[j]) {
                j++;
            }
            if (j == prefixLength) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static int getControlColor(Resources res) {
        if (res != null) {
            int colorfulId = res.getIdentifier("colorful_emui", "color", "androidhwext");
            if (colorfulId != 0) {
                return res.getColor(colorfulId);
            }
        }
        return 0;
    }

    public static char[] getLowerCaseQueryString(String queryString) {
        if (TextUtils.isEmpty(queryString)) {
            return new char[0];
        }
        char[] mLowerCaseQueryString = queryString.toLowerCase(Locale.getDefault()).toCharArray();
        char[] lowerperCaseQuerystring = new char[mLowerCaseQueryString.length];
        System.arraycopy(mLowerCaseQueryString, 0, lowerperCaseQuerystring, 0, mLowerCaseQueryString.length);
        return lowerperCaseQuerystring;
    }
}
