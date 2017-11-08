package com.huawei.hwid.core.c;

import android.content.Context;
import android.text.TextUtils;
import java.util.Locale;

/* compiled from: VerifyCodeUtil */
public class r {
    private static String b(String str, Context context) {
        Object obj = null;
        CharSequence charSequence;
        if (d.e(context).equals(Locale.CHINA.getLanguage().toLowerCase(Locale.getDefault()))) {
            charSequence = "华为帐号";
        } else {
            charSequence = "HWID";
        }
        if (TextUtils.isEmpty(str) || !str.contains(r0)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt < '0' || charAt > '9') {
                if (obj != null) {
                    break;
                }
            } else {
                stringBuilder.append(charAt);
                obj = 1;
            }
        }
        return stringBuilder.toString();
    }
}
