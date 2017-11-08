package com.google.android.gms.tagmanager;

import android.content.Context;
import android.net.Uri;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
class ay {
    static Map<String, String> Vn = new HashMap();

    ay() {
    }

    static void e(Context context, String str) {
        String l = l(str, "conv");
        if (l != null && l.length() > 0) {
            Vn.put(l, str);
            cz.a(context, "gtm_click_referrers", l, str);
        }
    }

    static String l(String str, String str2) {
        if (str2 != null) {
            return Uri.parse("http://hostname/?" + str).getQueryParameter(str2);
        }
        if (str.length() <= 0) {
            str = null;
        }
        return str;
    }
}
