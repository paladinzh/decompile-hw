package com.google.android.gms.internal;

import android.text.TextUtils;
import com.amap.api.services.core.AMapException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* compiled from: Unknown */
public final class fp {
    private static final Pattern CO = Pattern.compile("\\\\.");
    private static final Pattern CP = Pattern.compile("[\\\\\"/\b\f\n\r\t]");

    public static String ap(String str) {
        StringBuffer stringBuffer = null;
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        Matcher matcher = CP.matcher(str);
        while (matcher.find()) {
            if (stringBuffer == null) {
                stringBuffer = new StringBuffer();
            }
            switch (matcher.group().charAt(0)) {
                case '\b':
                    matcher.appendReplacement(stringBuffer, "\\\\b");
                    break;
                case '\t':
                    matcher.appendReplacement(stringBuffer, "\\\\t");
                    break;
                case '\n':
                    matcher.appendReplacement(stringBuffer, "\\\\n");
                    break;
                case '\f':
                    matcher.appendReplacement(stringBuffer, "\\\\f");
                    break;
                case '\r':
                    matcher.appendReplacement(stringBuffer, "\\\\r");
                    break;
                case AMapException.ERROR_CODE_SERVER /*34*/:
                    matcher.appendReplacement(stringBuffer, "\\\\\\\"");
                    break;
                case '/':
                    matcher.appendReplacement(stringBuffer, "\\\\/");
                    break;
                case '\\':
                    matcher.appendReplacement(stringBuffer, "\\\\\\\\");
                    break;
                default:
                    break;
            }
        }
        if (stringBuffer == null) {
            return str;
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }
}
