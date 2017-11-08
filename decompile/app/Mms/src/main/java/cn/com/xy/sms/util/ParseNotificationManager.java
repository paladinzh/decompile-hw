package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.dex.DexUtil;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class ParseNotificationManager {
    private static Map<String, Object> a(Context context, String str, String str2, String str3, String str4, long j, HashMap<String, String> hashMap) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str2 == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str4 != null) {
            return ParseSmsToBubbleUtil.parseSmsToBubbleResultMap(str, str2, str4, str3, j, 4, true, true, hashMap);
        } else {
            throw new Exception(" smsContent is null.");
        }
    }

    public static Map<String, Object> parseNotificationMsg(Context context, long j, String str, String str2, String str3, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            Map a = ParseManager.a(context, str, str2, str3, 0, ParseManager.putValueToMap(map, "msgId", String.valueOf(j)));
            Map<String, Object> hashMap;
            if (a == null || ParseBubbleManager.getParseStatu(a) == -1) {
                hashMap = new HashMap();
                hashMap.put("Result", Boolean.valueOf(false));
                return hashMap;
            }
            hashMap = DexUtil.handerNotificationValueMap(a);
            if (hashMap != null) {
                hashMap.put("Result", Boolean.valueOf(true));
                return hashMap;
            }
            hashMap = new HashMap();
            hashMap.put("Result", Boolean.valueOf(false));
            return hashMap;
        } else {
            throw new Exception(" smsContent is null.");
        }
    }

    public static Map<String, Object> parseNotificationMsg(Context context, String str, String str2, String str3, String str4, long j, HashMap<String, String> hashMap) {
        Map a = a(context, str, str2, str3, str4, j, hashMap);
        if (a == null) {
            return null;
        }
        ParseSmsToBubbleUtil.backGroundHandleMapByType(hashMap, a);
        return DexUtil.handerNotificationValueMap(a);
    }

    public static Map<String, Object> parseNotificationMsgAndPopupWindow(Context context, String str, String str2, String str3, String str4, long j, HashMap<String, String> hashMap) {
        Object hashMap2 = hashMap != null ? hashMap : new HashMap();
        if (!hashMap2.containsKey("from")) {
            hashMap2.put("from", "1");
        }
        Map a = a(context, str, str2, str3, str4, j, hashMap2);
        if (a == null) {
            return null;
        }
        ParseManager.parseMsgToPopupWindow(context, str2, str3, str4, a, false, hashMap2);
        return DexUtil.handerNotificationValueMap(a);
    }
}
