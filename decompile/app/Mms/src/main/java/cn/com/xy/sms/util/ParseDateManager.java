package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.dex.DexUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class ParseDateManager {
    public static Map<String, Object> parseDateMsg(Context context, String str, String str2, String str3, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            Map a = ParseManager.a(context, str, str2, str3, 0, map);
            String format = new SimpleDateFormat(Constant.PATTERN).format(new Date());
            Map<String, Object> hashMap;
            if (a == null) {
                hashMap = new HashMap();
                hashMap.put("Dojuage", Boolean.valueOf(false));
                hashMap.put("Result", Boolean.valueOf(false));
                hashMap.put("date", new StringBuilder(String.valueOf(format)).append(" 09:00").toString());
                return hashMap;
            }
            hashMap = DexUtil.handerDateValueMap(a);
            if (hashMap == null || hashMap.isEmpty()) {
                hashMap = new HashMap();
                hashMap.put("Dojuage", Boolean.valueOf(false));
                hashMap.put("Result", Boolean.valueOf(false));
                hashMap.put("date", new StringBuilder(String.valueOf(format)).append(" 09:00").toString());
                return hashMap;
            }
            hashMap.put("Result", Boolean.valueOf(true));
            return hashMap;
        } else {
            throw new Exception(" smsContent is null.");
        }
    }
}
