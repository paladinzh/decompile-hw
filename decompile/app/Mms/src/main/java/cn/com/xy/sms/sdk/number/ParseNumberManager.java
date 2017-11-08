package cn.com.xy.sms.sdk.number;

import android.content.Context;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.n;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.util.E;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.sdk.util.v;
import java.io.File;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseNumberManager {
    static {
        v.a(E.f, v.e);
    }

    public static File findLogoFile(Context context, String str, Map<String, String> map, XyCallBack xyCallBack) {
        return f.b(context, str, (Map) map, xyCallBack);
    }

    public static File findUnionLogoFile(Context context, String str, Map<String, String> map, XyCallBack xyCallBack) {
        return f.a(context, str, (Map) map, xyCallBack);
    }

    public static String getNumberSourceName(String str) {
        JSONObject g = k.g(str);
        return g != null ? DexUtil.getNumberSourceName(g.optString(NumberInfo.SOURCE_KEY)) : null;
    }

    public static void initEmbedNumber(Context context) {
        b.a(context);
    }

    public static void queryByNum(String str, Map<String, String> map, boolean z, XyCallBack xyCallBack) {
        l.a(str, map, z, xyCallBack);
    }

    public static void removeUserTag(String str, Map<String, String> map) {
        n.a(str);
        k.f(str);
    }

    public static void upgradeEmbedNumber(XyCallBack xyCallBack) {
        r.a(xyCallBack);
    }

    public static void uploadUserTag(String str, String str2, int i, Map<String, String> map, XyCallBack xyCallBack) {
        JSONObject a = n.a(str, str2, i);
        if (a != null) {
            k.a(str, a);
            k.d(str);
            try {
                E.e.execute(new A(str, str2, i, map, xyCallBack));
                return;
            } catch (Throwable th) {
                XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str, th.getMessage());
                return;
            }
        }
        XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str, "save user tag fail");
    }
}
