package cn.com.xy.sms.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Process;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.action.AbsSdkDoAction;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.a.f;
import cn.com.xy.sms.sdk.db.entity.h;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.NetWebUtil;
import cn.com.xy.sms.sdk.net.l;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.service.a.b;
import cn.com.xy.sms.sdk.service.e.g;
import cn.com.xy.sms.sdk.service.f.a;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.ConversationManager;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.DateUtils;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.PopupMsgManager;
import cn.com.xy.sms.sdk.util.PopupUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.sdk.util.k;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseManager {
    public static final String UPDATE_ICCID_INFO_CACHE_ACTION = "cn.com.xy.sms.iccidinfo.action";
    private static final String a = "yyyyMMdd";
    private static boolean b = false;
    private static BroadcastReceiver c;
    public static long checkDataForUpdateTime = 0;
    private static HashMap<String, Long> d = new HashMap();
    public static boolean isCheckDataForUpdate = false;
    public static boolean isupdateData = false;
    public static long mins = 1;
    public static long updateDataTime = 0;

    static Map<String, Object> a(Context context, String str, String str2, String str3, long j, Map<String, String> map) {
        return b.b(context, str, str2, str3, j, map);
    }

    private static Map a(Map map) {
        if (map == null) {
            map = new HashMap();
        }
        try {
            KeyManager.initAppKey();
            map.put(Constant.CHANNEL, l.b);
        } catch (Throwable th) {
        }
        return map;
    }

    static /* synthetic */ void a(String str, String str2) {
        String stringBuilder = new StringBuilder(String.valueOf(str2)).append("_").append(str).toString();
        SysParamEntityManager.insertOrUpdateKeyValue(Constant.getContext(), "bubbleViewVersion", stringBuilder, null);
        SysParamEntityManager.cacheMap.put("bubbleViewVersion", stringBuilder);
    }

    public static String addQueryTrafficAndChargeToMenuData(String str, Map<String, String> map) {
        return f.a(str, (Map) map);
    }

    private static BitmapDrawable b(Context context, String str, String str2, String str3, String str4, int i, int i2, SdkCallBack sdkCallBack) {
        BitmapDrawable bitmapDrawable = null;
        try {
            if (NetUtil.checkAccessNetWork(2)) {
                d.put(str4, Long.valueOf(System.currentTimeMillis()));
                if (cn.com.xy.sms.sdk.util.f.a(str, str2, str3, true) == 0) {
                    d.remove(str4);
                    bitmapDrawable = ViewUtil.createBitmapByPath2(context, str4, i, i2);
                    if (sdkCallBack != null) {
                        sdkCallBack.execute(bitmapDrawable);
                    }
                    if (bitmapDrawable != null) {
                        d.remove(str4);
                    }
                } else if (sdkCallBack != null) {
                    sdkCallBack.execute(null);
                }
                return bitmapDrawable;
            }
            if (sdkCallBack != null) {
                sdkCallBack.execute(null);
            }
            return null;
        } catch (Throwable th) {
        }
    }

    private static void b(String str, String str2) {
        String stringBuilder = new StringBuilder(String.valueOf(str2)).append("_").append(str).toString();
        SysParamEntityManager.insertOrUpdateKeyValue(Constant.getContext(), "bubbleViewVersion", stringBuilder, null);
        SysParamEntityManager.cacheMap.put("bubbleViewVersion", stringBuilder);
    }

    public static void checkDataForUpdate(Map<String, String> map, SdkCallBack sdkCallBack) {
        boolean z = true;
        try {
            if (System.currentTimeMillis() - checkDataForUpdateTime > Constant.sqlUpdateTimeCyc) {
                z = false;
            }
            if (!z) {
            }
            if (isCheckDataForUpdate) {
                XyUtil.doXycallBack(sdkCallBack, Constant.ACTION_PARSE);
                isCheckDataForUpdate = false;
                return;
            }
            checkDataForUpdateTime = System.currentTimeMillis();
            isCheckDataForUpdate = true;
            if (!NetUtil.checkAccessNetWork((Map) map)) {
                XyUtil.doXycallBack(sdkCallBack, ThemeUtil.SET_NULL_STR);
            } else if (!ParseItemManager.isInitData()) {
                XyUtil.doXycallBack(sdkCallBack, "0");
            } else if (k.b(false, false)) {
                XyUtil.doXycallBack(sdkCallBack, "1");
                g.a((Map) map, null);
            } else {
                g.a((Map) map, (XyCallBack) sdkCallBack);
            }
            isCheckDataForUpdate = false;
        } catch (Throwable th) {
            new StringBuilder("checkDataForUpdate: ").append(th.getMessage());
        } finally {
            isCheckDataForUpdate = false;
        }
    }

    public static boolean checkStationList(String str, String str2, String str3, boolean z) {
        return a.a(str, str2, str3);
    }

    public static void cleanToken(Context context) {
        try {
            Constant.initContext(context);
            DBManager.delete(SysParamEntityManager.TABLE_NAME, "p_key = ?", new String[]{Constant.NEWHTTPTOKEN});
            DBManager.delete(SysParamEntityManager.TABLE_NAME, "p_key = ?", new String[]{Constant.HTTPTOKEN});
            DBManager.delete(SysParamEntityManager.TABLE_NAME, "p_key = ?", new String[]{Constant.AESKEY});
        } catch (Throwable th) {
        }
    }

    public static void clearHistorySmsByNum(Context context, String str, Map<String, String> map) {
        PopupMsgManager.removeBusinessMessageByNum(context, str, false, map);
    }

    public static void clearRecognisedSdkCache(String str) {
        if (StringUtils.isNull(str)) {
            D.d();
        } else {
            D.f(str);
        }
    }

    public static void deleteMatchCache(String str, long j) {
        try {
            MatchCacheManager.deleteMatchCache(str, j);
        } catch (Throwable th) {
        }
    }

    public static void deleteMatchCache(String str, String str2, long j) {
        try {
            MatchCacheManager.deleteMatchCache(str, str2, j);
        } catch (Throwable th) {
        }
    }

    public static boolean doAction(Activity activity, String str, Map<String, String> map) {
        return DuoquUtils.doAction(activity, str, map);
    }

    public static BitmapDrawable findLogoByLogoName(Context context, String str, int i, int i2, int i3, Map<String, String> map, SdkCallBack sdkCallBack) {
        Throwable th;
        BitmapDrawable bitmapDrawable = null;
        String a = w.a();
        BitmapDrawable createBitmapByPath2;
        Long l;
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.util.ParseManager", "findLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), map, sdkCallBack);
            String path = Constant.getPath(Constant.DUOQU_PUBLIC_LOGO_DIR);
            String stringBuilder = new StringBuilder(String.valueOf(path)).append(str).toString();
            File file = new File(stringBuilder);
            if (file.exists()) {
                createBitmapByPath2 = ViewUtil.createBitmapByPath2(context, file, i, i2);
                if (createBitmapByPath2 == null) {
                    try {
                        d.put(stringBuilder, Long.valueOf(System.currentTimeMillis()));
                        XyUtil.doXycallBackResult(sdkCallBack, null);
                        ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "findLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), map, sdkCallBack, createBitmapByPath2);
                        l = (Long) d.get("runResourseQueue");
                        if (l != null) {
                            if ((System.currentTimeMillis() <= l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                            }
                            return null;
                        }
                        i.a(new cn.com.xy.sms.sdk.queue.k(7, new String[0]));
                        d.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
                        return null;
                    } catch (Throwable th2) {
                        bitmapDrawable = createBitmapByPath2;
                        th = th2;
                        ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "findLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), map, sdkCallBack, bitmapDrawable);
                        l = (Long) d.get("runResourseQueue");
                        if (l != null) {
                            if ((System.currentTimeMillis() > l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? null : 1) == null) {
                            }
                            throw th;
                        }
                        i.a(new cn.com.xy.sms.sdk.queue.k(7, new String[0]));
                        d.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
                        throw th;
                    }
                }
                XyUtil.doXycallBackResult(sdkCallBack, createBitmapByPath2);
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "findLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), map, sdkCallBack, createBitmapByPath2);
                l = (Long) d.get("runResourseQueue");
                if (l != null) {
                    if ((System.currentTimeMillis() <= l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                    }
                    return createBitmapByPath2;
                }
                i.a(new cn.com.xy.sms.sdk.queue.k(7, new String[0]));
                d.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
                return createBitmapByPath2;
            }
            l = (Long) d.get(stringBuilder);
            if (l != null) {
                if ((System.currentTimeMillis() >= l.longValue() + DexUtil.getUpdateCycleByType(19, (mins * 60) * 1000) ? 1 : null) == null) {
                    XyUtil.doXycallBackResult(sdkCallBack, null);
                    ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "findLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), map, sdkCallBack, null);
                    l = (Long) d.get("runResourseQueue");
                    if (l != null) {
                        if ((System.currentTimeMillis() <= l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                        }
                        return null;
                    }
                    i.a(new cn.com.xy.sms.sdk.queue.k(7, new String[0]));
                    d.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
                    return null;
                }
            }
            String str2 = NetUtil.BIZPORT_DOWN_URL + str;
            boolean z = false;
            if (map != null) {
                if (!map.isEmpty()) {
                    z = "true".equals(map.get("syn"));
                }
            }
            if (z) {
                bitmapDrawable = b(context, str2, path, str, stringBuilder, i, i2, sdkCallBack);
            } else {
                cn.com.xy.sms.sdk.a.a.b().execute(new j(context, str2, path, str, stringBuilder, i, i2, sdkCallBack));
            }
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "findLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), map, sdkCallBack, bitmapDrawable);
            l = (Long) d.get("runResourseQueue");
            if (l != null) {
                if ((System.currentTimeMillis() <= l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? 1 : null) == null) {
                }
                i.a(new cn.com.xy.sms.sdk.queue.k(12, ParseItemManager.STATE, "256"));
                return bitmapDrawable;
            }
            i.a(new cn.com.xy.sms.sdk.queue.k(7, new String[0]));
            d.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
            i.a(new cn.com.xy.sms.sdk.queue.k(12, ParseItemManager.STATE, "256"));
            return bitmapDrawable;
        } catch (Throwable th22) {
            th = th22;
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "findLogoByLogoName", context, str, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), map, sdkCallBack, bitmapDrawable);
            l = (Long) d.get("runResourseQueue");
            if (l != null) {
                if (System.currentTimeMillis() > l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000)) {
                }
                if ((System.currentTimeMillis() > l.longValue() + DexUtil.getUpdateCycleByType(20, 3600000) ? null : 1) == null) {
                }
                throw th;
            }
            i.a(new cn.com.xy.sms.sdk.queue.k(7, new String[0]));
            d.put("runResourseQueue", Long.valueOf(System.currentTimeMillis()));
            throw th;
        }
    }

    public static boolean geOnOffByType(int i) {
        return DexUtil.geOnOffByType(i);
    }

    public static String getAlgorithmVerion() {
        String str = "";
        try {
            str = h.a();
            if (StringUtils.isNull(str)) {
                return "";
            }
            str = new SimpleDateFormat("yyyyMMddHH").format(new SimpleDateFormat("yyyyMMddHHmmss").parse(str));
            return str;
        } catch (Throwable th) {
        }
    }

    public static String getBubbleViewVersion(Map<String, Object> map) {
        String str = Constant.bubble_version;
        if (StringUtils.isNull(Constant.current_bubble_version)) {
            Constant.current_bubble_version = str;
            return str;
        }
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), "bubbleViewVersion");
        if (!StringUtils.isNull(stringParam)) {
            String[] split = stringParam.split("_");
            str = split[0];
            stringParam = split[1];
            Constant.current_bubble_version = str;
            String currentTimeString = DateUtils.getCurrentTimeString(a);
            if (DateUtils.compareDateString(currentTimeString, stringParam, a)) {
                k kVar = new k(currentTimeString);
            }
        }
        return str;
    }

    public static JSONArray getConfigByType(int i, String str, Integer num) {
        return DexUtil.getConfigByType(i, str, num);
    }

    public static int getOperatorByNum(String str) {
        return !StringUtils.isNull(str) ? IccidLocationUtil.getOperatorByNum(StringUtils.getPhoneNumberNo86(str)) : -1;
    }

    public static int getOperatorNumByPubNum(String str) {
        return f.c(str);
    }

    public static int getParseVersion(Context context, Map map) {
        try {
            String paramValue = SdkParamUtil.getParamValue(context, "PARSE_VERSION");
            if (!StringUtils.isNull(paramValue)) {
                return Integer.parseInt(paramValue);
            }
        } catch (Throwable th) {
        }
        return 0;
    }

    public static String getRecogniseActionConfig(JSONObject jSONObject, Map map) {
        if (jSONObject == null || !n.a((byte) Constant.POWER_SMS_SPECIAL_VALUE)) {
            return null;
        }
        try {
            return DexUtil.getRecogniseActionConfig(jSONObject, map);
        } catch (Throwable th) {
            return null;
        }
    }

    public static String getSdkVersion() {
        return NetUtil.APPVERSION;
    }

    public static JSONObject getSmsType(Context context, String str, String str2, String str3, Map<String, String> map) {
        return b.a(context, str, str2, str3, map);
    }

    public static String getUIVersion() {
        return DexUtil.getUIVersion();
    }

    public static void initSdk(Context context, String str, String str2, boolean z, boolean z2, Map<String, String> map) {
        if (context == null) {
            throw new Exception("context is null,please check.");
        } else if (map != null && map.containsKey(Constant.RSAPRVKEY)) {
            Object obj;
            Runnable hVar;
            Constant.initContext(context);
            b.a(context);
            String curProcessName = AbsSdkDoAction.getCurProcessName(Process.myPid(), context);
            if (map != null) {
                try {
                    if (Boolean.FALSE.toString().equalsIgnoreCase((String) map.get(Constant.INIT_MAIN_PROCCESS))) {
                        obj = null;
                        if (obj == null || context.getPackageName().equals(curProcessName)) {
                            n.a();
                            hVar = new h(context, str, str2, z, z2, map);
                            if (map != null && map.containsKey("SYNCHRONIZED")) {
                                hVar.run();
                                return;
                            } else {
                                NetUtil.executeRunnable(hVar);
                            }
                        }
                        context.getPackageName();
                        return;
                    }
                } catch (Throwable th) {
                }
            }
            obj = 1;
            if (obj == null) {
                context.getPackageName();
                return;
            }
            n.a();
            hVar = new h(context, str, str2, z, z2, map);
            if (map != null) {
                hVar.run();
                return;
            }
            NetUtil.executeRunnable(hVar);
        } else {
            throw new Exception("rsa key is null,please check.");
        }
    }

    public static boolean isEnterpriseSms(Context context, String str, String str2, Map<String, String> map) {
        return (cn.com.xy.sms.sdk.util.f.c(Constant.getPARSE_PATH(), "parseUtilMain", "jar") && n.a(Constant.getJarPath()).booleanValue()) ? DexUtil.isEnterpriseSms(context, str, str2, map) : PopupUtil.isEnterpriseSms(context, str, str2, map);
    }

    public static boolean isFixedPhone(Context context, String str, Map<String, String> map) {
        return PopupUtil.isFixedPhone(str);
    }

    public static boolean isInitData() {
        return ParseItemManager.isInitData();
    }

    public static boolean isVerifyCodeSms(Context context, String str, String str2, String str3, Map<String, String> map) {
        return cn.com.xy.sms.sdk.service.g.a.a(context, str, str2, str3, map);
    }

    public static boolean ismUseNewDes() {
        return b;
    }

    public static HashMap<String, JSONObject> loadAllPubInfo(Set<String> set) {
        return f.a((Set) set);
    }

    public static HashMap<String, String[]> loadAllPubNum(Set<String> set) {
        return f.b((Set) set);
    }

    public static void loadLocation(String str, int i, String str2, boolean z) {
        IccidLocationUtil.queryIccid(null, str, str2, z, true);
    }

    public static boolean needUpdateJar() {
        return h.g();
    }

    public static void parseMsgCallBack(Context context, String str, String str2, String str3, Map<String, String> map) {
        try {
            ParseSmsToBubbleUtil.backGroundHandleMapByType(map, b.b(context, str, str2, str3, 0, map));
        } catch (Throwable th) {
            RuntimeException runtimeException = new RuntimeException(th);
        }
    }

    public static String parseMsgToBubble(Context context, String str, String str2, String str3, Map<String, String> map) {
        String a;
        String str4;
        Throwable th;
        Object obj = null;
        String a2 = w.a();
        Map hashMap;
        try {
            ConversationManager.saveLogIn(a2, "cn.com.xy.sms.util.ParseManager", "parseMsgToBubble", context, str, str2, str3, map);
            if (n.a((byte) 2)) {
                hashMap = map != null ? map : new HashMap();
                try {
                    hashMap.put("popup_type", "2");
                    long j = 0;
                    if (hashMap.containsKey("msgTime")) {
                        try {
                            j = Long.parseLong((String) hashMap.get("msgTime"));
                        } catch (Exception e) {
                        }
                    }
                    Map a3 = a(context, str, str2, str3, j, hashMap);
                    if (a3 != null) {
                        a = b.a(a3, hashMap);
                        if (a == null) {
                            str4 = a;
                        } else {
                            try {
                                i.a(new cn.com.xy.sms.sdk.queue.k(12, ParseItemManager.STATE, "64"));
                                ConversationManager.saveLogOut(a2, "cn.com.xy.sms.util.ParseManager", "parseMsgToBubble", context, str, str2, str3, hashMap, a);
                                return a;
                            } catch (Throwable th2) {
                                str4 = a;
                                th = th2;
                                ConversationManager.saveLogOut(a2, "cn.com.xy.sms.util.ParseManager", "parseMsgToBubble", context, str, str2, str3, hashMap, obj);
                                throw th;
                            }
                        }
                    }
                    ConversationManager.saveLogOut(a2, "cn.com.xy.sms.util.ParseManager", "parseMsgToBubble", context, str, str2, str3, hashMap, obj);
                } catch (Throwable th3) {
                    th = th3;
                    ConversationManager.saveLogOut(a2, "cn.com.xy.sms.util.ParseManager", "parseMsgToBubble", context, str, str2, str3, hashMap, obj);
                    throw th;
                }
                return null;
            }
            ConversationManager.saveLogOut(a2, "cn.com.xy.sms.util.ParseManager", "parseMsgToBubble", context, str, str2, str3, map, null);
            return null;
        } catch (Throwable th4) {
            th = th4;
            hashMap = map;
            ConversationManager.saveLogOut(a2, "cn.com.xy.sms.util.ParseManager", "parseMsgToBubble", context, str, str2, str3, hashMap, obj);
            throw th;
        }
    }

    public static Map<String, Object> parseMsgToBubbleCardResult(Context context, String str, String str2, String str3, String str4, long j, byte b, Map<String, String> map) {
        if (!n.a((byte) 9)) {
            return null;
        }
        Map<String, Object> a = a(context, str2, str3, str4, j, putValueToMap(map, "msgId", str));
        if (ParseBubbleManager.getParseStatu(a) == -1) {
            return a;
        }
        if (!ParseItemManager.execNqSql) {
            return PopupUtil.parseMsgToBubbleCardResult(context, str, str2, str3, str4, j, b, a, false);
        }
        if (a == null) {
            a = new HashMap();
        }
        a.clear();
        a.put("parseStatu", Constant.ACTION_PARSE);
        return a;
    }

    public static Map<String, Object> parseMsgToMap(Context context, String str, String str2, String str3, Map<String, String> map) {
        Map hashMap = map != null ? map : new HashMap();
        hashMap.put("popup_type", "2");
        return a(context, str, str2, str3, 0, hashMap);
    }

    public static Map<String, Object> parseMsgToPopupWindow(Context context, String str, String str2, String str3, Map<String, String> map) {
        return parseMsgToPopupWindow(context, str, str2, str3, false, map);
    }

    public static Map<String, Object> parseMsgToPopupWindow(Context context, String str, String str2, String str3, Map<String, Object> map, boolean z, Map<String, String> map2) {
        String str4;
        Map map3;
        if (map != null && map.size() > 1) {
            Map<String, Object> map4;
            if (!(map2 == null || map2.isEmpty())) {
                if (z) {
                    str4 = (String) map2.get("msgId");
                    String str5 = (String) map2.get("msgTime");
                    if (!(StringUtils.isNull(str4) || StringUtils.isNull(str5))) {
                        ParseSmsToBubbleUtil.backGroundParseSmsBubble(str4, str, str3, str2, Long.valueOf(str5).longValue(), false, true, map, map2);
                    }
                }
                ParseSmsToBubbleUtil.backGroundHandleMapByType(map2, map);
                str4 = (String) map2.get(Constant.POPUP_SHOW_MASTER);
                if (str4 != null) {
                    if ("0".equals(str4.trim())) {
                        return PopupUtil.getResultMap(false, true);
                    }
                    if ("1".equals(str4.trim())) {
                        str4 = (String) map.get("title_num");
                        if (str4.startsWith("01")) {
                            str4 = (String) map2.get(Constant.POPUP_SHOW_BANK);
                            if (str4 != null && "0".equals(str4.trim())) {
                                return PopupUtil.getResultMap(false, true);
                            }
                        } else if (str4.startsWith("02")) {
                            str4 = (String) map2.get(Constant.POPUP_SHOW_SP);
                            if (str4 != null && "0".equals(str4.trim())) {
                                return PopupUtil.getResultMap(false, true);
                            }
                        } else {
                            str4 = (String) map2.get(Constant.POPUP_SHOW_LIFE);
                            if (str4 != null && "0".equals(str4.trim())) {
                                return PopupUtil.getResultMap(false, true);
                            }
                        }
                    } else if ("2".equals(str4.trim())) {
                        Object obj;
                        String[] strArr = new String[]{"01025", "02044", "03006", "03015", "04010", "05035", "08104", "12003", "13004", "15003", "16002", "17005", "00000"};
                        str4 = (String) map.get("title_num");
                        for (int i = 0; i < 13; i++) {
                            if (str4.startsWith(strArr[i])) {
                                obj = 1;
                                break;
                            }
                        }
                        obj = null;
                        if (obj == null) {
                            return PopupUtil.getResultMap(false, true);
                        }
                    }
                }
            }
            Map<String, Object> parseMsgToPopupWindow = PopupUtil.parseMsgToPopupWindow(context, str, str3, map);
            if (ViewUtil.getChannelType() != 3) {
                map4 = parseMsgToPopupWindow;
            } else {
                map3 = null;
                if (map2 != null) {
                    map3 = new HashMap();
                    map3.putAll(map2);
                }
                map4 = PopupUtil.getResultMap(parseMsgToPopupWindow, str, str3, map3, context);
            }
            return map4;
        } else if (ViewUtil.getChannelType() != 3) {
            return PopupUtil.getResultMap(false, false);
        } else {
            if (!(map2 == null || map2.isEmpty())) {
                str4 = (String) map2.get(Constant.POPUP_SHOW_MASTER);
                if (str4 != null && "0".equals(str4.trim())) {
                    return PopupUtil.getResultMap(false, false);
                }
            }
            map3 = null;
            if (map2 != null) {
                map3 = new HashMap();
                map3.putAll(map2);
            }
            return PopupUtil.getResultMap(PopupUtil.getResultMap(false, false), str, str3, map3, context);
        }
    }

    public static Map<String, Object> parseMsgToPopupWindow(Context context, String str, String str2, String str3, boolean z, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            if (!n.a((byte) 3)) {
                PopupUtil.getResultMap(false, false);
            }
            Map hashMap = map != null ? map : new HashMap();
            hashMap.put("popup_type", "1");
            if (!hashMap.containsKey("from")) {
                hashMap.put("from", "1");
            }
            return parseMsgToPopupWindow(context, str, str2, str3, a(context, str, str2, str3, 0, hashMap), z, hashMap);
        } else {
            throw new Exception(" smsContent is null.");
        }
    }

    public static Map<String, Object> parseMsgToRichAndSimpleBubble(Context context, String str, String str2, String str3, String str4, long j, byte b, Map<String, String> map) {
        if (!n.a((byte) 9)) {
            return null;
        }
        Map b2 = b.b(context, str2, str3, str4, j, map);
        return ParseBubbleManager.getParseStatu(b2) != -1 ? PopupUtil.parseMsgToBubbleCardResult(context, str, str2, str3, str4, j, b, b2, false) : null;
    }

    public static JSONObject parseRecogniseValue(String str, String str2, long j, Map map) {
        if (StringUtils.isNull(str2) || !n.a((byte) Constant.POWER_SMS_SPECIAL_VALUE)) {
            return null;
        }
        try {
            String parseRecogniseValue = DexUtil.parseRecogniseValue(str, str2, j, a(map));
            if (StringUtils.isNull(parseRecogniseValue)) {
                return null;
            }
            JSONObject jSONObject = new JSONObject(parseRecogniseValue);
            JSONArray optJSONArray = jSONObject.optJSONArray("items");
            return (optJSONArray != null && optJSONArray.length() > 0) ? jSONObject : null;
        } catch (Throwable th) {
            return null;
        }
    }

    public static int parseSensitive(String str) {
        if (StringUtils.isNull(str)) {
            return 0;
        }
        try {
            return DexUtil.parseSensitive(str);
        } catch (Throwable th) {
            return 0;
        }
    }

    public static String parseSmsToClassify(Context context, String str, String str2, String str3, String str4, Map<String, String> map) {
        return n.a((byte) 6) ? g.a(a(context, str, str2, str3, 0, map)) : null;
    }

    public static int parseSmsType(Context context, String str, String str2, String str3, Map<String, String> map, int i) {
        Map hashMap = map != null ? map : new HashMap();
        try {
            hashMap.put("popup_type", "2");
            Map b = b.b(context, str, str3, str2, System.currentTimeMillis(), hashMap);
            if (b != null) {
                if (ParseBubbleManager.getParseStatu(b) != -1) {
                    return DexUtil.getSmsTypeByMap(b, i);
                }
            }
        } catch (Throwable th) {
        }
        return -1;
    }

    public static Map<String, Object> parseValidCodeSms(String str, String str2, long j) {
        return cn.com.xy.sms.sdk.service.g.a.a(str, str2, j);
    }

    public static Map<String, String> putValueToMap(Map<String, String> map, String str, String str2) {
        if (map == null) {
            map = new HashMap();
        }
        map.put(str, str2);
        return map;
    }

    public static void queryAllSimCardTrafficAndChargeActionData(Context context, String str, SdkCallBack sdkCallBack) {
        try {
            HashMap iccidAreaCodeMap = IccidLocationUtil.getIccidAreaCodeMap();
            if (iccidAreaCodeMap != null) {
                for (Entry key : iccidAreaCodeMap.entrySet()) {
                    String str2 = (String) key.getKey();
                    queryMenuByPhoneNum(context, str, 1, str2, null, new l(sdkCallBack, str2));
                }
                return;
            }
            sdkCallBack.execute(Integer.valueOf(0), "iccidMap is null");
        } catch (Throwable th) {
            sdkCallBack.execute(Integer.valueOf(0), "error:" + th.getMessage());
        }
    }

    public static void queryCommingMovie(int i, String str, SdkCallBack sdkCallBack) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("count", i);
            jSONObject.put("sdkVersion", NetUtil.APPVERSION);
            jSONObject.put("channel", KeyManager.getAppKey());
            jSONObject.put(NumberInfo.VERSION_KEY, str);
            NetWebUtil.sendPostRequest(NetWebUtil.WEB_SERVER_URL_COMMING_MOVIE, jSONObject.toString(), sdkCallBack);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(sdkCallBack, new Object[0]);
        }
    }

    public static String queryDefService(Context context) {
        return SysParamEntityManager.queryValueParamKey(context, "defService");
    }

    public static void queryDiscoverData(String str, String str2, String str3, SdkCallBack sdkCallBack) {
        if (StringUtils.isNull(str2)) {
            XyUtil.doXycallBackResult(sdkCallBack, new Object[0]);
            return;
        }
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("city", str);
            jSONObject.put("sceneId", str2);
            jSONObject.put("sdkVersion", NetUtil.APPVERSION);
            jSONObject.put("channel", KeyManager.getAppKey());
            jSONObject.put(NumberInfo.VERSION_KEY, str3);
            NetWebUtil.sendPostRequest(NetWebUtil.WEB_SERVER_URL_DISCOVER, jSONObject.toString(), sdkCallBack);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(sdkCallBack, new Object[0]);
        }
    }

    public static void queryFlightData(String str, String str2, String str3, Map<String, Object> map, SdkCallBack sdkCallBack) {
        a.a(str, str2, str3, map, sdkCallBack);
    }

    public static void queryFlightData(String str, String str2, Map<String, Object> map, SdkCallBack sdkCallBack) {
        a.a(str, str2, sdkCallBack);
    }

    public static String queryMenuByPhoneNum(Context context, String str, int i, String str2, Map<String, String> map) {
        if (!n.a((byte) 4)) {
            return null;
        }
        if (StringUtils.isPhoneNumber(str)) {
            return null;
        }
        String a = w.a();
        String str3 = "";
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.util.ParseManager", "queryMenuByPhoneNum", context, str, Integer.valueOf(i), str2, map);
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "queryMenuByPhoneNum", context, str, Integer.valueOf(i), str2, map, g.a(str, i, str2, (Map) map, null));
            return g.a(str, i, str2, (Map) map, null);
        } catch (Throwable th) {
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "queryMenuByPhoneNum", context, str, Integer.valueOf(i), str2, map, str3);
        }
    }

    public static String queryMenuByPhoneNum(Context context, String str, int i, String str2, Map<String, String> map, SdkCallBack sdkCallBack) {
        if (!n.a((byte) 4)) {
            return null;
        }
        String a = w.a();
        String str3 = "";
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.util.ParseManager", "queryMenuByPhoneNum", context, str, Integer.valueOf(i), str2, map, sdkCallBack);
            try {
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "queryMenuByPhoneNum", context, str, Integer.valueOf(i), str2, map, sdkCallBack, g.a(str, i, str2, (Map) map, sdkCallBack));
                cn.com.xy.sms.sdk.queue.b.a();
                cn.com.xy.sms.sdk.service.e.b.b();
            } catch (Throwable th) {
            }
            return r0;
        } catch (Throwable th2) {
        }
        return null;
    }

    public static void queryMoviePosters(String str, SdkCallBack sdkCallBack) {
        if (StringUtils.isNull(str)) {
            XyUtil.doXycallBackResult(sdkCallBack, new Object[0]);
            return;
        }
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("movieName", str);
            jSONObject.put("sdkVersion", NetUtil.APPVERSION);
            jSONObject.put("channel", KeyManager.getAppKey());
            NetWebUtil.sendPostRequest(NetWebUtil.WEB_SERVER_URL_MOVIE_POSTERS, jSONObject.toString(), sdkCallBack);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(sdkCallBack, new Object[0]);
        }
    }

    public static String queryPublicInfo(Context context, String str, int i, String str2, Map<String, String> map) {
        String a = w.a();
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.util.ParseManager", "queryPublicInfo", context, str, Integer.valueOf(i), str2, map);
            if (!n.a((byte) 5)) {
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "queryPublicInfo", context, str, Integer.valueOf(i), str2, map, null);
                return null;
            } else if (StringUtils.isPhoneNumber(str)) {
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "queryPublicInfo", context, str, Integer.valueOf(i), str2, map, null);
                return null;
            } else {
                i.a(new cn.com.xy.sms.sdk.queue.k(12, ParseItemManager.STATE, "2"));
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "queryPublicInfo", context, str, Integer.valueOf(i), str2, map, g.a(str, i, str2, (Map) map, null));
                return g.a(str, i, str2, (Map) map, null);
            }
        } catch (Throwable th) {
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "queryPublicInfo", context, str, Integer.valueOf(i), str2, map, null);
        }
    }

    public static String queryPublicInfo(Context context, String str, int i, String str2, Map<String, String> map, SdkCallBack sdkCallBack) {
        if (!n.a((byte) 5) || StringUtils.isPhoneNumber(str)) {
            return null;
        }
        String a = w.a();
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.util.ParseManager", "queryPublicInfo", context, str, Integer.valueOf(i), str2, map);
            try {
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "queryPublicInfo", context, str, Integer.valueOf(i), str2, map, g.a(str, i, str2, (Map) map, (XyCallBack) sdkCallBack));
                cn.com.xy.sms.sdk.queue.b.a();
                cn.com.xy.sms.sdk.service.e.b.b();
            } catch (Throwable th) {
            }
            return r0;
        } catch (Throwable th2) {
        }
        return null;
    }

    public static String queryPublicInfoWithId(Context context, String str, int i, String str2, Map<String, String> map, SdkCallBack sdkCallBack) {
        if (!n.a((byte) 5) || StringUtils.isPhoneNumber(str)) {
            return null;
        }
        String a = w.a();
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.util.ParseManager", "queryPublicInfoWithId", context, str, Integer.valueOf(i), str2, map);
            try {
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.util.ParseManager", "queryPublicInfoWithId", context, str, Integer.valueOf(i), str2, map, g.b(str, i, str2, map, sdkCallBack));
                cn.com.xy.sms.sdk.queue.b.a();
                cn.com.xy.sms.sdk.service.e.b.b();
            } catch (Throwable th) {
            }
            return r0;
        } catch (Throwable th2) {
        }
        return null;
    }

    public static void queryTrainInfo(String str, String str2, String str3, String str4, Map<String, Object> map, SdkCallBack sdkCallBack) {
        a.a(str, str2, str3, str4, map, sdkCallBack);
    }

    public static void reInitAlgorithm(Context context, SdkCallBack sdkCallBack) {
        cn.com.xy.sms.sdk.util.g.a(context, sdkCallBack);
    }

    public static long setDefServiceSwitch(Context context, String str) {
        try {
            SysParamEntityManager.insertOrUpdateKeyValue(context, "defService", str, null);
            return 0;
        } catch (Throwable th) {
            return -2;
        }
    }

    public static void setLogSdkDoAction(cn.com.xy.sms.sdk.util.n nVar) {
        DuoquUtils.logSdkDoAction = nVar;
    }

    public static void setSdkDoAction(AbsSdkDoAction absSdkDoAction) {
        DuoquUtils.sdkAction = absSdkDoAction;
    }

    public static void setSmartEnhance(boolean z) {
        try {
            SysParamEntityManager.insertOrUpdateKeyValue(Constant.getContext(), "smartsms_enhance", String.valueOf(z), null);
            SysParamEntityManager.cacheMap.put("smartsms_enhance", Boolean.valueOf(z));
        } catch (Throwable th) {
        }
    }

    public static void setmUseNewDes(boolean z) {
        b = z;
    }

    public static void unRegisterReceiver(Context context) {
        b.b(context);
    }

    public static void updateData(Map<String, String> map, SdkCallBack sdkCallBack) {
        Object obj = 1;
        try {
            if (System.currentTimeMillis() - updateDataTime > Constant.sqlUpdateTimeCyc) {
                obj = null;
            }
            if (obj == null) {
                isupdateData = false;
            }
            if (isupdateData) {
                XyUtil.doXycallBack(sdkCallBack, "-3");
                return;
            }
            updateDataTime = System.currentTimeMillis();
            isupdateData = true;
            NetUtil.executeRunnable(new i(map, sdkCallBack));
        } catch (Throwable th) {
        }
    }

    public static void updateMatchCacheManager(BusinessSmsMessage businessSmsMessage) {
        if (businessSmsMessage != null) {
            try {
                updateMatchCacheManager((String) businessSmsMessage.getValue("phoneNum"), businessSmsMessage.getTitleNo(), String.valueOf(businessSmsMessage.getSmsId()), new JSONObject(businessSmsMessage.bubbleJsonObj.toString()), businessSmsMessage.getMessageBody());
            } catch (Throwable th) {
            }
        }
    }

    public static void updateMatchCacheManager(String str, String str2, String str3, JSONObject jSONObject, String str4) {
        MatchCacheManager.updateMatchCacheManager(str, str2, str3, jSONObject, str4);
    }

    public static void updateMatchCacheManager(String str, String str2, String str3, JSONObject jSONObject, JSONArray jSONArray, String str4) {
        MatchCacheManager.updateMatchCacheManager(str, str2, str3, jSONObject, jSONArray, str4);
    }

    public static void updateNow() {
        k.a(true, false);
    }
}
