package cn.com.xy.sms.sdk.number;

import android.content.Context;
import android.util.Pair;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.E;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class f {
    private static String a = "DownloadInThreadPool";
    private static final HashMap<String, Long> b = new HashMap();
    private static String c = "duoqu_mobile_";

    private static XyCallBack a(String str, XyCallBack xyCallBack) {
        return new i(str, xyCallBack);
    }

    private static File a(Context context, String str, String str2, Map<String, String> map, XyCallBack xyCallBack) {
        try {
            File e = e(str2);
            if (e != null) {
                XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(1), str2, e);
                return e;
            } else if (f(str2)) {
                if (XyUtil.getBoolean(map, "DownloadInThreadPool", true)) {
                    E.c.execute(new g(context, str, str2, xyCallBack));
                    XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-3), str2);
                } else {
                    e = b(context, str, str2, xyCallBack);
                }
                return e;
            } else {
                XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-5), str2, "repeat request");
                return null;
            }
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str2, th.getMessage());
            return null;
        }
    }

    public static File a(Context context, String str, Map<String, String> map) {
        try {
            JSONObject g = k.g(str);
            if (g == null) {
                return null;
            }
            String optString = g.optString(NumberInfo.LOGO_KEY);
            if (StringUtils.isNull(optString)) {
                return null;
            }
            String str2;
            if (optString.toLowerCase().startsWith("http")) {
                Pair b = b(g);
                str2 = (String) b.first;
                optString = (String) b.second;
            } else {
                str2 = NetUtil.BIZPORT_DOWN_URL;
            }
            return a(context, str2, optString, map, null);
        } catch (Throwable th) {
            return null;
        }
    }

    public static File a(Context context, String str, Map<String, String> map, XyCallBack xyCallBack) {
        JSONObject g = k.g(str);
        if (g != null) {
            return a(context, NetUtil.BIZPORT_DOWN_URL, !"1".equals(g.optString(NumberInfo.AUTH_KEY)) ? "duoqu_mobile_" + g.optString(NumberInfo.SOURCE_KEY) + ".png" : "duoqu_mobile_" + g.optString(NumberInfo.SOURCE_KEY) + ".png", map, a(str, xyCallBack));
        }
        XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str, "no num data");
        return null;
    }

    public static File a(String str) {
        try {
            JSONObject g = k.g(str);
            if (g == null) {
                return null;
            }
            String optString = g.optString(NumberInfo.LOGO_KEY);
            if (StringUtils.isNull(optString)) {
                return null;
            }
            if (optString.toLowerCase().startsWith("http")) {
                optString = (String) b(g).second;
            }
            File e = e(optString);
            return e == null ? null : e;
        } catch (Throwable th) {
        }
    }

    static /* synthetic */ void a(File file) {
        if (file != null && file.exists()) {
            synchronized (b) {
                b.remove(file.getName());
            }
        }
    }

    private static boolean a(JSONObject jSONObject) {
        return "1".equals(jSONObject.optString(NumberInfo.AUTH_KEY));
    }

    private static Pair<String, String> b(JSONObject jSONObject) {
        Object obj = null;
        if (jSONObject == null || jSONObject.length() == 0) {
            return null;
        }
        Pair<String, String> pair;
        String optString;
        if (jSONObject != null) {
            try {
                if (jSONObject.has(NumberInfo.SERVER_URL_KEY) && jSONObject.has(NumberInfo.LOGO_NAME_KEY)) {
                    obj = 1;
                }
                if (obj != null) {
                    pair = new Pair(jSONObject.optString(NumberInfo.SERVER_URL_KEY), jSONObject.optString(NumberInfo.LOGO_NAME_KEY));
                    if (pair == null) {
                        optString = jSONObject.optString(NumberInfo.LOGO_KEY);
                        if (StringUtils.isNull(optString)) {
                            pair = null;
                        } else {
                            int lastIndexOf = optString.lastIndexOf("/");
                            pair = new Pair(optString.substring(0, lastIndexOf + 1), optString.substring(lastIndexOf + 1));
                        }
                        if (pair != null) {
                            jSONObject.put(NumberInfo.SERVER_URL_KEY, pair.first);
                            jSONObject.put(NumberInfo.LOGO_NAME_KEY, pair.second);
                        }
                    }
                    return pair;
                }
            } catch (Throwable th) {
                return null;
            }
        }
        pair = null;
        if (pair == null) {
            optString = jSONObject.optString(NumberInfo.LOGO_KEY);
            if (StringUtils.isNull(optString)) {
                pair = null;
            } else {
                int lastIndexOf2 = optString.lastIndexOf("/");
                pair = new Pair(optString.substring(0, lastIndexOf2 + 1), optString.substring(lastIndexOf2 + 1));
            }
            if (pair != null) {
                jSONObject.put(NumberInfo.SERVER_URL_KEY, pair.first);
                jSONObject.put(NumberInfo.LOGO_NAME_KEY, pair.second);
            }
        }
        return pair;
    }

    private static File b(Context context, String str, String str2, XyCallBack xyCallBack) {
        boolean z = true;
        if (!(str2 != null ? str2.toLowerCase().startsWith("duoqu_mobile_") : false)) {
            if (!NetUtil.BIZPORT_DOWN_URL.equals(str)) {
            }
            return cn.com.xy.sms.sdk.util.f.a(str, str2, Constant.getPath(Constant.DUOQU_PUBLIC_LOGO_DIR), z, new h(xyCallBack));
        }
        z = false;
        return cn.com.xy.sms.sdk.util.f.a(str, str2, Constant.getPath(Constant.DUOQU_PUBLIC_LOGO_DIR), z, new h(xyCallBack));
    }

    public static File b(Context context, String str, Map<String, String> map, XyCallBack xyCallBack) {
        try {
            JSONObject g = k.g(str);
            if (g != null) {
                String optString = g.optString(NumberInfo.LOGO_KEY);
                if (StringUtils.isNull(optString)) {
                    XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str, "logo is null");
                    return null;
                }
                String str2;
                if (optString.toLowerCase().startsWith("http")) {
                    Pair b = b(g);
                    str2 = (String) b.first;
                    optString = (String) b.second;
                } else {
                    str2 = NetUtil.BIZPORT_DOWN_URL;
                }
                return a(context, str2, optString, map, a(str, xyCallBack));
            }
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str, "no num data");
            return null;
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str, th.getMessage());
            return null;
        }
    }

    private static void b(File file) {
        if (file != null && file.exists()) {
            synchronized (b) {
                b.remove(file.getName());
            }
        }
    }

    private static boolean b(String str) {
        return str != null ? str.toLowerCase().startsWith("duoqu_mobile_") : false;
    }

    private static Pair<String, String> c(JSONObject jSONObject) {
        Object obj = null;
        if (jSONObject == null) {
            return null;
        }
        if (jSONObject.has(NumberInfo.SERVER_URL_KEY) && jSONObject.has(NumberInfo.LOGO_NAME_KEY)) {
            obj = 1;
        }
        return obj != null ? new Pair(jSONObject.optString(NumberInfo.SERVER_URL_KEY), jSONObject.optString(NumberInfo.LOGO_NAME_KEY)) : null;
    }

    private static boolean c(String str) {
        return !NetUtil.BIZPORT_DOWN_URL.equals(str);
    }

    private static Pair<String, String> d(String str) {
        if (!StringUtils.isNull(str)) {
            return null;
        }
        int lastIndexOf = str.lastIndexOf("/");
        return new Pair(str.substring(0, lastIndexOf + 1), str.substring(lastIndexOf + 1));
    }

    private static File e(String str) {
        File file = new File(new StringBuilder(String.valueOf(Constant.getPath(Constant.DUOQU_PUBLIC_LOGO_DIR))).append(str).toString());
        return !file.exists() ? null : file;
    }

    private static boolean f(String str) {
        synchronized (b) {
            Long l = (Long) b.get(str);
            if (l != null) {
                if (l.longValue() + Constant.MINUTE >= System.currentTimeMillis()) {
                    return false;
                }
            }
            b.put(str, Long.valueOf(System.currentTimeMillis()));
            return true;
        }
    }
}
