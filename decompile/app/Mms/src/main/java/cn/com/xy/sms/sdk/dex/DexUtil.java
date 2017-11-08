package cn.com.xy.sms.sdk.dex;

import android.content.Context;
import android.util.LruCache;
import cn.com.xy.sms.sdk.Iservice.CorpSignInterface;
import cn.com.xy.sms.sdk.Iservice.IActionService;
import cn.com.xy.sms.sdk.Iservice.OnlineParseInterface;
import cn.com.xy.sms.sdk.Iservice.OnlineUpdateCycleConfigInterface;
import cn.com.xy.sms.sdk.Iservice.OnlineViewInterface;
import cn.com.xy.sms.sdk.Iservice.ParseBoxInterface;
import cn.com.xy.sms.sdk.Iservice.ParseBubbleInterface;
import cn.com.xy.sms.sdk.Iservice.ParseCardInterface;
import cn.com.xy.sms.sdk.Iservice.ParseContactInterface;
import cn.com.xy.sms.sdk.Iservice.ParseDateInterface;
import cn.com.xy.sms.sdk.Iservice.ParseNotificationInterface;
import cn.com.xy.sms.sdk.Iservice.ParsePayInterface;
import cn.com.xy.sms.sdk.Iservice.ParseRemindInterface;
import cn.com.xy.sms.sdk.Iservice.ParseVerifyCodeInterface;
import cn.com.xy.sms.sdk.Iservice.ParseVerifyCodeValidTimeInterfaceget;
import cn.com.xy.sms.sdk.Iservice.ParseWatchInterface;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.number.a;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.queue.k;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.ConversationManager;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.PopupUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.sdk.util.c;
import cn.com.xy.sms.sdk.util.f;
import cn.com.xy.sms.sdk.util.g;
import cn.com.xy.sms.util.w;
import com.google.android.gms.actions.SearchIntents;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class DexUtil {
    private static final String a = "DexUtil";
    private static ClassLoader b;
    private static OnlineParseInterface c;
    private static OnlineUpdateCycleConfigInterface d;
    private static ParseVerifyCodeValidTimeInterfaceget e;
    private static Map<String, ClassLoader> f = new ConcurrentHashMap();
    private static Map<String, Class> g = new ConcurrentHashMap();
    private static String h = "";
    private static LruCache<String, Map<String, Object>> i = new LruCache(12);
    private static long j = 0;
    private static Object k = null;

    private static Object a(Class<?> cls) {
        if (k != null) {
            return k;
        }
        if (cls == null) {
            return null;
        }
        Object newInstance = cls.newInstance();
        k = newInstance;
        return newInstance;
    }

    private static String a(String str) {
        return !a.a.toString().equals(str) ? !a.b.toString().equals(str) ? !a.c.toString().equals(str) ? !a.d.toString().equals(str) ? "号码联盟" : "触宝" : "搜狗" : "电话邦" : "360";
    }

    private static void a(Object obj) {
        k = null;
    }

    private static void a(JSONObject jSONObject, Map<String, Object> map) {
        try {
            jSONObject.put("NEW_ADACTION", map.get("NEW_ADACTION"));
        } catch (Throwable th) {
        }
    }

    private static OnlineUpdateCycleConfigInterface b() {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.OnlineUpdateCycleConfig");
            if (classBymap != null) {
                d = (OnlineUpdateCycleConfigInterface) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return d;
    }

    public static void beforeInitBigJar() {
        try {
            int i;
            f.a();
            String[] strArr = new String[]{"ScenesScanner", "ParseSimpleBubbleUtil", "ParseNotification"};
            String str = "cn.com.xy.sms.sdk.Iservice.";
            for (i = 0; i < 3; i++) {
                getClassBymap(null, new StringBuilder(String.valueOf(str)).append(strArr[i]).toString());
            }
            i = 1;
            while (i <= 21) {
                getClassBymap(null, new StringBuilder(String.valueOf(str)).append("PU").append(i >= 10 ? Integer.valueOf(i) : "0" + i).toString());
                i++;
            }
            DuoquUtils.getSdkDoAction().onEventCallback(11, null);
        } catch (Throwable th) {
        } finally {
            g.a = true;
        }
    }

    private static ParseVerifyCodeValidTimeInterfaceget c() {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseVerifyCodeValidTime");
            if (classBymap != null) {
                e = (ParseVerifyCodeValidTimeInterfaceget) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return e;
    }

    public static String catchUrls(String str, String str2) {
        String str3;
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilUrl");
            if (classBymap != null) {
                str3 = (String) classBymap.getMethod("catchUrls", new Class[]{String.class, String.class}).invoke(classBymap, new Object[]{str, str2});
                return str3 == null ? str3 : "";
            }
        } catch (Throwable th) {
            th.getMessage();
        }
        str3 = null;
        if (str3 == null) {
        }
    }

    public static void cleanCache() {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilEmail");
            if (classBymap != null) {
                classBymap.getMethod("cleanCache", new Class[0]).invoke(classBymap, new Object[0]);
            }
        } catch (Throwable th) {
        }
    }

    public static Date convertDate(String str) {
        try {
            ClassLoader dexClassLoader = getDexClassLoader();
            if (dexClassLoader != null) {
                Class loadClass = dexClassLoader.loadClass("cn.com.xy.sms.sdk.Iservice.DateTimeNormalizer");
                if (loadClass != null) {
                    return (Date) loadClass.getMethod("convertDate", new Class[]{String.class}).invoke(loadClass, new Object[]{str});
                }
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static Date convertDate(String str, Date date) {
        try {
            ClassLoader dexClassLoader = getDexClassLoader();
            if (dexClassLoader != null) {
                Class loadClass = dexClassLoader.loadClass("cn.com.xy.sms.sdk.Iservice.DateTimeNormalizer");
                if (loadClass != null) {
                    return (Date) loadClass.getMethod("convertDate", new Class[]{String.class, Date.class, Boolean.class, Boolean.class}).invoke(loadClass, new Object[]{str, date, Boolean.valueOf(true), Boolean.valueOf(false)});
                }
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static long convertDateLong(String str, Date date, boolean z, boolean z2, boolean z3) {
        try {
            ClassLoader dexClassLoader = getDexClassLoader();
            if (dexClassLoader != null) {
                Class loadClass = dexClassLoader.loadClass("cn.com.xy.sms.sdk.Iservice.DateTimeNormalizer");
                if (loadClass != null) {
                    return ((Long) loadClass.getMethod("convertDateLong", new Class[]{String.class, Date.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE}).invoke(loadClass, new Object[]{str, date, Boolean.valueOf(z), Boolean.valueOf(z2), Boolean.valueOf(z3)})).longValue();
                }
            }
        } catch (Throwable th) {
        }
        return 0;
    }

    private static Map<String, String> d() {
        Map<String, String> hashMap = new HashMap();
        hashMap.put("1", "骚扰电话");
        hashMap.put("2", "快递外卖");
        hashMap.put(NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR, "疑似诈骗");
        hashMap.put("4", "广告推销");
        hashMap.put("5", "房产中介");
        hashMap.put("6", "车辆服务");
        hashMap.put("7", "保险理财");
        hashMap.put("8", "招聘猎头");
        hashMap.put("9", "响一声");
        hashMap.put("10", "装修维修");
        hashMap.put("11", "违法犯罪");
        hashMap.put("12", "教育培训");
        hashMap.put("13", "企业电话");
        hashMap.put("14", "客服电话");
        return hashMap;
    }

    public static void deleteLog(String str, String str2, JSONObject jSONObject) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.LogService");
            if (classBymap != null) {
                classBymap.getMethod("delete", new Class[]{String.class, String.class, JSONObject.class}).invoke(classBymap, new Object[]{str, str2, jSONObject});
            }
        } catch (Throwable th) {
        }
    }

    public static boolean geOnOffByType(int i) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.OnlineUpdateCycleConfig");
            if (classBymap != null) {
                KeyManager.initAppKey();
                Object a = a(classBymap);
                return ((Boolean) classBymap.getMethod("geOnOffByType", new Class[]{Integer.class, String.class}).invoke(a, new Object[]{Integer.valueOf(i), KeyManager.getAppKey()})).booleanValue();
            }
        } catch (Throwable th) {
        }
        return false;
    }

    public static int getActionCode(String str) {
        try {
            OnlineParseInterface onlineParseImpl = getOnlineParseImpl(false);
            return onlineParseImpl == null ? PopupUtil.getActionCode(str) : onlineParseImpl.getActionCode(str);
        } catch (Throwable th) {
            th.getMessage();
            return PopupUtil.getActionCode(str);
        }
    }

    public static IActionService getActionInterfaceImpl(boolean z) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ActionServiceImpl");
            if (classBymap != null) {
                return (IActionService) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static String getBubbleViewVersion(Map<String, Object> map) {
        String bubbleViewVersion;
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilBubble");
            if (classBymap != null) {
                ParseBubbleInterface parseBubbleInterface = (ParseBubbleInterface) classBymap.newInstance();
                if (parseBubbleInterface != null) {
                    bubbleViewVersion = parseBubbleInterface.getBubbleViewVersion(map);
                    return bubbleViewVersion == null ? bubbleViewVersion : "";
                }
            }
        } catch (Throwable th) {
        }
        bubbleViewVersion = null;
        if (bubbleViewVersion == null) {
        }
    }

    public static Class getClassBymap(Map<String, String> map, String str) {
        return getClassBymap(map, str, false);
    }

    public static Class getClassBymap(Map<String, String> map, String str, boolean z) {
        Throwable th;
        String substring;
        try {
            substring = str.substring(str.lastIndexOf(".") + 1);
            try {
                ClassLoader classLoaderBymap = getClassLoaderBymap(map, substring, z);
                if (classLoaderBymap != null) {
                    Class loadClass;
                    if (z) {
                        loadClass = classLoaderBymap.loadClass(str);
                        if (loadClass != null) {
                            g.put(new StringBuilder(String.valueOf(substring)).append("_Class").toString(), loadClass);
                            return loadClass;
                        }
                    }
                    loadClass = (Class) g.get(new StringBuilder(String.valueOf(substring)).append("_Class").toString());
                    if (loadClass != null) {
                        return loadClass;
                    }
                    loadClass = classLoaderBymap.loadClass(str);
                    if (loadClass != null) {
                        g.put(new StringBuilder(String.valueOf(substring)).append("_Class").toString(), loadClass);
                        return loadClass;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                removeClassLoaderBySubname(substring);
                th.getMessage();
                saveExceptionLog(th);
                return null;
            }
        } catch (Throwable th3) {
            th = th3;
            substring = null;
            removeClassLoaderBySubname(substring);
            th.getMessage();
            saveExceptionLog(th);
            return null;
        }
        return null;
    }

    public static ClassLoader getClassLoaderBymap(Map<String, String> map, String str, boolean z) {
        ClassLoader classLoader = z ? null : (ClassLoader) f.get(new StringBuilder(String.valueOf(str)).append("_ClassLoader").toString());
        if (classLoader == null) {
            try {
                String d = f.d(Constant.getPARSE_PATH(), new StringBuilder(String.valueOf(str)).append("_").toString(), ".jar");
                File file = new File(d);
                if (file.exists()) {
                    if (n.a(d).booleanValue()) {
                        File dir = Constant.getContext().getDir("outdex", 0);
                        ClassLoader dexClassLoader = getDexClassLoader();
                        if (dexClassLoader != null) {
                            classLoader = new DexClassLoader(file.getCanonicalPath(), dir.getCanonicalPath(), null, dexClassLoader);
                        }
                        if (classLoader != null) {
                            f.put(new StringBuilder(String.valueOf(str)).append("_ClassLoader").toString(), classLoader);
                            try {
                                cn.com.xy.sms.sdk.a.a.e.execute(new a(str));
                            } catch (Throwable th) {
                            }
                            return classLoader;
                        }
                    }
                }
                try {
                    cn.com.xy.sms.sdk.a.a.e.execute(new a(str));
                } catch (Throwable th2) {
                }
            } catch (Throwable th3) {
            }
            return null;
        }
        try {
            cn.com.xy.sms.sdk.a.a.e.execute(new a(str));
        } catch (Throwable th4) {
        }
        return classLoader;
    }

    public static String getCmd(String str, String str2) {
        String str3;
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilUnsubscribe");
            if (classBymap != null) {
                str3 = (String) classBymap.getMethod("parseUnsubscribe", new Class[]{String.class, String.class}).invoke(classBymap, new Object[]{str, str2});
                return str3 == null ? str3 : "";
            }
        } catch (Throwable th) {
        }
        str3 = null;
        if (str3 == null) {
        }
    }

    public static JSONArray getConfigByType(int i, String str, Integer num) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.OnlineUpdateCycleConfig");
            if (classBymap != null) {
                KeyManager.initAppKey();
                Object a = a(classBymap);
                return (JSONArray) classBymap.getMethod("getConfigByType", new Class[]{Integer.class, String.class, String.class, String.class, Integer.class}).invoke(a, new Object[]{Integer.valueOf(i), KeyManager.getAppKey(), NetUtil.APPVERSION, str, num});
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static String getCorp(String str) {
        try {
            CorpSignInterface corpSignImpl = getCorpSignImpl(false);
            return corpSignImpl == null ? c.a(str) : corpSignImpl.getCorp(str);
        } catch (Throwable th) {
            return "";
        }
    }

    public static String[] getCorpAndEc(String str) {
        String[] strArr = null;
        try {
            CorpSignInterface corpSignImpl = getCorpSignImpl(false);
            if (corpSignImpl != null) {
                strArr = corpSignImpl.getCorpAndEc(str);
            }
        } catch (Throwable th) {
        }
        if (strArr != null && strArr.length >= 2) {
            return strArr;
        }
        strArr = new String[2];
        strArr[0] = getCorp(str);
        return strArr;
    }

    public static CorpSignInterface getCorpSignImpl(boolean z) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.CorpSignImpl");
            if (classBymap != null) {
                return (CorpSignInterface) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static synchronized ClassLoader getDexClassLoader() {
        ClassLoader classLoader;
        synchronized (DexUtil.class) {
            try {
                if (b == null) {
                    File file = new File(Constant.getJarPath());
                    if (file.exists() && n.a(Constant.getJarPath()).booleanValue()) {
                        File dir = Constant.getContext().getDir("outdex", 0);
                        b = new DexClassLoader(file.getCanonicalPath(), dir.getCanonicalPath(), null, Constant.getContext().getClassLoader());
                        XyUtil.chmod("640", dir.getCanonicalPath() + File.separator + file.getName().substring(0, file.getName().length() - 4) + ".dex");
                    }
                }
            } catch (Throwable th) {
                th.getMessage();
            }
            classLoader = b;
        }
        return classLoader;
    }

    public static synchronized ClassLoader getDexClassLoader(boolean z) {
        ClassLoader classLoader;
        synchronized (DexUtil.class) {
            try {
                if (b == null || z) {
                    File file = new File(Constant.getJarPath());
                    if (file.exists()) {
                        if (n.a(Constant.getJarPath()).booleanValue()) {
                            b = new DexClassLoader(file.getCanonicalPath(), Constant.getContext().getDir("outdex", 0).getCanonicalPath(), null, Constant.getContext().getClassLoader());
                        }
                    }
                    classLoader = b;
                } else {
                    classLoader = b;
                }
            } catch (Throwable th) {
                th.getMessage();
            }
        }
        return classLoader;
    }

    public static String getNumberSourceName(String str) {
        String str2;
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.OnlineUpdateCycleConfig");
            if (classBymap != null) {
                str2 = (String) classBymap.getMethod("getNumberSourceName", new Class[]{String.class}).invoke(classBymap, new Object[]{str});
                return StringUtils.isNull(str2) ? str2 : a.a.toString().equals(str) ? a.b.toString().equals(str) ? a.c.toString().equals(str) ? a.d.toString().equals(str) ? "号码联盟" : "触宝" : "搜狗" : "电话邦" : "360";
            }
        } catch (Throwable th) {
        }
        str2 = null;
        if (StringUtils.isNull(str2)) {
            if (a.a.toString().equals(str)) {
            }
        }
    }

    public static Map<String, String> getNumberTagTypeMap(Map<String, Object> map) {
        Map<String, String> map2;
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.OnlineUpdateCycleConfig");
            if (classBymap != null) {
                map2 = (Map) classBymap.getMethod("getNumberTagTypeMap", new Class[]{Map.class}).invoke(classBymap, new Object[]{map});
                if (map2 != null) {
                    return map2;
                }
                map2 = new HashMap();
                map2.put("1", "骚扰电话");
                map2.put("2", "快递外卖");
                map2.put(NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR, "疑似诈骗");
                map2.put("4", "广告推销");
                map2.put("5", "房产中介");
                map2.put("6", "车辆服务");
                map2.put("7", "保险理财");
                map2.put("8", "招聘猎头");
                map2.put("9", "响一声");
                map2.put("10", "装修维修");
                map2.put("11", "违法犯罪");
                map2.put("12", "教育培训");
                map2.put("13", "企业电话");
                map2.put("14", "客服电话");
                return map2;
            }
        } catch (Throwable th) {
        }
        map2 = null;
        if (map2 != null) {
            return map2;
        }
        map2 = new HashMap();
        map2.put("1", "骚扰电话");
        map2.put("2", "快递外卖");
        map2.put(NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR, "疑似诈骗");
        map2.put("4", "广告推销");
        map2.put("5", "房产中介");
        map2.put("6", "车辆服务");
        map2.put("7", "保险理财");
        map2.put("8", "招聘猎头");
        map2.put("9", "响一声");
        map2.put("10", "装修维修");
        map2.put("11", "违法犯罪");
        map2.put("12", "教育培训");
        map2.put("13", "企业电话");
        map2.put("14", "客服电话");
        return map2;
    }

    public static String getOnLineConfigureData(int i) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.OnlineUpdateCycleConfig");
            if (classBymap != null) {
                Object a = a(classBymap);
                return (String) classBymap.getMethod("getConfigureString", new Class[]{Integer.class, Map.class}).invoke(a, new Object[]{Integer.valueOf(i), null});
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static synchronized OnlineParseInterface getOnlineParseImpl(boolean z) {
        OnlineParseInterface onlineParseInterface;
        synchronized (DexUtil.class) {
            try {
                if (c == null || z) {
                    ClassLoader dexClassLoader = getDexClassLoader();
                    if (dexClassLoader != null) {
                        Class loadClass = dexClassLoader.loadClass("cn.com.xy.sms.sdk.Iservice.OnlineParseImpl");
                        if (loadClass != null) {
                            c = (OnlineParseInterface) loadClass.newInstance();
                        }
                    }
                    onlineParseInterface = c;
                } else {
                    onlineParseInterface = c;
                }
            } catch (Throwable th) {
            }
        }
        return onlineParseInterface;
    }

    public static OnlineViewInterface getOnlineViewImpl(boolean z) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.OnlineViewImpl");
            if (classBymap != null) {
                return (OnlineViewInterface) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static ParseBoxInterface getParseBoxImpl(boolean z) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseBox");
            if (classBymap != null) {
                return (ParseBoxInterface) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static ParseCardInterface getParseCardImpl(boolean z) {
        ParseCardInterface parseCardInterface;
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilCard");
            if (classBymap == null) {
                return null;
            }
            parseCardInterface = (ParseCardInterface) classBymap.newInstance();
            if (parseCardInterface == null) {
                return parseCardInterface;
            }
            i.a(new k(12, ParseItemManager.STATE, "32"));
            return parseCardInterface;
        } catch (Throwable th) {
            return null;
        }
    }

    public static ParseContactInterface getParseContactImpl(boolean z) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseContact");
            if (classBymap != null) {
                return (ParseContactInterface) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static ParseDateInterface getParseDateImpl(boolean z) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseDate");
            if (classBymap != null) {
                return (ParseDateInterface) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static ParseNotificationInterface getParseNotificationImpl(boolean z) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseNotification");
            if (classBymap != null) {
                return (ParseNotificationInterface) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static ParsePayInterface getParsePayImpl(boolean z) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilPay");
            if (classBymap != null) {
                return (ParsePayInterface) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static ParseRemindInterface getParseRemindImpl(boolean z) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseRemind");
            if (classBymap != null) {
                return (ParseRemindInterface) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static ParseWatchInterface getParseWatchImpl(boolean z) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseWatch");
            if (classBymap != null) {
                return (ParseWatchInterface) classBymap.newInstance();
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static String getRecogniseActionConfig(JSONObject jSONObject, Map<String, String> map) {
        String a = w.a();
        String str = "";
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.dex.DexUtil", "getRecogniseActionConfig", jSONObject, map);
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.WizardServiceMenuData");
            if (classBymap == null) {
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "getRecogniseActionConfig", jSONObject, map, str);
                return null;
            }
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "getRecogniseActionConfig", jSONObject, map, (String) classBymap.getMethod("getRecogniseMenuAction", new Class[]{JSONObject.class, Map.class}).invoke(classBymap, new Object[]{jSONObject, map}));
            return (String) classBymap.getMethod("getRecogniseMenuAction", new Class[]{JSONObject.class, Map.class}).invoke(classBymap, new Object[]{jSONObject, map});
        } catch (Throwable th) {
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "getRecogniseActionConfig", jSONObject, map, str);
        }
    }

    public static String getSceneVersion() {
        Object obj = null;
        try {
            obj = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.CHANNEL);
            OnlineParseInterface onlineParseImpl = getOnlineParseImpl(false);
            if (onlineParseImpl != null) {
                String sceneVersion = onlineParseImpl.getSceneVersion(obj);
                if (!StringUtils.isNull(sceneVersion)) {
                    return sceneVersion;
                }
            }
        } catch (Throwable th) {
        }
        return !"VMhlWdEwVNEW_LENOVO".equals(obj) ? "20140815" : "20150619";
    }

    public static JSONObject getSmsType(Map<String, Object> map) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilBubble");
            if (classBymap != null) {
                return (JSONObject) classBymap.getMethod("getSmsType", new Class[]{Map.class}).invoke(classBymap, new Object[]{map});
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static int getSmsTypeByMap(Map<String, Object> map, int i) {
        int i2 = -1;
        try {
            OnlineParseInterface onlineParseImpl = getOnlineParseImpl(false);
            if (onlineParseImpl != null) {
                i2 = onlineParseImpl.getSmsTypeByMap(map, i);
            }
            return i2;
        } catch (Throwable th) {
            return -1;
        }
    }

    public static String getSuanfaVersion() {
        Object obj = null;
        try {
            obj = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.CHANNEL);
            OnlineParseInterface onlineParseImpl = getOnlineParseImpl(false);
            if (onlineParseImpl != null) {
                String reqVersion = onlineParseImpl.getReqVersion(obj);
                if (!StringUtils.isNull(reqVersion)) {
                    return reqVersion;
                }
            }
        } catch (Throwable th) {
        }
        return !"VMhlWdEwVNEW_LENOVO".equals(obj) ? Constant.suanfa_version : "20150619";
    }

    public static String getUIVersion() {
        try {
            if (StringUtils.isNull(h)) {
                Class cls = Class.forName(DuoquUtils.getSdkDoAction().getConfig(3, null));
                Method method = cls.getMethod("getUIVersion", new Class[0]);
                if (method != null) {
                    h = (String) method.invoke(cls, new Object[0]);
                }
            }
        } catch (Throwable th) {
            h = ThemeUtil.SET_NULL_STR;
        }
        return h;
    }

    public static synchronized long getUpdateCycleByType(int i, long j) {
        synchronized (DexUtil.class) {
            try {
                if (d == null) {
                    d = b();
                }
                if (d != null) {
                    long updateCycle = d.getUpdateCycle(i, j);
                    return updateCycle;
                }
            } catch (Throwable th) {
            }
        }
        return j;
    }

    public static JSONObject handerBoxValueMap(Map<String, Object> map) {
        JSONObject jSONObject = null;
        try {
            ParseBoxInterface parseBoxImpl = getParseBoxImpl(false);
            if (parseBoxImpl != null) {
                jSONObject = parseBoxImpl.handerValueMap(map);
            }
        } catch (Throwable th) {
        }
        return jSONObject;
    }

    public static Map<String, Object> handerBubbleValueMap(Map<String, Object> map) {
        JSONObject changeMapToJason;
        Object obj;
        Throwable th;
        Map<String, Object> map2 = null;
        String a = w.a();
        try {
            changeMapToJason = JsonUtil.changeMapToJason(map);
            try {
                ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerBubbleValueMap", changeMapToJason);
                Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilBubble");
                if (classBymap != null) {
                    ParseBubbleInterface parseBubbleInterface = (ParseBubbleInterface) classBymap.newInstance();
                    if (parseBubbleInterface != null) {
                        map2 = parseBubbleInterface.handerValueMap(map);
                        if (map2 != null) {
                            i.a(new k(12, ParseItemManager.STATE, "8"));
                        }
                    }
                }
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerBubbleValueMap", changeMapToJason, JsonUtil.changeMapToJason(map2));
            } catch (Throwable th2) {
                th = th2;
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerBubbleValueMap", changeMapToJason, JsonUtil.changeMapToJason(null));
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            changeMapToJason = null;
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerBubbleValueMap", changeMapToJason, JsonUtil.changeMapToJason(null));
            throw th;
        }
        return map2;
    }

    public static JSONObject handerContactValueMap(Map<String, Object> map) {
        JSONObject jSONObject = null;
        try {
            ParseContactInterface parseContactImpl = getParseContactImpl(false);
            if (parseContactImpl != null) {
                jSONObject = parseContactImpl.handerValueMap(map);
            }
        } catch (Throwable th) {
        }
        return jSONObject;
    }

    public static Map<String, Object> handerDateValueMap(Map<String, Object> map) {
        try {
            ParseDateInterface parseDateImpl = getParseDateImpl(false);
            if (parseDateImpl != null) {
                return parseDateImpl.handerValueMap(map);
            }
        } catch (Throwable th) {
            new StringBuilder("handerDateValueMap: ").append(th.getMessage());
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Map<String, Object> handerNotificationValueMap(Map<String, Object> map) {
        Throwable th;
        Map map2;
        Map<String, Object> map3 = null;
        String a = w.a();
        JSONObject changeMapToJason;
        try {
            changeMapToJason = JsonUtil.changeMapToJason(map);
            try {
                ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerNotificationValueMap", changeMapToJason);
                ParseNotificationInterface parseNotificationImpl = getParseNotificationImpl(false);
                if (parseNotificationImpl != null) {
                    map3 = parseNotificationImpl.handerValueMap(map);
                    if (map3 != null) {
                        i.a(new k(12, ParseItemManager.STATE, "4"));
                    }
                }
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerNotificationValueMap", changeMapToJason, JsonUtil.changeMapToJason(map3));
            } catch (Throwable th2) {
                Throwable th3 = th2;
                Map<String, Object> map4 = map3;
                Object obj = changeMapToJason;
                th = th3;
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerNotificationValueMap", map3, JsonUtil.changeMapToJason(map2));
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            map2 = map3;
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerNotificationValueMap", map3, JsonUtil.changeMapToJason(map2));
            throw th;
        }
        return map3;
    }

    public static Map<String, Object> handerPayValueMap(Map<String, Object> map) {
        Map<String, Object> map2 = null;
        try {
            ParsePayInterface parsePayImpl = getParsePayImpl(false);
            if (parsePayImpl != null) {
                map2 = parsePayImpl.handerValueMap(map);
            }
        } catch (Throwable th) {
        }
        return map2;
    }

    public static Map<String, Object> handerRemindValueMap(Map<String, Object> map) {
        Map<String, Object> map2 = null;
        try {
            ParseRemindInterface parseRemindImpl = getParseRemindImpl(false);
            if (parseRemindImpl != null) {
                map2 = parseRemindImpl.handerValueMap(map);
            }
        } catch (Throwable th) {
        }
        return map2;
    }

    public static Map<String, Object> handerValueMap(Map<String, Object> map) {
        Throwable th;
        Object obj;
        Map<String, Object> map2 = null;
        String a = w.a();
        JSONObject changeMapToJason;
        try {
            changeMapToJason = JsonUtil.changeMapToJason(map);
            try {
                ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerValueMap", changeMapToJason);
                ParseCardInterface parseCardImpl = getParseCardImpl(false);
                if (parseCardImpl != null) {
                    map2 = parseCardImpl.handerValueMap(map);
                }
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerValueMap", changeMapToJason, JsonUtil.changeMapToJason(map2));
            } catch (Throwable th2) {
                Throwable th3 = th2;
                JSONObject jSONObject = changeMapToJason;
                th = th3;
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerValueMap", obj, JsonUtil.changeMapToJason(map2));
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            obj = map2;
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerValueMap", obj, JsonUtil.changeMapToJason(map2));
            throw th;
        }
        return map2;
    }

    public static Map<String, Object> handerValueMap(Map<String, Object> map, String str) {
        JSONObject changeMapToJason;
        Throwable th;
        Object obj;
        Map<String, Object> map2 = null;
        String a = w.a();
        try {
            changeMapToJason = JsonUtil.changeMapToJason(map);
            try {
                ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerValueMap", changeMapToJason, str);
                OnlineViewInterface onlineViewImpl = getOnlineViewImpl(false);
                if (onlineViewImpl != null) {
                    map2 = onlineViewImpl.handerValueMap(map, str);
                }
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerValueMap", changeMapToJason, str, JsonUtil.changeMapToJason(map2));
            } catch (Throwable th2) {
                Throwable th3 = th2;
                JSONObject jSONObject = changeMapToJason;
                th = th3;
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerValueMap", obj, str, JsonUtil.changeMapToJason(map2));
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            obj = map2;
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "handerValueMap", obj, str, JsonUtil.changeMapToJason(map2));
            throw th;
        }
        return map2;
    }

    public static Map<String, Object> handerValueMapByType(int i, Map<String, Object> map) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseVerifyCode");
            if (classBymap != null) {
                ParseVerifyCodeInterface parseVerifyCodeInterface = (ParseVerifyCodeInterface) classBymap.newInstance();
                if (parseVerifyCodeInterface != null) {
                    return parseVerifyCodeInterface.handerValueMapByType(i, map);
                }
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static JSONObject handerWatchValueMap(Map<String, Object> map) {
        JSONObject jSONObject = null;
        try {
            ParseWatchInterface parseWatchImpl = getParseWatchImpl(false);
            if (parseWatchImpl != null) {
                jSONObject = parseWatchImpl.handerValueMap(map);
            }
        } catch (Throwable th) {
        }
        return jSONObject;
    }

    public static void handleParseMsg(Context context, String str, String str2, String str3, String str4, long j, Map<String, String> map, Map<String, Object> map2) {
        try {
            JSONObject changeMapToJason = JsonUtil.changeMapToJason(map2);
            if (changeMapToJason != null) {
                cn.com.xy.sms.sdk.a.a.f.execute(new b(context, str, str2, str3, str4, j, map, changeMapToJason));
            }
        } catch (Throwable th) {
        }
    }

    public static synchronized Map<String, Object> handleValidTime(Map<String, Object> map) {
        synchronized (DexUtil.class) {
            if (map != null) {
                try {
                    if (e == null) {
                        e = c();
                    }
                    if (e != null) {
                        Map<String, Object> handleValidTime = e.handleValidTime(map);
                        return handleValidTime;
                    }
                } catch (Throwable th) {
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public static Object[] handlerParamsToJSONObjectIfNeed(Object[] objArr) {
        int i = 0;
        if (objArr == null || objArr.length == 0) {
            return null;
        }
        while (i < objArr.length) {
            try {
                Object obj = objArr[i];
                if (obj != null) {
                    if ((obj instanceof XyCallBack) || (obj instanceof Context) || obj.getClass().getName().startsWith("android.")) {
                        objArr[i] = new JSONObject().put("objectToJson", true).put("className", obj.getClass().getName());
                    } else if (obj.getClass().getName().startsWith("cn.com.xy.sms")) {
                        JSONObject parseObjectToJson = JsonUtil.parseObjectToJson(obj);
                        if (parseObjectToJson != null) {
                            objArr[i] = parseObjectToJson;
                        }
                    }
                }
                i++;
            } catch (Throwable th) {
            }
        }
        return objArr;
    }

    public static void init() {
        try {
            if (new File(Constant.getJarPath()).exists()) {
                if (n.a(Constant.getJarPath()).booleanValue()) {
                    b = getDexClassLoader(true);
                    c = getOnlineParseImpl(true);
                    if (LogManager.debug) {
                    }
                } else if (LogManager.debug) {
                }
            }
        } catch (Throwable th) {
            th.getMessage();
        }
    }

    public static boolean init(Set<String> set) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilEmail");
            if (classBymap != null) {
                return ((Boolean) classBymap.getMethod("init", new Class[]{Set.class}).invoke(classBymap, new Object[]{set})).booleanValue();
            }
        } catch (Throwable th) {
        }
        return false;
    }

    public static void initOnlineUpdateCycleConfig() {
        d = b();
        k = null;
    }

    public static void initParseVerifyCodeValidTime() {
        e = c();
    }

    public static void insertLog(String str, String str2, JSONObject jSONObject) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.LogService");
            if (classBymap != null) {
                classBymap.getMethod("insert", new Class[]{String.class, String.class, JSONObject.class}).invoke(classBymap, new Object[]{str, str2, jSONObject});
            }
        } catch (Throwable th) {
        }
    }

    public static boolean isEnterpriseEmail(String str, Map<String, Object> map) {
        try {
            return ((Boolean) methodInvoke("cn.com.xy.sms.sdk.Iservice.ParseUtilEmail", "canRec", new Class[]{String.class, Map.class}, new Object[]{str, map})).booleanValue();
        } catch (Throwable th) {
            return false;
        }
    }

    public static boolean isEnterpriseSms(Context context, String str, String str2, Map<String, String> map) {
        try {
            OnlineParseInterface onlineParseImpl = getOnlineParseImpl(false);
            return onlineParseImpl == null ? PopupUtil.isEnterpriseSms(context, str, str2, map) : onlineParseImpl.isEnterpriseSms(context, str, str2, map);
        } catch (Throwable th) {
            return PopupUtil.isEnterpriseSms(context, str, str2, map);
        }
    }

    public static boolean isNoAreaCodeFixedPhone(String str) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.OnlineUpdateCycleConfig");
            if (classBymap != null) {
                return ((Boolean) classBymap.getMethod("isNoAreaCodeFixedPhone", new Class[]{String.class}).invoke(classBymap, new Object[]{str})).booleanValue();
            }
        } catch (Throwable th) {
        }
        if (!StringUtils.isNull(str)) {
            if (str.length() == 7 || str.length() == 8) {
                return true;
            }
        }
        return false;
    }

    public static Boolean isOperatorsPhoneType(String str) {
        Boolean valueOf = Boolean.valueOf(false);
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilCard");
            if (classBymap != null) {
                return (Boolean) classBymap.getMethod("isOperatorsPhone", new Class[]{String.class}).invoke(classBymap, new Object[]{str});
            }
        } catch (Throwable th) {
        }
        return valueOf;
    }

    public static int isServiceChoose(String str, String str2) {
        int i = -1;
        try {
            OnlineParseInterface onlineParseImpl = getOnlineParseImpl(false);
            if (onlineParseImpl != null) {
                i = onlineParseImpl.isServiceChoose(str, str2);
            }
            return i;
        } catch (Throwable th) {
            return -2;
        }
    }

    public static boolean isVCEmail(String str) {
        try {
            return ((Boolean) methodInvoke("cn.com.xy.sms.sdk.Iservice.ParseUtilEmail", "isVCEmail", new Class[]{String.class}, new Object[]{str})).booleanValue();
        } catch (Throwable th) {
            return false;
        }
    }

    public static boolean isVCEmail(String str, String str2, String str3, String str4, Map<String, String> map) {
        try {
            return ((Boolean) methodInvoke("cn.com.xy.sms.sdk.Iservice.ParseUtilEmail", "isVCEmail", new Class[]{String.class, String.class, String.class, String.class, Map.class}, new Object[]{str, str2, str3, str4, map})).booleanValue();
        } catch (Throwable th) {
            return false;
        }
    }

    public static Object methodInvoke(String str, String str2, Class<?>[] clsArr, Object[] objArr) {
        Class classBymap = getClassBymap(null, str);
        return classBymap == null ? null : classBymap.getMethod(str2, clsArr).invoke(classBymap, objArr);
    }

    public static String multiReplace(String str) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilMultiple");
            if (classBymap != null) {
                return (String) classBymap.getMethod("multiReplace", new Class[]{String.class}).invoke(classBymap, new Object[]{str});
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static Map<String, Object> parseEmail(String str, String str2, Map<String, String> map) {
        Map<String, Object> map2;
        Throwable th;
        Object obj = null;
        String a = w.a();
        try {
            Map<String, Object> map3;
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseEmail", str, str2, map);
            if (ParseItemManager.isInitData()) {
                Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilEmail");
                if (classBymap != null) {
                    if (((Map) classBymap.getMethod("parseEmail", new Class[]{String.class, String.class, Map.class}).invoke(classBymap, new Object[]{str, str2, map})) != null) {
                        Class classBymap2 = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilEmailMapping");
                        if (classBymap2 != null) {
                            map2 = (Map) classBymap2.getMethod("handerValueMap", new Class[]{Map.class}).invoke(classBymap2, new Object[]{r0});
                        }
                    }
                }
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseEmail", str, str2, map, map3);
                return map3;
            }
            map2 = new HashMap();
            try {
                map2.put("parseStatu", Integer.valueOf(-1));
            } catch (Throwable th2) {
                Throwable th3 = th2;
                map3 = map2;
                th = th3;
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseEmail", str, str2, map, obj);
                throw th;
            }
            map3 = map2;
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseEmail", str, str2, map, map3);
            return map3;
        } catch (Throwable th4) {
            th = th4;
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseEmail", str, str2, map, obj);
            throw th;
        }
    }

    public static JSONArray parseMsgForCardArray(JSONObject jSONObject, Map<String, String> map) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilBubble");
            if (classBymap != null) {
                return (JSONArray) classBymap.getMethod("parseMsgForCardArray", new Class[]{JSONObject.class, Map.class}).invoke(classBymap, new Object[]{jSONObject, map});
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static Map<String, Object> parseMsgToMap(String str, String str2, Map<String, String> map) {
        Map<String, Object> map2 = null;
        try {
            String md5 = StringUtils.getMD5(new StringBuilder(String.valueOf(str)).append(str2).toString());
            Map map3 = (Map) i.get(md5);
            if (map3 == null) {
                if ((System.currentTimeMillis() - j <= 30000 ? 1 : null) == null) {
                    i.evictAll();
                }
                OnlineParseInterface onlineParseImpl = getOnlineParseImpl(false);
                if (onlineParseImpl != null) {
                    map2 = onlineParseImpl.parseMessage(str, str2, map);
                    j = System.currentTimeMillis();
                    if (map2 != null && map2.size() > 2) {
                        HashMap hashMap = new HashMap();
                        hashMap.putAll(map2);
                        i.put(md5, hashMap);
                    }
                }
                return map2;
            }
            Map hashMap2 = new HashMap();
            hashMap2.putAll(map3);
            hashMap2.put("from_cache", "");
            return hashMap2;
        } catch (Throwable th) {
            th.getMessage();
        }
    }

    public static String[] parseMsgToNewContacts(String str, String str2, String str3, String[] strArr) {
        String[] strArr2 = null;
        try {
            OnlineParseInterface onlineParseImpl = getOnlineParseImpl(false);
            if (onlineParseImpl != null) {
                strArr2 = onlineParseImpl.parseMsgToNewContacts(str, str2, str3, strArr);
            }
            return strArr2;
        } catch (Throwable th) {
            return null;
        }
    }

    public static String parseRecogniseValue(String str, String str2, long j, Map map) {
        String a = w.a();
        String str3 = "";
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseRecogniseValue", str, str2, Long.valueOf(j), map);
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilMultiple");
            if (classBymap == null) {
                ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseRecogniseValue", str, str2, Long.valueOf(j), map, str3);
                return null;
            }
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseRecogniseValue", str, str2, Long.valueOf(j), map, (String) classBymap.getMethod("multiParse", new Class[]{String.class, String.class, Long.TYPE, Map.class}).invoke(classBymap, new Object[]{str, str2, Long.valueOf(j), map}));
            return (String) classBymap.getMethod("multiParse", new Class[]{String.class, String.class, Long.TYPE, Map.class}).invoke(classBymap, new Object[]{str, str2, Long.valueOf(j), map});
        } catch (Throwable th) {
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseRecogniseValue", str, str2, Long.valueOf(j), map, str3);
        }
    }

    public static int parseSensitive(String str) {
        String a = w.a();
        try {
            int i;
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseSensitive", str);
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseSensitiveUtil");
            if (classBymap == null) {
                i = 0;
            } else {
                i = ((Integer) classBymap.getMethod("parseSensitive", new Class[]{String.class}).invoke(classBymap, new Object[]{str})).intValue();
            }
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseSensitive", str);
            return i;
        } catch (Throwable th) {
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseSensitive", str);
        }
    }

    public static String[] parseShard(String str, String str2, String str3) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilMultiple");
            if (classBymap != null) {
                return (String[]) classBymap.getMethod("check", new Class[]{String.class, String.class, String.class}).invoke(classBymap, new Object[]{str, str2, str3});
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static Map<String, Object> parseVerCode(String str, String str2, String str3, Map<String, String> map) {
        Map<String, Object> map2;
        Throwable th;
        Object obj = null;
        String a = w.a();
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseVerCode", str, str2, str3, map);
            if (ParseItemManager.isInitData()) {
                Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilEmail");
                if (classBymap == null) {
                    map2 = null;
                } else {
                    map2 = (Map) classBymap.getMethod("parseVerCode", new Class[]{String.class, String.class, String.class, Map.class}).invoke(classBymap, new Object[]{str, str2, str3, map});
                }
            } else {
                map2 = new HashMap();
                try {
                    map2.put("parseStatu", Integer.valueOf(-1));
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    Map<String, Object> map3 = map2;
                    th = th3;
                    ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseVerCode", str, str2, str3, map, obj);
                    throw th;
                }
            }
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseVerCode", str, str2, str3, map, map2);
        } catch (Throwable th4) {
            th = th4;
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.dex.DexUtil", "parseVerCode", str, str2, str3, map, obj);
            throw th;
        }
        return map2;
    }

    public static Map<String, Object> parseVerifyCodeToMap(String str, String str2, Map<String, String> map) {
        try {
            OnlineParseInterface onlineParseImpl = getOnlineParseImpl(false);
            if (onlineParseImpl != null) {
                return onlineParseImpl.parseVerCode(str, str2, map);
            }
        } catch (Throwable th) {
            th.getMessage();
        }
        return null;
    }

    public static void postCallback(Integer num, String str) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.LogService");
            if (classBymap != null) {
                classBymap.getMethod("postCallback", new Class[]{Integer.class, String.class}).invoke(classBymap, new Object[]{num, str});
            }
        } catch (Throwable th) {
        }
    }

    public static void putActionDataToDataSource(JSONObject jSONObject, Map<String, Object> map, Map<String, Object> map2) {
        if (!(jSONObject == null || map == null)) {
            try {
                if (map.containsKey("NEW_ADACTION")) {
                    Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.WizardServiceMenuData");
                    if (classBymap == null) {
                        a(jSONObject, map);
                        return;
                    }
                    classBymap.getMethod("putActionDataToDataSource", new Class[]{JSONObject.class, Map.class, Map.class}).invoke(classBymap, new Object[]{jSONObject, map, map2});
                }
            } catch (Throwable th) {
                a(jSONObject, map);
            }
        }
    }

    public static JSONObject queryConversationMsg(Context context, String str, JSONObject jSONObject, Map map) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.ParseUtilConversationHelper");
            if (classBymap != null) {
                return (JSONObject) classBymap.getMethod("queryConversationMsg", new Class[]{Context.class, String.class, JSONObject.class, Map.class}).invoke(classBymap, new Object[]{context, str, jSONObject, map});
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static String queryLog(Integer num, String str, String str2) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.LogService");
            if (classBymap != null) {
                return (String) classBymap.getMethod("queryLog", new Class[]{Integer.class, String.class, String.class}).invoke(classBymap, new Object[]{num, str, str2});
            }
        } catch (Throwable th) {
        }
        return "";
    }

    public static JSONArray queryLog(String str, String str2, String[] strArr, JSONObject jSONObject, String str3, String str4) {
        try {
            Class classBymap = getClassBymap(null, "cn.com.xy.sms.sdk.Iservice.LogService");
            if (classBymap != null) {
                return (JSONArray) classBymap.getMethod(SearchIntents.EXTRA_QUERY, new Class[]{String.class, String.class, String[].class, JSONObject.class, String.class, String.class}).invoke(classBymap, new Object[]{str, str2, strArr, jSONObject, str3, str4});
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static void removeClassLoaderBySubname(String str) {
        try {
            f.remove(new StringBuilder(String.valueOf(str)).append("_ClassLoader").toString());
            g.remove(new StringBuilder(String.valueOf(str)).append("_Class").toString());
        } catch (Throwable th) {
            th.getMessage();
        }
    }

    public static void saveExceptionLog(Throwable th) {
        try {
            cn.com.xy.sms.sdk.a.a.f.execute(new e(th));
        } catch (Throwable th2) {
        }
    }

    public static void saveLogIn(String str, String str2, String str3, Object... objArr) {
        try {
            cn.com.xy.sms.sdk.a.a.f.execute(new c(str, str2, str3, objArr));
        } catch (Throwable th) {
        }
    }

    public static void saveLogOut(String str, String str2, String str3, Object... objArr) {
        try {
            cn.com.xy.sms.sdk.a.a.f.execute(new d(str, str2, str3, objArr));
        } catch (Throwable th) {
        }
    }
}
