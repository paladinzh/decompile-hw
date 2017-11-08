package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.util.PopupUtil;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class ParseYCardManager {
    private static String a = "yunyingshang_s_0001";
    private static String b = "1003";
    private static String c = "xiaoyuan";

    public static Map<String, Object> parseMsgForCard(Context context, String str, String str2, String str3, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            if (!n.a((byte) 8)) {
                PopupUtil.getResultMap(false, false);
            }
            Map a = ParseManager.a(context, str, str2, str3, 0, map);
            Map<String, Object> hashMap;
            if (a == null) {
                hashMap = new HashMap();
                hashMap.put("Result", Boolean.valueOf(false));
                return hashMap;
            }
            a.put("parseMsgForCard", "true");
            hashMap = DexUtil.handerValueMap(a);
            if (hashMap != null) {
                return hashMap;
            }
            hashMap = new HashMap();
            hashMap.put("Result", Boolean.valueOf(false));
            return hashMap;
        } else {
            throw new Exception(" smsContent is null.");
        }
    }

    public static String parseMsgForCardData(Context context, String str, String str2, String str3, Map<String, String> map) {
        return ParseCardManager.parseMsgForCardData(context, str, str2, str3, map);
    }
}
