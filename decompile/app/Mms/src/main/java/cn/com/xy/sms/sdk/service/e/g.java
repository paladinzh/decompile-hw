package cn.com.xy.sms.sdk.service.e;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.entity.D;
import cn.com.xy.sms.sdk.db.entity.E;
import cn.com.xy.sms.sdk.db.entity.H;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.a.f;
import cn.com.xy.sms.sdk.db.entity.k;
import cn.com.xy.sms.sdk.db.entity.m;
import cn.com.xy.sms.sdk.db.i;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class g {
    public static final a a = new a(1);
    static boolean b = false;
    private static a c = new a(2);

    public static String a(String str, int i, String str2, Map<String, String> map, XyCallBack xyCallBack) {
        if (StringUtils.isNull(str)) {
            return "";
        }
        if (!a(xyCallBack)) {
            return ThemeUtil.SET_NULL_STR;
        }
        try {
            String str3;
            String valueByKey = StringUtils.getValueByKey(map, IccidInfoManager.CNUM);
            String valueByKey2 = StringUtils.getValueByKey(map, "code");
            int d = f.d(StringUtils.getValueByKey(map, "simIndex"));
            if (StringUtils.isNull(valueByKey2)) {
                valueByKey2 = IccidLocationUtil.getAreaCodeByCnumOrIccid(valueByKey, d, str2, str);
            }
            JSONObject a = f.a(str, valueByKey2, i);
            String str4 = "";
            if (map != null && map.containsKey("QUERY_NOW")) {
                a(str, valueByKey, i, valueByKey2, str2, xyCallBack, true, true);
                str3 = str4;
            } else {
                boolean c = f.c(a);
                if (a == null || c) {
                    if (map != null && map.containsKey("SUPPORT_NET_QUERY")) {
                        a(str, valueByKey, i, valueByKey2, str2, xyCallBack, true, c);
                    } else {
                        if (map != null) {
                            if (map.containsKey("SYNC_QUERY")) {
                                a(str, valueByKey, i, valueByKey2, str2, xyCallBack, true, c, false);
                            }
                        }
                        a(str, valueByKey, i, valueByKey2, str2, xyCallBack, false, c);
                    }
                } else if (f.b(a)) {
                    str4 = JsonUtil.pubInfoToJson(a);
                }
                str3 = str4;
                XyUtil.doXycallBackResult(xyCallBack, str3);
            }
            c(valueByKey2, str2);
            return str3;
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(xyCallBack, "");
            return "";
        }
    }

    public static String a(String str, int i, String str2, Map<String, String> map, SdkCallBack sdkCallBack) {
        if (StringUtils.isPhoneNumber(str)) {
            if (sdkCallBack != null) {
                sdkCallBack.execute("");
            }
            return "";
        }
        String valueByKey = StringUtils.getValueByKey(map, IccidInfoManager.CNUM);
        String valueByKey2 = StringUtils.getValueByKey(map, "code");
        int d = f.d(StringUtils.getValueByKey(map, "simIndex"));
        if (StringUtils.isNull(valueByKey2)) {
            valueByKey2 = IccidLocationUtil.getAreaCodeByCnumOrIccid(valueByKey, d, str2, str);
        }
        JSONArray a = f.a(str, valueByKey2, i, map == null ? false : map.containsKey("EXCLUDE_CN"), str2, valueByKey, d, sdkCallBack);
        if (a == null || a.length() <= 0) {
            return "";
        }
        String str3 = "";
        if (a.length() >= 3) {
            str3 = a.toString();
        }
        if (sdkCallBack != null) {
            sdkCallBack.execute(str3);
        }
        NetUtil.executeRunnable(new i(valueByKey2, str2));
        return str3;
    }

    public static String a(Map<String, Object> map) {
        if (map != null) {
            String str = (String) map.get("title_num");
            if (!StringUtils.isNull(str)) {
                try {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("scenetype", str);
                    return jSONObject.toString();
                } catch (Throwable th) {
                }
            }
        }
        return "";
    }

    public static void a() {
        boolean z = false;
        if (!(System.currentTimeMillis() < Constant.lastSqlUpdateTime + Constant.sqlUpdateTimeCyc) && NetUtil.checkAccessNetWork(2)) {
            if (i.c() == null) {
                i.c("menuMain");
            }
            k c = i.c();
            if (c != null) {
                if (System.currentTimeMillis() <= c.e + DexUtil.getUpdateCycleByType(5, Constant.weekTime)) {
                    z = true;
                }
                if (!z) {
                    i.a(c, null, true, null);
                } else if (SysParamEntityManager.getIntParam(Constant.getContext(), Constant.AUTO_UPDATE_DATA) == 0 && NetUtil.checkAccessNetWork(1)) {
                    i.b(c);
                }
            }
            Constant.lastSqlUpdateTime = System.currentTimeMillis();
        }
    }

    public static void a(String str, String str2) {
        try {
            b(str, str2);
        } catch (Throwable th) {
        }
    }

    private static void a(String str, String str2, int i, String str3, String str4, XyCallBack xyCallBack, boolean z, boolean z2) {
        a(str, str2, i, str3, str4, xyCallBack, z2, z, true);
    }

    private static void a(String str, String str2, int i, String str3, String str4, XyCallBack xyCallBack, boolean z, boolean z2, boolean z3) {
        if (SysParamEntityManager.getIntParam(Constant.getContext(), Constant.QUERY_ONLINE) != 0) {
            Runnable jVar = new j(z, str, str2, str3, str4, i, xyCallBack, z2);
            if (z3) {
                a.b.execute(jVar);
            } else {
                jVar.run();
            }
        }
    }

    static void a(String str, String str2, boolean z) {
        List a = m.a(str, System.currentTimeMillis() - DexUtil.getUpdateCycleByType(1, Constant.month), 0);
        if (a != null && !a.isEmpty()) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("request_time", Long.valueOf(System.currentTimeMillis()));
                DBManager.update("tb_netquery_time", contentValues, "request_time < ? and request_time > 0 AND status = 0", new String[]{String.valueOf(r0)});
            } catch (Throwable th) {
            }
            while (true) {
                int size = a.size();
                if (size > 0) {
                    Collection arrayList = new ArrayList();
                    if (size > 10) {
                        size = 10;
                    }
                    arrayList.addAll(a.subList(0, size));
                    b.a(arrayList, str, str2, "1", null, false);
                    a.removeAll(arrayList);
                } else {
                    return;
                }
            }
        }
    }

    public static void a(Map<String, String> map, XyCallBack xyCallBack) {
        if (i.c() == null) {
            i.c("menuMain");
        }
        i.a((Map) map, xyCallBack);
    }

    public static void a(String[] strArr) {
        if (strArr != null) {
            if (r3 != 0) {
                D d;
                StringBuffer stringBuffer = new StringBuffer();
                int i = 0;
                for (String append : strArr) {
                    stringBuffer.append(append);
                    i++;
                    if (i != 10) {
                        stringBuffer.append(",");
                    } else {
                        d = new D();
                        d.a = stringBuffer.toString();
                        d.c = H.UPDATE_PUBINFO.toString();
                        E.a(d);
                        stringBuffer.setLength(0);
                        i = 0;
                    }
                }
                if (i > 0) {
                    d = new D();
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                    d.a = stringBuffer.toString();
                    d.c = H.UPDATE_PUBINFO.toString();
                    E.a(d);
                }
                E.a(H.UPDATE_PUBINFO);
            }
        }
    }

    private static boolean a(XyCallBack xyCallBack) {
        if (ParseItemManager.isInitData()) {
            return true;
        }
        XyUtil.doXycallBackResult(xyCallBack, ThemeUtil.SET_NULL_STR);
        return false;
    }

    public static String b(String str, int i, String str2, Map<String, String> map, XyCallBack xyCallBack) {
        if (StringUtils.isNull(str)) {
            return "";
        }
        if (!a(xyCallBack)) {
            return ThemeUtil.SET_NULL_STR;
        }
        try {
            String pubInfoToJson;
            String valueByKey = StringUtils.getValueByKey(map, IccidInfoManager.CNUM);
            String valueByKey2 = StringUtils.getValueByKey(map, "code");
            int d = f.d(StringUtils.getValueByKey(map, "simIndex"));
            String valueByKey3 = StringUtils.getValueByKey(map, "id");
            if (StringUtils.isNull(valueByKey2)) {
                valueByKey2 = IccidLocationUtil.getAreaCodeByCnumOrIccid(valueByKey, d, str2, str);
            }
            d = SysParamEntityManager.getIntParam(Constant.getContext(), Constant.QUERY_ONLINE);
            JSONObject a = f.a(str, valueByKey2, i);
            String str3 = "";
            boolean c = f.c(a);
            if (a == null || c) {
                if (d == 1) {
                    if (NetUtil.isEnhance()) {
                        b.a(str, valueByKey, valueByKey2, str2, new StringBuilder(String.valueOf(i)).toString(), xyCallBack, 0, valueByKey3, c);
                    }
                }
            } else if (f.b(a)) {
                pubInfoToJson = JsonUtil.pubInfoToJson(a);
                XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(0), pubInfoToJson, valueByKey3);
                c(valueByKey2, str2);
                return pubInfoToJson;
            }
            pubInfoToJson = str3;
            c(valueByKey2, str2);
            return pubInfoToJson;
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(xyCallBack, "");
            return "";
        }
    }

    public static void b(String str, String str2) {
        synchronized (a) {
            if (b) {
                return;
            }
            b = true;
            a.b = str;
            a.a = str2;
            a.a.execute(a);
        }
    }

    static void b(String str, String str2, boolean z) {
        List d = f.d();
        if (d == null || d.size() == 0) {
            synchronized (a) {
                b = false;
            }
            return;
        }
        String str3 = str;
        String str4 = str2;
        b.a(d, str3, str4, "1", new h(str, str2, z), z);
    }

    private static void c(String str, String str2) {
        Long l = (Long) Constant.checkCodeIccidMap.get(new StringBuilder(String.valueOf(str)).append(str2).toString());
        if ((System.currentTimeMillis() < (l == null ? 0 : l.longValue()) + 600000 ? 1 : null) == null && !c.c && SysParamEntityManager.getIntParam(Constant.getContext(), Constant.QUERY_ONLINE) != 0) {
            c.b = str;
            c.a = str2;
            a.a.execute(c);
            Constant.checkCodeIccidMap.put(new StringBuilder(String.valueOf(str)).append(str2).toString(), Long.valueOf(System.currentTimeMillis()));
        }
    }
}
