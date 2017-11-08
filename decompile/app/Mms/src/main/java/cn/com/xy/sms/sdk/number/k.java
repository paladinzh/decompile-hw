package cn.com.xy.sms.sdk.number;

import android.util.LruCache;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.n;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.autonavi.amap.mapcore.VTMCDataCache;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class k {
    private static final LruCache<String, JSONObject> a = new LruCache(VTMCDataCache.MAXSIZE);
    private static final Set<String> b = Collections.synchronizedSet(new HashSet());

    public static JSONObject a(String str) {
        return (JSONObject) a.get(str);
    }

    public static void a() {
        a.evictAll();
        b.clear();
    }

    public static void a(String str, int i) {
        try {
            JSONObject a = a(str);
            if (a != null) {
                a.put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, 1);
            }
        } catch (Throwable th) {
        }
    }

    private static void a(String str, String str2, int i, int i2) {
        try {
            JSONObject a = a(str);
            if (a != null) {
                a.put(NumberInfo.USER_TAG_KEY, str2);
                a.put(NumberInfo.USER_TAG_TYPE_KEY, i);
                a.put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, i2);
            }
        } catch (Throwable th) {
        }
    }

    public static void a(String str, JSONObject jSONObject) {
        a.put(str, jSONObject);
    }

    public static void a(JSONArray jSONArray) {
        if (jSONArray != null) {
            try {
                if (jSONArray.length() != 0) {
                    int length = jSONArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject jSONObject = jSONArray.getJSONObject(i);
                        a(jSONObject.getString(NumberInfo.NUM_KEY), jSONObject);
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void b(String str) {
        a.remove(str);
    }

    public static void b(JSONArray jSONArray) {
        if (jSONArray != null) {
            try {
                if (jSONArray.length() != 0) {
                    int length = jSONArray.length();
                    for (int i = 0; i < length; i++) {
                        a(jSONArray.getString(i), 1);
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void c(String str) {
        b.add(str);
    }

    public static void d(String str) {
        b.remove(str);
    }

    public static boolean e(String str) {
        return b.contains(str);
    }

    public static void f(String str) {
        try {
            JSONObject a = a(str);
            if (a != null) {
                n.a(a);
                if (a.length() == 1 && a.has(NumberInfo.NUM_KEY)) {
                    b(str);
                }
            }
        } catch (Throwable th) {
        }
    }

    public static JSONObject g(String str) {
        if (StringUtils.isNull(str)) {
            return null;
        }
        JSONObject a = a(str);
        if (a != null) {
            return a;
        }
        a = n.b(str);
        if (a != null && n.b(a)) {
            a(str, a);
        }
        return a;
    }
}
