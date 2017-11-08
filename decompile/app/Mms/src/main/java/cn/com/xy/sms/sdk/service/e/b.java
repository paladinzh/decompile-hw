package cn.com.xy.sms.sdk.service.e;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.C;
import cn.com.xy.sms.sdk.db.entity.H;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.a.f;
import cn.com.xy.sms.sdk.db.entity.m;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.ConversationManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.w;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class b {
    private static boolean a = false;
    private static int b = 0;
    private static int c = 1;
    private static int d = 2;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static Object a(List<String> list, boolean z, String str, String str2, XyCallBack xyCallBack, int i, Object... objArr) {
        Object obj;
        Object obj2 = null;
        if (objArr != null) {
            try {
                if (objArr[0].toString().equals("0")) {
                    if (objArr.length == 2) {
                        String obj3 = objArr[1].toString();
                        f.b((List) list);
                        Map b = j.b(obj3);
                        if (b != null) {
                            if (b.size() != 0) {
                                JSONObject jSONObject = (JSONObject) b.get(b.keySet().iterator().next());
                                String optString = jSONObject.optString("id");
                                if ("0".equals(optString)) {
                                    JSONObject jSONObject2;
                                    if (xyCallBack != null) {
                                        switch (i) {
                                            case 0:
                                                break;
                                            case 1:
                                                try {
                                                    String[] strArr = new String[2];
                                                    JSONArray optJSONArray = jSONObject.optJSONArray("pubMenuInfolist");
                                                    if (optJSONArray != null && optJSONArray.length() > 1) {
                                                        if (!z) {
                                                            String jSONArray = optJSONArray.toString();
                                                            XyUtil.doXycallBackResult(xyCallBack, jSONArray, jSONObject.optString("pubId"));
                                                            break;
                                                        }
                                                        strArr[0] = jSONObject.optString("pubName");
                                                        strArr[1] = optJSONArray.toString();
                                                        XyUtil.doXycallBackResult(xyCallBack, strArr[0], strArr[1]);
                                                        break;
                                                    }
                                                } catch (Throwable th) {
                                                    jSONObject = null;
                                                    if (xyCallBack != null) {
                                                        xyCallBack.execute(ThemeUtil.SET_NULL_STR);
                                                    }
                                                    if (i == 0 || i == 2) {
                                                        C.a(true);
                                                        jSONObject2 = jSONObject;
                                                        for (String obj32 : b.keySet()) {
                                                            f.a((JSONObject) b.get(obj32));
                                                        }
                                                        jSONObject = jSONObject2;
                                                        if (!"0".equals(optString)) {
                                                            if (i == 0) {
                                                            }
                                                            C.a(false);
                                                        }
                                                        obj2 = obj;
                                                        f.a(a((List) list));
                                                        f.a(str);
                                                        return obj2;
                                                    }
                                                    jSONObject2 = jSONObject;
                                                    while (r5.hasNext()) {
                                                        f.a((JSONObject) b.get(obj32));
                                                    }
                                                    jSONObject = jSONObject2;
                                                    if ("0".equals(optString)) {
                                                        if (i == 0) {
                                                        }
                                                        C.a(false);
                                                    }
                                                    obj2 = obj;
                                                    f.a(a((List) list));
                                                    f.a(str);
                                                    return obj2;
                                                }
                                                break;
                                            case 2:
                                                xyCallBack.execute(b);
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                    jSONObject = null;
                                    if (i == 0 || i == 2) {
                                        try {
                                            C.a(true);
                                        } catch (Throwable th2) {
                                            if (xyCallBack != null) {
                                                xyCallBack.execute(ThemeUtil.SET_NULL_STR);
                                            }
                                            if (i == 0) {
                                                jSONObject2 = jSONObject;
                                                while (r5.hasNext()) {
                                                    f.a((JSONObject) b.get(obj32));
                                                }
                                                jSONObject = jSONObject2;
                                                if ("0".equals(optString)) {
                                                    if (i == 0) {
                                                    }
                                                    C.a(false);
                                                }
                                                obj2 = obj;
                                                f.a(a((List) list));
                                                f.a(str);
                                                return obj2;
                                            }
                                            C.a(true);
                                            jSONObject2 = jSONObject;
                                            while (r5.hasNext()) {
                                                f.a((JSONObject) b.get(obj32));
                                            }
                                            jSONObject = jSONObject2;
                                            if ("0".equals(optString)) {
                                                if (i == 0) {
                                                }
                                                C.a(false);
                                            }
                                            obj2 = obj;
                                            f.a(a((List) list));
                                            f.a(str);
                                            return obj2;
                                        }
                                        jSONObject2 = jSONObject;
                                        while (r5.hasNext()) {
                                            f.a((JSONObject) b.get(obj32));
                                        }
                                        jSONObject = jSONObject2;
                                    } else {
                                        jSONObject2 = jSONObject;
                                        while (r5.hasNext()) {
                                            f.a((JSONObject) b.get(obj32));
                                        }
                                        jSONObject = jSONObject2;
                                    }
                                } else {
                                    if ("1".equals(optString)) {
                                        NetUtil.QueryTokenRequest(str2);
                                    }
                                    obj = null;
                                }
                                if ("0".equals(optString)) {
                                    if (i == 0 || i == 2) {
                                        C.a(false);
                                    }
                                }
                                obj2 = obj;
                            }
                        }
                        if (xyCallBack != null) {
                            xyCallBack.execute(new Object[0]);
                        }
                        if (i == 0 || i == 2) {
                            C.a(false);
                        }
                        f.a(a((List) list));
                        f.a(str);
                        return null;
                    }
                }
                f.a(a((List) list));
                f.a(str);
                return obj2;
            } catch (Throwable th3) {
                f.a(a((List) list));
                f.a(str);
            }
        } else {
            if (i == 0 || i == 2) {
                C.a(false);
            }
            if (xyCallBack != null) {
                xyCallBack.execute(Integer.valueOf(-1));
                f.a(a((List) list));
                f.a(str);
                return null;
            }
            f.a(a((List) list));
            f.a(str);
            return null;
        }
    }

    private static List<String> a(List<String> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        List<String> arrayList = new ArrayList();
        for (String jSONObject : list) {
            try {
                String jSONObject2 = new JSONObject(jSONObject2).getString(IccidInfoManager.NUM);
                if (!StringUtils.isNull(jSONObject2)) {
                    arrayList.add(jSONObject2);
                }
            } catch (Throwable th) {
            }
        }
        return arrayList;
    }

    public static void a() {
        a(H.UPLOAD_SHARD);
    }

    private static void a(H h) {
        synchronized (g.a) {
            if (a) {
                return;
            }
            a = true;
            a.a.execute(new f(h));
        }
    }

    public static void a(String str, String str2, String str3, String str4, String str5, XyCallBack xyCallBack, int i, String str6, boolean z) {
        if (z || m.a(str, str3)) {
            try {
                NetUtil.requestTokenIfNeed(str4);
                e eVar = new e(str, str3, xyCallBack, 0, str6, str4);
                if (NetUtil.isEnhance()) {
                    String a = j.a(str, str2, str3, str4, str5);
                    if (StringUtils.isNull(a)) {
                        XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-1), null, str6);
                        return;
                    }
                    NetUtil.executePubNumServiceHttpRequest(a, "990005", eVar, str2, true, false, NetUtil.REQ_QUERY_PUBINFO, true);
                    return;
                }
                XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-1), null, str6);
                return;
            } catch (Throwable th) {
                DexUtil.saveExceptionLog(th);
                return;
            }
        }
        XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-1), null, str6);
    }

    static /* synthetic */ void a(String str, String str2, Object[] objArr) {
        if (objArr != null && objArr.length > 0) {
            try {
                int intValue = ((Integer) objArr[0]).intValue();
                if (intValue == -6 || intValue == -7) {
                    long updateCycleByType = DexUtil.getUpdateCycleByType(41, 7200000);
                    m.a(str, str2, 0, System.currentTimeMillis() - (DexUtil.getUpdateCycleByType(1, Constant.month) - updateCycleByType));
                }
            } catch (Throwable th) {
                DexUtil.saveExceptionLog(th);
            }
        }
    }

    public static void a(List<String> list, String str, String str2, String str3, XyCallBack xyCallBack, boolean z) {
        try {
            if (NetUtil.checkAccessNetWork(1)) {
                String a = w.a();
                ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.service.pubInfo.PubInfoNetService", "queryPubInfoByList", list, str, str2, str3, xyCallBack, Boolean.valueOf(z));
                d dVar = new d(a, list, str, str2, str3, xyCallBack, z);
                NetUtil.requestTokenIfNeed(str2);
                a = j.a((List) list, str, str2, str3);
                if (!StringUtils.isNull(a)) {
                    NetUtil.executePubNumServiceHttpRequest(a, "990005", dVar, null, z, false, NetUtil.REQ_QUERY_PUBINFO, true);
                }
            }
        } catch (Throwable th) {
            DexUtil.saveExceptionLog(th);
        }
    }

    public static void a(boolean z, String str, String str2, String str3, String str4, String str5, XyCallBack xyCallBack, int i, boolean z2, boolean z3, boolean z4) {
        if (z4 || m.a(str, str3)) {
            try {
                NetUtil.requestTokenIfNeed(str4);
                String a = w.a();
                ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.service.pubInfo.PubInfoNetService", "queryPubInfoRequest", Boolean.valueOf(false), str, str2, str3, str4, str5, xyCallBack, Integer.valueOf(i), Boolean.valueOf(false), Boolean.valueOf(z3), Boolean.valueOf(z4));
                c cVar = new c(str, str3, a, false, str2, str4, str5, xyCallBack, i, false, z3, z4);
                if (NetUtil.isEnhance()) {
                    String a2 = j.a(str, str2, str3, str4, str5);
                    if (StringUtils.isNull(a2)) {
                        XyUtil.doXycallBack(xyCallBack, "");
                        return;
                    }
                    NetUtil.executePubNumServiceHttpRequest(a2, "990005", cVar, str2, false, false, NetUtil.REQ_QUERY_PUBINFO, true);
                    return;
                }
                XyUtil.doXycallBack(xyCallBack, "");
                return;
            } catch (Throwable th) {
                DexUtil.saveExceptionLog(th);
                return;
            }
        }
        XyUtil.doXycallBack(xyCallBack, "");
    }

    public static void b() {
        a(H.UPLOAD_PUBINFO_SIGN);
    }

    private static void b(String str, String str2, Object[] objArr) {
        if (objArr != null && objArr.length > 0) {
            try {
                int intValue = ((Integer) objArr[0]).intValue();
                if (intValue == -6 || intValue == -7) {
                    long updateCycleByType = DexUtil.getUpdateCycleByType(41, 7200000);
                    m.a(str, str2, 0, System.currentTimeMillis() - (DexUtil.getUpdateCycleByType(1, Constant.month) - updateCycleByType));
                }
            } catch (Throwable th) {
                DexUtil.saveExceptionLog(th);
            }
        }
    }

    public static void c() {
        a(H.UPLOAD_PUBINFO_CMD);
    }
}
