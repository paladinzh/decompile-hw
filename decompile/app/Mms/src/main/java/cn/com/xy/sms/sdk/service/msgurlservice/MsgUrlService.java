package cn.com.xy.sms.sdk.service.msgurlservice;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.db.entity.l;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class MsgUrlService {
    public static final int RESULT_BLACK_LIST = -1;
    public static final int RESULT_ERROR_PARA = -4;
    public static final int RESULT_NOT_FIND = 406;
    public static final int RESULT_NOT_IMPL = -999;
    public static final int RESULT_NOT_YUMING = 1;
    public static final int RESULT_NO_NET = 405;
    public static final int RESULT_SERVER_ERROR = 404;
    public static final int RESULT_THIRD_MIN_LEVLE = -1;
    public static final int RESULT_TOKEN_FLASH = 403;
    public static final int RESULT_WHITE_LIST = 2;
    private static boolean a = false;

    static /* synthetic */ int a(JSONArray jSONArray) {
        if (jSONArray == null || jSONArray.length() <= 0) {
            return RESULT_SERVER_ERROR;
        }
        int i = Integer.MAX_VALUE;
        for (int i2 = 0; i2 < jSONArray.length(); i2++) {
            int optInt = jSONArray.optJSONObject(i2).optInt("validStatus");
            if (optInt <= i) {
                i = optInt;
            }
            if (optInt < 0) {
                return -1;
            }
        }
        return i;
    }

    static /* synthetic */ void a(String str, String str2, String str3, long j, String str4) {
        try {
            String md5 = MatchCacheManager.getMD5(str2, str3);
            if (!StringUtils.isNull(md5)) {
                String[] strArr = new String[10];
                strArr[0] = "msg_num_md5";
                strArr[1] = md5;
                strArr[2] = NetUtil.REQ_QUERY_NUM;
                strArr[3] = StringUtils.getPhoneNumberNo86(str2);
                strArr[4] = "msg_id";
                strArr[5] = str;
                strArr[6] = "urls_result";
                if (str4 == null) {
                    str4 = "";
                }
                strArr[7] = str4;
                strArr[8] = "urls_lasttime";
                strArr[9] = String.valueOf(System.currentTimeMillis());
                MatchCacheManager.insertOrUpdate(BaseManager.getContentValues(null, strArr), 5);
            }
        } catch (Throwable th) {
        }
    }

    private static void a(String str, String str2, String str3, String str4) {
        try {
            String md5 = MatchCacheManager.getMD5(str2, str3);
            if (!StringUtils.isNull(md5)) {
                String[] strArr = new String[10];
                strArr[0] = "msg_num_md5";
                strArr[1] = md5;
                strArr[2] = NetUtil.REQ_QUERY_NUM;
                strArr[3] = StringUtils.getPhoneNumberNo86(str2);
                strArr[4] = "msg_id";
                strArr[5] = str;
                strArr[6] = "urls_result";
                if (str4 == null) {
                    str4 = "";
                }
                strArr[7] = str4;
                strArr[8] = "urls_lasttime";
                strArr[9] = String.valueOf(System.currentTimeMillis());
                MatchCacheManager.insertOrUpdate(BaseManager.getContentValues(null, strArr), 5);
            }
        } catch (Throwable th) {
        }
    }

    static /* synthetic */ void a(String str, String str2, String str3, Map map, JSONObject jSONObject, XyCallBack xyCallBack) {
        if (jSONObject == null) {
            try {
                int parseSensitive = DexUtil.parseSensitive(str3);
                String catchUrls = DexUtil.catchUrls(str3, null);
            } catch (Throwable th) {
                return;
            }
        }
        parseSensitive = jSONObject.optInt("hasSensitive");
        catchUrls = jSONObject.optString("urls");
        boolean z = map != null && map.containsKey("breviary") && ((String) map.get("breviary")).equalsIgnoreCase("true");
        if (StringUtils.isNull(catchUrls)) {
            XyUtil.doXycallBackResult(xyCallBack, b("", parseSensitive, RESULT_NOT_FIND, jSONObject, null));
            return;
        }
        String[] split = catchUrls.split("_ARR_");
        HashMap a = l.a(split, false);
        int intValue = ((Integer) a.get("statu")).intValue();
        Object obj = null;
        boolean checkAccessNetWork = NetUtil.checkAccessNetWork(2);
        if (a.containsKey("hasNotCheck") && intValue != -1) {
            obj = 1;
            if (!checkAccessNetWork) {
                intValue = RESULT_NO_NET;
            }
        }
        if (obj != null && checkAccessNetWork) {
            String a2 = j.a(str2, split, z);
            NetUtil.requestNewTokenIfNeed(map);
            NetUtil.executeNewServiceHttpRequest(NetUtil.URL_VALIDITY, a2, new j(a2, catchUrls, parseSensitive, jSONObject, xyCallBack), false, false, true, map);
            return;
        }
        JSONObject b = b(catchUrls, parseSensitive, intValue, jSONObject, null);
        XyUtil.doXycallBackResult(xyCallBack, b);
    }

    private static void a(String str, String str2, String str3, String[] strArr, int i, Map<String, String> map) {
        if (i >= 0) {
            try {
                if (i <= strArr.length - 1) {
                    executeRunnable(new e(i, strArr, str, str2, str3, map));
                }
            } catch (Throwable th) {
            }
        }
    }

    private static void a(String str, String str2, Map<String, String> map, JSONObject jSONObject, XyCallBack xyCallBack) {
        if (jSONObject == null) {
            try {
                int parseSensitive = DexUtil.parseSensitive(str2);
                String catchUrls = DexUtil.catchUrls(str2, null);
            } catch (Throwable th) {
                return;
            }
        }
        parseSensitive = jSONObject.optInt("hasSensitive");
        catchUrls = jSONObject.optString("urls");
        boolean z = map != null && map.containsKey("breviary") && ((String) map.get("breviary")).equalsIgnoreCase("true");
        if (StringUtils.isNull(catchUrls)) {
            XyUtil.doXycallBackResult(xyCallBack, b("", parseSensitive, RESULT_NOT_FIND, jSONObject, null));
            return;
        }
        int i;
        String[] split = catchUrls.split("_ARR_");
        HashMap a = l.a(split, false);
        int intValue = ((Integer) a.get("statu")).intValue();
        boolean checkAccessNetWork = NetUtil.checkAccessNetWork(2);
        if (!a.containsKey("hasNotCheck") || intValue == -1) {
            i = intValue;
            Object obj = null;
        } else if (checkAccessNetWork) {
            i = intValue;
            intValue = 1;
        } else {
            i = RESULT_NO_NET;
            intValue = 1;
        }
        if (obj != null && checkAccessNetWork) {
            String a2 = j.a(str, split, z);
            NetUtil.requestNewTokenIfNeed(map);
            NetUtil.executeNewServiceHttpRequest(NetUtil.URL_VALIDITY, a2, new j(a2, catchUrls, parseSensitive, jSONObject, xyCallBack), false, false, true, map);
            return;
        }
        JSONObject b = b(catchUrls, parseSensitive, i, jSONObject, null);
        XyUtil.doXycallBackResult(xyCallBack, b);
    }

    private static int b(JSONArray jSONArray) {
        if (jSONArray == null || jSONArray.length() <= 0) {
            return RESULT_SERVER_ERROR;
        }
        int i = Integer.MAX_VALUE;
        for (int i2 = 0; i2 < jSONArray.length(); i2++) {
            int optInt = jSONArray.optJSONObject(i2).optInt("validStatus");
            if (optInt <= i) {
                i = optInt;
            }
            if (optInt < 0) {
                return -1;
            }
        }
        return i;
    }

    private static JSONObject b(String str, int i, int i2, JSONObject jSONObject, JSONArray jSONArray) {
        if (jSONObject == null) {
            jSONObject = new JSONObject();
        } else {
            jSONObject.remove("results");
        }
        try {
            jSONObject.put("urls", str);
            jSONObject.put("hasSensitive", i);
            jSONObject.put("validStatus", i2);
            if (jSONArray != null) {
                jSONObject.put("results", jSONArray);
            }
            return jSONObject;
        } catch (Throwable th) {
            return jSONObject;
        }
    }

    private static JSONObject b(String str, String str2, String str3) {
        Map loadDataByParam = MatchCacheManager.loadDataByParam("msg_id=?", new String[]{str}, new String[]{"msg_id", "msg_num_md5", "urls_result", "urls_lasttime"});
        if (loadDataByParam != null) {
            try {
                JSONObject jSONObject = (JSONObject) loadDataByParam.get(str);
                if (jSONObject != null) {
                    if (MatchCacheManager.getMD5(str2, str3).equalsIgnoreCase(jSONObject.optString("msg_num_md5"))) {
                        String optString = jSONObject.optString("urls_result");
                        if (!StringUtils.isNull(optString)) {
                            return new JSONObject(optString);
                        }
                    }
                }
            } catch (Throwable th) {
            }
        }
        return null;
    }

    private static boolean b(JSONObject jSONObject) {
        if (jSONObject == null) {
            return false;
        }
        try {
            if (!StringUtils.isNull(jSONObject.optString("urls"))) {
                int optInt = jSONObject.optInt("validStatus", RESULT_NOT_FIND);
                if (optInt < -1 || optInt > 2) {
                    return false;
                }
            }
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    public static void checkUrlFromMsg(String str, String str2, String str3, String str4, long j, Map<String, String> map, SdkCallBack sdkCallBack, boolean z) {
        if (str == null || str2 == null || str4 == null) {
            throw new Exception("msgid or phoneNum or smsContent is null but they need value.");
        }
        D b = D.b(str2);
        if (((JSONObject) b.p.get(str)) != null) {
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(0), (JSONObject) b.p.get(str), str, Integer.valueOf(16));
        } else if (b.q.contains(str)) {
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), " inQueueUrlsData", str, Integer.valueOf(16));
        } else {
            try {
                JSONObject jSONObject = (JSONObject) b.o.remove(str);
                if (jSONObject != null) {
                    String optString = jSONObject.optString("urls_result");
                    if (!StringUtils.isNull(optString)) {
                        JSONObject jSONObject2 = new JSONObject(optString);
                        String str5 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_num_md5");
                        optString = MatchCacheManager.getMD5(str2, str4);
                        if (optString != null && str5 != null && optString.equals(str5) && b(jSONObject2)) {
                            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(0), jSONObject2, str, Integer.valueOf(16));
                            return;
                        }
                    }
                }
            } catch (Throwable th) {
            }
            if (z) {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-4), " is scrolling", str, Integer.valueOf(16));
                return;
            }
            b.q.add(str);
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-2), " need parse", str, Integer.valueOf(16));
            a.c().execute(new h(str, str2, str4, map, sdkCallBack, b, j));
        }
    }

    public static int checkValidUrl(String str, String str2, String str3, String str4, Map<String, String> map, XyCallBack xyCallBack) {
        HashMap hashMap = new HashMap();
        int checkNetWork = XyUtil.checkNetWork(Constant.getContext(), 2);
        try {
            if (StringUtils.isNull(str4)) {
                XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-4));
                if (checkNetWork != -1) {
                    updateCheckValidUrl();
                }
                return -4;
            }
            String[] split = str4.split("_ARR_");
            if (split != null) {
                if (split.length > 0) {
                    boolean z = DuoquUtils.getSdkDoAction().checkValidUrl(str, str2, str3, str4, map) != RESULT_NOT_IMPL;
                    Map queryLocal = queryLocal(str, str2, str3, str4, split, z, map);
                    int intValue;
                    if (queryLocal.containsKey("localfind")) {
                        XyUtil.doXycallBackResult(xyCallBack, (Integer) queryLocal.get("result"));
                        intValue = r0.intValue();
                        if (checkNetWork != -1) {
                            updateCheckValidUrl();
                        }
                        return intValue;
                    } else if (z) {
                        intValue = thirdValid(str, str2, str3, str4, split, map);
                        MatchCacheManager.updateCheckStatu(str4, intValue);
                        XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(intValue));
                        if (checkNetWork != -1) {
                            updateCheckValidUrl();
                        }
                        return intValue;
                    } else if (checkNetWork != -1) {
                        NetUtil.executeRunnable(new b(str, str2, str3, str4, split, map, hashMap, xyCallBack));
                        if (checkNetWork != -1) {
                            updateCheckValidUrl();
                        }
                        if (hashMap.containsKey(str4)) {
                            XyUtil.doXycallBackResult(xyCallBack, hashMap.remove(str4));
                            return ((Integer) hashMap.remove(str4)).intValue();
                        }
                        XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(RESULT_NOT_FIND));
                        return RESULT_NOT_FIND;
                    } else {
                        XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(RESULT_NO_NET));
                        if (checkNetWork != -1) {
                            updateCheckValidUrl();
                        }
                        return RESULT_NO_NET;
                    }
                }
            }
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-4));
            if (checkNetWork != -1) {
                updateCheckValidUrl();
            }
            return -4;
        } catch (Throwable th) {
            if (checkNetWork != -1) {
                updateCheckValidUrl();
            }
        }
    }

    public static void checkValidUrlNet(String str, String str2, String str3, String str4, String[] strArr, Map<String, String> map, boolean z, HashMap<String, Integer> hashMap, XyCallBack xyCallBack) {
        String a = j.a(str2, strArr);
        try {
            if (!StringUtils.isNull(a)) {
                NetUtil.requestNewTokenIfNeed(map);
                NetUtil.executeNewServiceHttpRequest(NetUtil.URL_VALIDITY, a, new c(a, z, map, NetUtil.getToken(), hashMap, str4, xyCallBack), z, false, true, map);
            }
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(RESULT_SERVER_ERROR));
        }
    }

    public static void checkValidUrlNetBatch(JSONArray jSONArray, boolean z) {
        if (jSONArray != null) {
            try {
                if (jSONArray.length() > 0) {
                    String a = j.a(jSONArray);
                    if (!StringUtils.isNull(a)) {
                        NetUtil.requestNewTokenIfNeed(null);
                        NetUtil.executeNewServiceHttpRequest(NetUtil.URL_VALIDITY, a, new g(a, z, NetUtil.getToken()), z, false, true, null);
                    }
                    synchronized (MsgUrlService.class) {
                        a = false;
                    }
                    return;
                }
            } catch (Throwable th) {
                synchronized (MsgUrlService.class) {
                    a = false;
                }
            }
        }
        synchronized (MsgUrlService.class) {
            a = false;
        }
    }

    public static void executeRunnable(Runnable runnable) {
        a.h.execute(runnable);
    }

    public static String pickUrlFromMsg(String str, String str2, String str3, String str4, Map<String, String> map) {
        String catchUrls = DexUtil.catchUrls(str3, null);
        executeRunnable(new a(catchUrls));
        return catchUrls;
    }

    public static void putUrlsResultData(String str, Map<String, JSONObject> map, boolean z) {
        D b = D.b(str);
        synchronized (b.o) {
            if (z) {
                try {
                    b.o.clear();
                    b.p.clear();
                    b.q.clear();
                } catch (Throwable th) {
                }
            }
            if (map != null) {
                b.o.putAll(map);
            }
        }
    }

    public static Map<String, Object> queryLocal(String str, String str2, String str3, String str4, String[] strArr, boolean z, Map<String, String> map) {
        Map hashMap = new HashMap();
        if (strArr.length != 1) {
            HashMap a = l.a(strArr, z);
            int intValue = ((Integer) a.get("statu")).intValue();
            if ((!a.containsKey("hasNotCheck") ? null : 1) != null) {
                Object obj;
                if (z ? intValue >= 0 : intValue >= 0) {
                    obj = null;
                } else {
                    int i = 1;
                }
                checkValidUrlNet(str, str2, str3, str4, strArr, map, true, null, null);
                if (obj != null) {
                    hashMap.put("result", Integer.valueOf(-1));
                    hashMap.put("localfind", Boolean.valueOf(true));
                    return hashMap;
                }
            }
            hashMap.put("result", Integer.valueOf(intValue));
            hashMap.put("localfind", Boolean.valueOf(true));
            return hashMap;
        }
        int a2 = l.a(str4, z);
        if (a2 != 0) {
            hashMap.put("result", Integer.valueOf(a2));
            hashMap.put("localfind", Boolean.valueOf(true));
            return hashMap;
        }
        return hashMap;
    }

    public static void saveUrlResult(JSONArray jSONArray, String str, int i) {
        if (jSONArray != null && jSONArray.length() > 0) {
            l.a(jSONArray);
            MatchCacheManager.updateCheckStatu(str, i);
        }
    }

    public static void saveUrlResult(JSONArray jSONArray, String str, int i, boolean z) {
        if (jSONArray == null || jSONArray.length() <= 0) {
            return;
        }
        if (z) {
            executeRunnable(new d(jSONArray, str, i));
        } else {
            saveUrlResult(jSONArray, str, i);
        }
    }

    public static int thirdValid(String str, String str2, String str3, String str4, String[] strArr, Map<String, String> map) {
        if (strArr.length != 1) {
            int i = 0;
            int i2 = -1;
            int i3 = Integer.MAX_VALUE;
            while (i < strArr.length) {
                try {
                    int checkValidUrl;
                    checkValidUrl = DuoquUtils.getSdkDoAction().checkValidUrl(str, str2, str3, strArr[i], map);
                    l.a(strArr[i], checkValidUrl);
                    int i4 = checkValidUrl > i3 ? i3 : checkValidUrl;
                    if (checkValidUrl >= 0) {
                        i2 = i;
                        i3 = i4;
                        i++;
                    } else {
                        a(str, str2, str3, strArr, i, (Map) map);
                        return -1;
                    }
                } catch (Throwable th) {
                    Throwable th2 = th;
                    a(str, str2, str3, strArr, i2, (Map) map);
                }
            }
            a(str, str2, str3, strArr, i2, (Map) map);
            return i3;
        }
        checkValidUrl = DuoquUtils.getSdkDoAction().checkValidUrl(str, str2, str3, str4, map);
        l.a(str4, checkValidUrl);
        return checkValidUrl;
    }

    public static synchronized void updateCheckValidUrl() {
        synchronized (MsgUrlService.class) {
            if (a) {
                return;
            }
            a = true;
            executeRunnable(new f());
        }
    }
}
