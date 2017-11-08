package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.dex.DexUtil;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class j {
    public static String a(String str) {
        JSONObject g = k.g(str);
        return g != null ? DexUtil.getNumberSourceName(g.optString(NumberInfo.SOURCE_KEY)) : null;
    }
}
