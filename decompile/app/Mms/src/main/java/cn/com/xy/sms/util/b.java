package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.sdk.util.f;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class b {
    private static String a = "version";
    private static final String b = "duoqu_embed_number.zip";
    private static final String c = "embed_number.zip";
    private static final String d = "EmbedNumber.txt";
    private static final String e = "numberOTA.zip";
    private static final String f = "EmbedDiff.txt";
    private static final String g = "embed_number_version";
    private static final String h = "201606021010";
    private static Context i = null;
    private static String j = null;
    private static String k = null;

    public static String a() {
        if (k != null) {
            return k;
        }
        String stringParam = SysParamEntityManager.getStringParam(i, g);
        if (StringUtils.isNull(stringParam)) {
            stringParam = h;
            a(stringParam);
        }
        k = stringParam;
        return stringParam;
    }

    public static JSONObject a(JSONObject jSONObject, Map<String, String> map, String[] strArr, String str) {
        if (strArr == null || strArr.length < 6) {
            return null;
        }
        try {
            jSONObject.put(NumberInfo.NUM_KEY, strArr[0]);
            Object obj;
            String str2;
            if ("1".equals(strArr[1])) {
                jSONObject.remove(NumberInfo.TYPE_KEY);
                jSONObject.remove("tag");
                jSONObject.remove("amount");
                jSONObject.put(NumberInfo.NUM_TYPE_KEY, 1);
                obj = strArr[2];
                str2 = NumberInfo.LOGO_KEY;
                if (StringUtils.isNull(obj)) {
                    obj = "";
                }
                jSONObject.put(str2, obj);
                jSONObject.put("name", strArr[3]);
            } else {
                jSONObject.remove(NumberInfo.NUM_TYPE_KEY);
                jSONObject.remove(NumberInfo.LOGO_KEY);
                jSONObject.remove("name");
                jSONObject.put(NumberInfo.TYPE_KEY, 2);
                obj = (String) map.get(strArr[2]);
                str2 = "tag";
                if (StringUtils.isNull(obj)) {
                    obj = "";
                }
                jSONObject.put(str2, obj);
                jSONObject.put("amount", Integer.parseInt(strArr[3]));
            }
            jSONObject.put(NumberInfo.AUTH_KEY, !"1".equals(strArr[4]) ? 0 : 1);
            jSONObject.put(NumberInfo.SOURCE_KEY, Integer.parseInt(strArr[5]));
            jSONObject.put(NumberInfo.VERSION_KEY, str);
            return jSONObject;
        } catch (Throwable th) {
            return null;
        }
    }

    public static void a(Context context) {
        i = context;
        a();
    }

    public static void a(String str) {
        if (StringUtils.isNull(k) || !k.equals(str)) {
            k = str;
            SysParamEntityManager.setParam(g, str);
        }
    }

    public static String b() {
        return e() + File.separator + e;
    }

    public static void b(String str) {
        f.f(str, e(), e);
    }

    public static InputStream c() {
        XyUtil.upZipFile(b(), e());
        return c(f);
    }

    private static InputStream c(String str) {
        return f.b(e() + File.separator + str);
    }

    public static InputStream d() {
        XyUtil.unZip(Constant.getContext().getResources().getAssets().open(b), c, e());
        return c(d);
    }

    private static String e() {
        if (!StringUtils.isNull(j)) {
            return j;
        }
        String path = Constant.getPath(Constant.DUOQU_EMBED_NUMBER_DIR);
        j = path;
        return path;
    }
}
