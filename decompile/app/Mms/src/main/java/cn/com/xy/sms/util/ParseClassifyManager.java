package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class ParseClassifyManager {
    public static Map<String, Object> parseClassifyMsg(Context context, String str, String str2, String str3, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            Map a = ParseManager.a(context, str, str2, str3, 0, map);
            Map<String, Object> hashMap = new HashMap();
            hashMap.put("Result", Boolean.valueOf(false));
            if (a != null) {
                String str4 = (String) a.get("title_num");
                if (!StringUtils.isNull(str4)) {
                    if (str4.startsWith("01") || str4.startsWith("02") || str4.startsWith("03") || str4.startsWith("06") || str4.startsWith("08") || str4.startsWith("14")) {
                        hashMap.put("Result", Boolean.valueOf(true));
                    }
                }
            }
            return hashMap;
        } else {
            throw new Exception(" smsContent is null.");
        }
    }
}
