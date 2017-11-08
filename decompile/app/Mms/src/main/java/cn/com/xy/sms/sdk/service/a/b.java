package cn.com.xy.sms.sdk.service.a;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.provider.ContactsProvider;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.queue.k;
import cn.com.xy.sms.sdk.util.ConversationManager;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.f;
import cn.com.xy.sms.util.ParseBubbleManager;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.ParseMsgUrlManager;
import cn.com.xy.sms.util.w;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class b {
    private static long a = 0;
    private static String b = null;
    private static a c = new a();
    private static boolean d = false;
    private static BroadcastReceiver e = null;

    public static String a(Map<String, Object> map, Map<String, String> map2) {
        if (map != null) {
            try {
                if (!map.isEmpty()) {
                    String str = map2 == null ? null : (String) map2.get("isUseNewAction");
                    Object obj = map.get("NEW_ADACTION");
                    return (!"true".equalsIgnoreCase(str) || obj == null || StringUtils.isNull(obj.toString())) ? (String) map.get("ADACTION") : (String) obj;
                }
            } catch (Throwable th) {
                return "";
            }
        }
        return "";
    }

    static Map<String, Object> a(Context context, String str, String str2, String str3, long j, Map<String, String> map) {
        Map<String, Object> hashMap;
        Throwable th;
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            Map<String, Object> map2 = null;
            String a = w.a();
            Map hashMap2;
            String trim;
            try {
                ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.service.baseparse.BaseParseService", "parseMsg", context, str, str2, str3, Long.valueOf(j), map);
                String valueOf = String.valueOf(j);
                hashMap2 = map != null ? map : new HashMap();
                try {
                    if (!hashMap2.containsKey(Constant.KEY_ALLOW_VERCODE_MSG)) {
                        hashMap2.put(Constant.KEY_ALLOW_VERCODE_MSG, "true");
                    }
                    if (hashMap2.containsKey(Constant.KEY_ALLOW_PERSONAL_MSG) && !DuoquUtils.getSdkDoAction().isContact(context, str)) {
                        hashMap2.put(Constant.KEY_ALLOW_PERSONAL_MSG, "false");
                    }
                    hashMap2.put(NumberInfo.VERSION_KEY, DexUtil.getSceneVersion());
                    hashMap2.put("channel", SysParamEntityManager.getStringParam(context, Constant.CHANNEL));
                    hashMap2.put("smsCenterNum", str2);
                    if (b == null) {
                        b = IccidLocationUtil.getProvince();
                    }
                    if (StringUtils.isNull(b)) {
                        hashMap2.put("provice", b);
                    }
                    int intParam = SysParamEntityManager.getIntParam(Constant.getContext(), Constant.RECOGNIZE_LEVEL);
                    if (intParam != -1) {
                        hashMap2.put(Constant.RECOGNIZE_LEVEL, new StringBuilder(String.valueOf(intParam)).toString());
                    }
                    JSONArray timeSubInfo = DuoquUtils.getSdkDoAction().getTimeSubInfo(str3, j);
                    hashMap2.put(Constant.KEY_HW_PARSE_TIME, timeSubInfo != null ? timeSubInfo.toString() : "");
                    String str4 = new String(str3);
                    trim = str3.trim();
                    try {
                        String str5;
                        Map hashMap3;
                        k kVar;
                        if (!d) {
                            if (f.c(Constant.getPARSE_PATH(), "parseUtilMain", "jar")) {
                                if (!n.a(Constant.getJarPath()).booleanValue()) {
                                }
                            }
                            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "parseUtilMain not valid.", null);
                            cn.com.xy.sms.sdk.util.k.a();
                            if (ParseItemManager.isInitData()) {
                                hashMap = new HashMap();
                                try {
                                    hashMap.put("parseStatu", Integer.valueOf(-1));
                                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "parse msg parseStatu = -1", null);
                                    ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.service.baseparse.BaseParseService", "parseMsg", context, str, str2, trim, Long.valueOf(j), hashMap2, hashMap);
                                    return hashMap;
                                } catch (Throwable th2) {
                                    th = th2;
                                    map2 = hashMap;
                                    ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.service.baseparse.BaseParseService", "parseMsg", context, str, str2, trim, Long.valueOf(j), hashMap2, r13);
                                    throw th;
                                }
                            }
                            if ((System.currentTimeMillis() >= a + 600000 ? 1 : null) == null) {
                                i.a(new k(2, new String[0]));
                                a = System.currentTimeMillis();
                            }
                            if (map2 != null) {
                                if (map2.containsKey("from_cache")) {
                                    if (map2 != null) {
                                        str5 = (String) hashMap2.get("simIndex");
                                        if (!StringUtils.isNull(str5)) {
                                            map2.put("simIndex", str5);
                                        }
                                        if (StringUtils.isNull((String) hashMap2.get("msgTime"))) {
                                            map2.put("msgTime", hashMap2.get("msgTime"));
                                        } else {
                                            if ((j >= 0 ? 1 : null) == null) {
                                                map2.put("msgTime", String.valueOf(j));
                                            }
                                        }
                                        if (!(StringUtils.isNull((String) map2.get("title_num")) || map2.containsKey("from_cache"))) {
                                            if (!StringUtils.isNull((String) hashMap2.get("simIccid"))) {
                                                i.a(new k(1, "simIccid", (String) hashMap2.get("simIccid"), "receiveNum", str, "sms", trim, "centerNum", str2, "sceneId", str5));
                                            }
                                            i.a(new k(6, "titleNo", str5));
                                            i.a(new k(3, "titleNo", str5));
                                            i.a(new k(8, "titleNo", str5));
                                            i.a(new k(5, "titleNo", str5, NumberInfo.TYPE_KEY, "0"));
                                            hashMap3 = new HashMap();
                                            hashMap3.putAll(map2);
                                            ContactsProvider.addContactsToDb(context, hashMap3);
                                        }
                                        if ("true".equals((String) hashMap2.get("pickUrl"))) {
                                            str5 = ParseMsgUrlManager.pickUrlFromMsg("", str, trim, valueOf, hashMap2);
                                            if (!StringUtils.isNull(str5)) {
                                                map2.put(Constant.URLS, str5);
                                            }
                                        }
                                    }
                                    if (Boolean.valueOf((String) hashMap2.get("parse_recognise_value")).booleanValue()) {
                                        if (!StringUtils.isNull((String) hashMap2.get("msgId"))) {
                                            kVar = new k(15, "msgId", (String) hashMap2.get("msgId"), IccidInfoManager.NUM, str, IccidInfoManager.CNUM, str2, "msg", str4, "smsTime", valueOf);
                                            if (map2 != null) {
                                                try {
                                                    if (!StringUtils.isNull((String) hashMap2.get("ref_basevalue")) && Boolean.valueOf((String) hashMap2.get("ref_basevalue")).booleanValue()) {
                                                        map2.remove("ref_basevalue");
                                                        hashMap3 = new HashMap();
                                                        hashMap3.putAll(map2);
                                                        kVar.a(hashMap3);
                                                    }
                                                } catch (Exception e) {
                                                }
                                            }
                                            i.a(kVar);
                                        }
                                    }
                                    DexUtil.handleParseMsg(context, (String) hashMap2.get("msgId"), str, str2, trim, j, hashMap2, map2);
                                    ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.service.baseparse.BaseParseService", "parseMsg", context, str, str2, trim, Long.valueOf(j), hashMap2, map2);
                                    return map2;
                                }
                            }
                            i.a(new k(9, IccidInfoManager.NUM, str, "msg", trim, IccidInfoManager.CNUM, str2, "smsTime", valueOf));
                            if ("1".equals(hashMap2.get("from"))) {
                                i.a(new k(10, IccidInfoManager.NUM, str, "msg", trim, IccidInfoManager.CNUM, str2, "smsTime", valueOf));
                            }
                            if (map2 != null) {
                                str5 = (String) hashMap2.get("simIndex");
                                if (StringUtils.isNull(str5)) {
                                    map2.put("simIndex", str5);
                                }
                                if (StringUtils.isNull((String) hashMap2.get("msgTime"))) {
                                    map2.put("msgTime", hashMap2.get("msgTime"));
                                } else {
                                    if (j >= 0) {
                                    }
                                    if ((j >= 0 ? 1 : null) == null) {
                                        map2.put("msgTime", String.valueOf(j));
                                    }
                                }
                                if (StringUtils.isNull((String) hashMap2.get("simIccid"))) {
                                    i.a(new k(1, "simIccid", (String) hashMap2.get("simIccid"), "receiveNum", str, "sms", trim, "centerNum", str2, "sceneId", str5));
                                }
                                i.a(new k(6, "titleNo", str5));
                                i.a(new k(3, "titleNo", str5));
                                i.a(new k(8, "titleNo", str5));
                                i.a(new k(5, "titleNo", str5, NumberInfo.TYPE_KEY, "0"));
                                hashMap3 = new HashMap();
                                hashMap3.putAll(map2);
                                ContactsProvider.addContactsToDb(context, hashMap3);
                                if ("true".equals((String) hashMap2.get("pickUrl"))) {
                                    str5 = ParseMsgUrlManager.pickUrlFromMsg("", str, trim, valueOf, hashMap2);
                                    if (StringUtils.isNull(str5)) {
                                        map2.put(Constant.URLS, str5);
                                    }
                                }
                            }
                            if (Boolean.valueOf((String) hashMap2.get("parse_recognise_value")).booleanValue()) {
                                if (StringUtils.isNull((String) hashMap2.get("msgId"))) {
                                    kVar = new k(15, "msgId", (String) hashMap2.get("msgId"), IccidInfoManager.NUM, str, IccidInfoManager.CNUM, str2, "msg", str4, "smsTime", valueOf);
                                    if (map2 != null) {
                                        map2.remove("ref_basevalue");
                                        hashMap3 = new HashMap();
                                        hashMap3.putAll(map2);
                                        kVar.a(hashMap3);
                                    }
                                    i.a(kVar);
                                }
                            }
                            DexUtil.handleParseMsg(context, (String) hashMap2.get("msgId"), str, str2, trim, j, hashMap2, map2);
                            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.service.baseparse.BaseParseService", "parseMsg", context, str, str2, trim, Long.valueOf(j), hashMap2, map2);
                            return map2;
                        }
                        d = true;
                        map2 = !"true".equals(hashMap2.get("parseVerifyCode")) ? DexUtil.parseMsgToMap(str, trim, hashMap2) : DexUtil.parseVerifyCodeToMap(str, trim, hashMap2);
                        if (map2 == null) {
                            i.a(new k(16, IccidInfoManager.NUM, str, "msg", trim, "smsTime", valueOf));
                            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "baseParseMsg is null.", null);
                        }
                        if ("1".equals(hashMap2.get("from"))) {
                            String[] strArr = new String[4];
                            strArr[0] = "phoneNumber";
                            strArr[1] = str;
                            strArr[2] = "isSuccess";
                            strArr[3] = String.valueOf(map2 != null);
                            i.a(new k(14, strArr));
                        }
                        if (ParseItemManager.isInitData()) {
                            hashMap = new HashMap();
                            hashMap.put("parseStatu", Integer.valueOf(-1));
                            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "parse msg parseStatu = -1", null);
                            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.service.baseparse.BaseParseService", "parseMsg", context, str, str2, trim, Long.valueOf(j), hashMap2, hashMap);
                            return hashMap;
                        }
                        if (System.currentTimeMillis() >= a + 600000) {
                        }
                        if ((System.currentTimeMillis() >= a + 600000 ? 1 : null) == null) {
                            i.a(new k(2, new String[0]));
                            a = System.currentTimeMillis();
                        }
                        if (map2 != null) {
                            if (map2.containsKey("from_cache")) {
                                if (map2 != null) {
                                    str5 = (String) hashMap2.get("simIndex");
                                    if (StringUtils.isNull(str5)) {
                                        map2.put("simIndex", str5);
                                    }
                                    if (StringUtils.isNull((String) hashMap2.get("msgTime"))) {
                                        if (j >= 0) {
                                        }
                                        if ((j >= 0 ? 1 : null) == null) {
                                            map2.put("msgTime", String.valueOf(j));
                                        }
                                    } else {
                                        map2.put("msgTime", hashMap2.get("msgTime"));
                                    }
                                    if (StringUtils.isNull((String) hashMap2.get("simIccid"))) {
                                        i.a(new k(1, "simIccid", (String) hashMap2.get("simIccid"), "receiveNum", str, "sms", trim, "centerNum", str2, "sceneId", str5));
                                    }
                                    i.a(new k(6, "titleNo", str5));
                                    i.a(new k(3, "titleNo", str5));
                                    i.a(new k(8, "titleNo", str5));
                                    i.a(new k(5, "titleNo", str5, NumberInfo.TYPE_KEY, "0"));
                                    hashMap3 = new HashMap();
                                    hashMap3.putAll(map2);
                                    ContactsProvider.addContactsToDb(context, hashMap3);
                                    if ("true".equals((String) hashMap2.get("pickUrl"))) {
                                        str5 = ParseMsgUrlManager.pickUrlFromMsg("", str, trim, valueOf, hashMap2);
                                        if (StringUtils.isNull(str5)) {
                                            map2.put(Constant.URLS, str5);
                                        }
                                    }
                                }
                                if (Boolean.valueOf((String) hashMap2.get("parse_recognise_value")).booleanValue()) {
                                    if (StringUtils.isNull((String) hashMap2.get("msgId"))) {
                                        kVar = new k(15, "msgId", (String) hashMap2.get("msgId"), IccidInfoManager.NUM, str, IccidInfoManager.CNUM, str2, "msg", str4, "smsTime", valueOf);
                                        if (map2 != null) {
                                            map2.remove("ref_basevalue");
                                            hashMap3 = new HashMap();
                                            hashMap3.putAll(map2);
                                            kVar.a(hashMap3);
                                        }
                                        i.a(kVar);
                                    }
                                }
                                DexUtil.handleParseMsg(context, (String) hashMap2.get("msgId"), str, str2, trim, j, hashMap2, map2);
                                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.service.baseparse.BaseParseService", "parseMsg", context, str, str2, trim, Long.valueOf(j), hashMap2, map2);
                                return map2;
                            }
                        }
                        i.a(new k(9, IccidInfoManager.NUM, str, "msg", trim, IccidInfoManager.CNUM, str2, "smsTime", valueOf));
                        if ("1".equals(hashMap2.get("from"))) {
                            i.a(new k(10, IccidInfoManager.NUM, str, "msg", trim, IccidInfoManager.CNUM, str2, "smsTime", valueOf));
                        }
                        if (map2 != null) {
                            str5 = (String) hashMap2.get("simIndex");
                            if (StringUtils.isNull(str5)) {
                                map2.put("simIndex", str5);
                            }
                            if (StringUtils.isNull((String) hashMap2.get("msgTime"))) {
                                map2.put("msgTime", hashMap2.get("msgTime"));
                            } else {
                                if (j >= 0) {
                                }
                                if ((j >= 0 ? 1 : null) == null) {
                                    map2.put("msgTime", String.valueOf(j));
                                }
                            }
                            if (StringUtils.isNull((String) hashMap2.get("simIccid"))) {
                                i.a(new k(1, "simIccid", (String) hashMap2.get("simIccid"), "receiveNum", str, "sms", trim, "centerNum", str2, "sceneId", str5));
                            }
                            i.a(new k(6, "titleNo", str5));
                            i.a(new k(3, "titleNo", str5));
                            i.a(new k(8, "titleNo", str5));
                            i.a(new k(5, "titleNo", str5, NumberInfo.TYPE_KEY, "0"));
                            hashMap3 = new HashMap();
                            hashMap3.putAll(map2);
                            ContactsProvider.addContactsToDb(context, hashMap3);
                            if ("true".equals((String) hashMap2.get("pickUrl"))) {
                                str5 = ParseMsgUrlManager.pickUrlFromMsg("", str, trim, valueOf, hashMap2);
                                if (StringUtils.isNull(str5)) {
                                    map2.put(Constant.URLS, str5);
                                }
                            }
                        }
                        if (Boolean.valueOf((String) hashMap2.get("parse_recognise_value")).booleanValue()) {
                            if (StringUtils.isNull((String) hashMap2.get("msgId"))) {
                                kVar = new k(15, "msgId", (String) hashMap2.get("msgId"), IccidInfoManager.NUM, str, IccidInfoManager.CNUM, str2, "msg", str4, "smsTime", valueOf);
                                if (map2 != null) {
                                    map2.remove("ref_basevalue");
                                    hashMap3 = new HashMap();
                                    hashMap3.putAll(map2);
                                    kVar.a(hashMap3);
                                }
                                i.a(kVar);
                            }
                        }
                        DexUtil.handleParseMsg(context, (String) hashMap2.get("msgId"), str, str2, trim, j, hashMap2, map2);
                        ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.service.baseparse.BaseParseService", "parseMsg", context, str, str2, trim, Long.valueOf(j), hashMap2, map2);
                        return map2;
                    } catch (Throwable th3) {
                        th = th3;
                        DexUtil.saveExceptionLog(th);
                        ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.service.baseparse.BaseParseService", "parseMsg", context, str, str2, trim, Long.valueOf(j), hashMap2, map2);
                        return map2;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    trim = str3;
                    ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.service.baseparse.BaseParseService", "parseMsg", context, str, str2, trim, Long.valueOf(j), hashMap2, r13);
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                Map<String, String> map3 = map;
                trim = str3;
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.service.baseparse.BaseParseService", "parseMsg", context, str, str2, trim, Long.valueOf(j), hashMap2, r13);
                throw th;
            }
        } else {
            throw new Exception(" smsContent is null.");
        }
    }

    public static JSONObject a(Context context, String str, String str2, String str3, Map<String, String> map) {
        try {
            Map b = b(context, str, str3, str2, 0, map);
            if (!(b == null || ParseBubbleManager.getParseStatu(b) == -1)) {
                return DexUtil.getSmsType(b);
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static synchronized void a(Context context) {
        synchronized (b.class) {
            try {
                if (e == null) {
                    e = new d();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(ParseManager.UPDATE_ICCID_INFO_CACHE_ACTION);
                    context.registerReceiver(e, intentFilter);
                    return;
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void a(Context context, String str, String str2, String str3, long j, Map<String, String> map, XyCallBack xyCallBack) {
        a.a().execute(new c(context, str, str2, str3, j, map, xyCallBack));
    }

    static /* synthetic */ void a(Intent intent) {
        if (intent != null) {
            IccidLocationUtil.updateIccidCache(intent.getStringExtra(IccidInfoManager.ICCID), intent.getIntExtra("simIndex", -1));
        }
    }

    public static synchronized Map<String, Object> b(Context context, String str, String str2, String str3, long j, Map<String, String> map) {
        Map<String, Object> map2;
        Throwable th;
        synchronized (b.class) {
            a aVar;
            Future submit;
            try {
                long longValue;
                aVar = c;
                aVar.a = context;
                aVar.b = str;
                aVar.c = str2;
                aVar.d = str3;
                aVar.e = j;
                aVar.f = map;
                submit = a.g.submit(c);
                if (map != null) {
                    try {
                        if (map.containsKey("PARSE_TIME_OUT")) {
                            longValue = Long.valueOf((String) map.get("PARSE_TIME_OUT")).longValue();
                            if (longValue == 0) {
                                longValue = 6000;
                            }
                            if (submit == null) {
                                try {
                                } catch (Throwable th2) {
                                    th = th2;
                                    try {
                                        if (!(th instanceof TimeoutException)) {
                                            if (!(th instanceof InterruptedException)) {
                                                map2 = null;
                                                c.a();
                                                return map2;
                                            }
                                        }
                                        try {
                                            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", " baseparse timeout or interrupted", null);
                                            if (submit != null) {
                                                submit.cancel(true);
                                            }
                                        } catch (Throwable th3) {
                                        }
                                        try {
                                            aVar = c;
                                            try {
                                                if (aVar.g != null) {
                                                    aVar.g.stop();
                                                }
                                            } catch (Throwable th4) {
                                            }
                                            a.g.shutdownNow();
                                        } catch (Throwable th5) {
                                        }
                                        a.g = Executors.newFixedThreadPool(1);
                                        map2 = new HashMap();
                                        map2.put("parseStatu", Integer.valueOf(-1));
                                        c.a();
                                        return map2;
                                    } catch (Throwable th6) {
                                        c.a();
                                    }
                                }
                            }
                            c.a();
                        }
                    } catch (Exception e) {
                    }
                }
                longValue = 0;
                if (longValue == 0) {
                    longValue = 6000;
                }
                map2 = submit == null ? (Map) submit.get(longValue, TimeUnit.MILLISECONDS) : null;
                c.a();
            } catch (Throwable th7) {
                th = th7;
                submit = null;
                if (th instanceof TimeoutException) {
                    if (th instanceof InterruptedException) {
                        map2 = null;
                        c.a();
                        return map2;
                    }
                }
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", " baseparse timeout or interrupted", null);
                if (submit != null) {
                    submit.cancel(true);
                }
                aVar = c;
                if (aVar.g != null) {
                    aVar.g.stop();
                }
                a.g.shutdownNow();
                a.g = Executors.newFixedThreadPool(1);
                map2 = new HashMap();
                map2.put("parseStatu", Integer.valueOf(-1));
                c.a();
                return map2;
            }
        }
        return map2;
    }

    public static synchronized void b(Context context) {
        synchronized (b.class) {
            try {
                if (e != null) {
                    context.unregisterReceiver(e);
                    e = null;
                    return;
                }
            } catch (Throwable th) {
            }
        }
    }

    private static void b(Intent intent) {
        if (intent != null) {
            IccidLocationUtil.updateIccidCache(intent.getStringExtra(IccidInfoManager.ICCID), intent.getIntExtra("simIndex", -1));
        }
    }
}
