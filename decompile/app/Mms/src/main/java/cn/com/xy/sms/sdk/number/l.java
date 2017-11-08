package cn.com.xy.sms.sdk.number;

import android.database.Cursor;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.n;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.service.number.a;
import cn.com.xy.sms.sdk.util.E;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.sdk.util.v;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class l {
    private static String a = "RunInThreadPool";
    private static String b = "QueryDBRunInCurrentThread";
    private static final Set<String> c = new HashSet();

    public static Cursor a() {
        return n.a();
    }

    public static JSONObject a(String str) {
        if (!b.a()) {
            return null;
        }
        JSONObject e;
        try {
            if (k.e(str)) {
                b();
                return null;
            }
            e = e(str);
            if (e == null) {
                try {
                    if (i(str)) {
                        Object[] g = g(str);
                        if (g == null) {
                            j(str);
                            b();
                            return e;
                        }
                        JSONObject jSONObject;
                        if (((Integer) g[0]).intValue() == 1) {
                            if (g.length > 2) {
                                jSONObject = (JSONObject) g[2];
                                b();
                                return jSONObject;
                            }
                        }
                        jSONObject = e;
                        b();
                        return jSONObject;
                    }
                    b();
                    return null;
                } catch (Throwable th) {
                    try {
                        j(str);
                        return e;
                    } finally {
                        b();
                    }
                }
            } else {
                b();
                return e;
            }
        } catch (Throwable th2) {
            e = null;
            j(str);
            return e;
        }
    }

    public static JSONObject a(String str, Map<String, String> map) {
        try {
            if (!b.a() || k.e(str)) {
                return null;
            }
            JSONObject e = e(str);
            if (e != null) {
                return e;
            }
            if (!i(str)) {
                return null;
            }
            if (NetUtil.checkAccessNetWork(2)) {
                e = new JSONObject();
                b(str, null, new n(e));
                return e.length() != 0 ? e : null;
            } else {
                h(str);
                n.a(str, null, "", System.currentTimeMillis());
                return null;
            }
        } catch (Throwable th) {
            j(str);
            return null;
        }
    }

    public static void a(String str, Map<String, String> map, boolean z, XyCallBack xyCallBack) {
        if (b.a()) {
            try {
                if (a.a((Map) map)) {
                    XyUtil.removeLoactionInfo();
                    XyUtil.setLoaction(null);
                    XyUtil.removeAreaCodeInfo();
                }
                if (e(str) != null) {
                    XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(1), str, e(str));
                    b();
                } else if (z) {
                    XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-4), str, "scrolling");
                    b();
                } else if (k.e(str)) {
                    XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-1), str, "no data");
                    b();
                } else if (i(str)) {
                    boolean z2 = XyUtil.getBoolean(map, "QueryDBRunInCurrentThread", false);
                    if (z2) {
                        Object[] g = g(str);
                        if (g != null) {
                            XyUtil.doXycallBackResult(xyCallBack, g);
                            b();
                            return;
                        }
                    }
                    E.b.execute(new m(z2, str, xyCallBack, map));
                    XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-2), str, "need query");
                } else {
                    XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-2), str, "in query queue");
                    b();
                }
            } catch (Throwable th) {
                j(str);
                XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str, th.getMessage());
            } finally {
                b();
            }
        } else {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str, "init embed number running");
        }
    }

    private static void a(String str, JSONObject jSONObject) {
        k.a(str, jSONObject);
        k.d(str);
        j(str);
    }

    private static void a(Map<String, String> map) {
        if (a.a((Map) map)) {
            XyUtil.removeLoactionInfo();
            XyUtil.setLoaction(null);
            XyUtil.removeAreaCodeInfo();
        }
    }

    private static void b() {
        v.a(E.d, v.a);
        v.a(E.e, v.b);
        v.a(E.e, v.c);
        v.a(E.a, v.d);
        v.a(E.f, v.e);
        v.a(E.a, v.f);
    }

    private static void b(String str, Map<String, String> map, XyCallBack xyCallBack) {
        try {
            Map hashMap = new HashMap();
            hashMap.put(str, "");
            a.a(hashMap, map, new o(str, xyCallBack));
        } catch (Throwable th) {
            j(str);
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str, th.getMessage());
        }
    }

    private static JSONObject e(String str) {
        JSONObject a = k.a(str);
        if (a != null) {
            return a;
        }
        String f = f(str);
        if (StringUtils.isNull(f)) {
            return a;
        }
        JSONObject a2 = k.a(f);
        return (a2 == null || StringUtils.isNull(a2.optString("areaCode"))) ? a : a2;
    }

    private static String f(String str) {
        if (!DexUtil.isNoAreaCodeFixedPhone(str)) {
            return null;
        }
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), "areaCode");
        return StringUtils.isNull(stringParam) ? null : new StringBuilder(String.valueOf(stringParam)).append(str).toString();
    }

    private static Object[] g(String str) {
        JSONObject b = n.b(str);
        if (b == null) {
            String f = f(str);
            if (!StringUtils.isNull(f)) {
                b = n.b(f);
                if (b != null) {
                    j(str);
                    str = f;
                }
            }
        }
        if (b == null) {
            return null;
        }
        n.a(str, System.currentTimeMillis());
        if (n.b(b)) {
            k.a(str, b);
            k.d(str);
            j(str);
            return new Object[]{Integer.valueOf(1), str, b};
        }
        h(str);
        return new Object[]{Integer.valueOf(-1), str, "no data"};
    }

    private static void h(String str) {
        k.c(str);
        k.b(str);
        j(str);
    }

    private static boolean i(String str) {
        synchronized (c) {
            if (c.contains(str)) {
                return false;
            }
            c.add(str);
            return true;
        }
    }

    private static void j(String str) {
        synchronized (c) {
            c.remove(str);
        }
    }
}
