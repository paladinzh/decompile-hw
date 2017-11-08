package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.dex.DexUtil;
import java.util.Map;
import java.util.Set;

/* compiled from: Unknown */
public class ParseEmailManager {
    public static void cleanCache() {
        DexUtil.cleanCache();
    }

    public static boolean init(Set<String> set) {
        return DexUtil.init(set);
    }

    public static boolean isEnterpriseEmail(String str, Map<String, Object> map) {
        return DexUtil.isEnterpriseEmail(str, map);
    }

    public static boolean isVCEmail(String str) {
        return DexUtil.isVCEmail(str);
    }

    public static boolean isVCEmail(String str, String str2, String str3, String str4, Map<String, String> map) {
        return DexUtil.isVCEmail(str, str2, str3, str4, map);
    }

    public static Map<String, Object> parseEmailToMap(Context context, String str, String str2, Map<String, String> map) {
        return DexUtil.parseEmail(str, str2, map);
    }

    public static Map<String, Object> parseVerCode(String str, String str2, String str3, Map<String, String> map) {
        return DexUtil.parseVerCode(str, str2, str3, map);
    }
}
