package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.dex.DexUtil;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class ParsePayManager {
    public static Map<String, Object> parseMsgForPay(Context context, String str, String str2, String str3, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            Map a = ParseManager.a(context, str, str2, str3, 0, map);
            Map<String, Object> hashMap;
            if (a == null || ParseBubbleManager.getParseStatu(a) == -1) {
                hashMap = new HashMap();
                hashMap.put("Result", Boolean.valueOf(false));
                return hashMap;
            }
            hashMap = DexUtil.handerPayValueMap(a);
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
}
