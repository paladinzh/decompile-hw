package com.google.android.gms.internal;

import java.util.Map;

/* compiled from: Unknown */
public final class as implements ar {
    private static boolean a(Map<String, String> map) {
        return "1".equals(map.get("custom_close"));
    }

    private static int b(Map<String, String> map) {
        String str = (String) map.get("o");
        if (str != null) {
            if ("p".equalsIgnoreCase(str)) {
                return cv.aU();
            }
            if ("l".equalsIgnoreCase(str)) {
                return cv.aT();
            }
        }
        return -1;
    }

    public void a(dd ddVar, Map<String, String> map) {
        String str = (String) map.get("a");
        if (str != null) {
            de bb = ddVar.bb();
            if ("expand".equalsIgnoreCase(str)) {
                if (ddVar.be()) {
                    da.w("Cannot expand WebView that is already expanded.");
                    return;
                }
                bb.a(a(map), b(map));
            } else if ("webapp".equalsIgnoreCase(str)) {
                str = (String) map.get("u");
                if (str == null) {
                    bb.a(a(map), b(map), (String) map.get("html"), (String) map.get("baseurl"));
                } else {
                    bb.a(a(map), b(map), str);
                }
            } else {
                bb.a(new bn((String) map.get("i"), (String) map.get("u"), (String) map.get("m"), (String) map.get("p"), (String) map.get("c"), (String) map.get("f"), (String) map.get("e")));
            }
            return;
        }
        da.w("Action missing from an open GMSG.");
    }
}
