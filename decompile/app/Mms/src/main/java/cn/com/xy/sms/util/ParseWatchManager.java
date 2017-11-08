package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.dex.DexUtil;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseWatchManager {
    public static JSONObject parseMsgForWatch(Context context, String str, String str2, String str3, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            Map a = ParseManager.a(context, str, str2, str3, 0, map);
            return (a == null || ParseBubbleManager.getParseStatu(a) == -1) ? null : DexUtil.handerWatchValueMap(a);
        } else {
            throw new Exception(" smsContent is null.");
        }
    }
}
