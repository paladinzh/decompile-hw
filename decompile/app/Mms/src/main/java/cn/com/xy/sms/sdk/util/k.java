package cn.com.xy.sms.sdk.util;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Process;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.a.a;
import cn.com.xy.sms.sdk.db.entity.E;
import cn.com.xy.sms.sdk.db.entity.H;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.e;
import cn.com.xy.sms.sdk.db.entity.g;
import cn.com.xy.sms.sdk.db.entity.h;
import cn.com.xy.sms.sdk.db.entity.v;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.i;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.queue.b;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkParamUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class k extends Thread {
    private static boolean a = false;
    private static long b = 0;
    private static long c = 600000;
    private static int d = 20;

    public static synchronized void a() {
        synchronized (k.class) {
            if (!a) {
                a = true;
                k kVar = new k();
                kVar.setPriority(1);
                kVar.start();
            }
        }
    }

    private static void a(g gVar, boolean z) {
        Object obj = 1;
        long currentTimeMillis = System.currentTimeMillis();
        if (gVar.f == 0) {
            if (!z) {
                if ((gVar.h > currentTimeMillis ? 1 : null) == null) {
                }
            }
            if (!StringUtils.isNull(gVar.d)) {
                String str = gVar.b + ".zip";
                if (f.f(gVar.d, Constant.getFilePath(), str) != -1 && XyUtil.upZipFile(Constant.getFilePath() + str, Constant.getPARSE_PATH())) {
                    String str2 = gVar.b + ".sql";
                    File file = new File(Constant.getPARSE_PATH() + str2);
                    if (file.exists()) {
                        String str3 = gVar.b + ".txt";
                        str2 = StringUtils.getFileMD5(Constant.getPARSE_PATH() + str2);
                        str3 = n.a(Constant.getPARSE_PATH(), str3);
                        boolean z2 = LogManager.debug;
                        if (str2.equals(str3)) {
                            str2 = gVar.b;
                            try {
                                if (!StringUtils.isNull(str2)) {
                                    ParseItemManager.deleteTimeOutMatchId(str2.substring(2));
                                }
                            } catch (Throwable th) {
                            }
                            try {
                                Constant.getContext();
                                if (!a(file)) {
                                    return;
                                }
                            } catch (Throwable th2) {
                            }
                        }
                    }
                    str2 = gVar.b;
                    try {
                        long currentTimeMillis2 = System.currentTimeMillis();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("last_load_time", new StringBuilder(String.valueOf(currentTimeMillis2)).toString());
                        contentValues.put("status", new StringBuilder("1").toString());
                        DBManager.update("tb_jar_list", contentValues, "name = ? ", new String[]{str2});
                        SdkParamUtil.setParamValue(Constant.getContext(), Constant.SMART_DATA_UPDATE_TIME, new StringBuilder(String.valueOf(currentTimeMillis2)).toString());
                    } catch (Throwable th3) {
                    }
                    f.a(Constant.getPARSE_PATH(), gVar.b + "_", ".jar", gVar.b + "_" + gVar.c + ".jar");
                    f.b(gVar.b + "_", ".dex", gVar.b + "_" + gVar.c + ".dex");
                    str2 = gVar.b + "_" + gVar.c + ".jar";
                    f.a(Constant.getPARSE_PATH(), gVar.b + ".jar", str2);
                    f.a.put(gVar.b, str2);
                    if ("parseUtilMain".equals(gVar.b) || "ParseSimpleBubbleUtil".equals(gVar.b)) {
                        DexUtil.removeClassLoaderBySubname(gVar.b);
                        DexUtil.init();
                    } else if ("OnlineUpdateCycleConfig".equals(gVar.b)) {
                        DexUtil.removeClassLoaderBySubname(gVar.b);
                        DexUtil.initOnlineUpdateCycleConfig();
                    } else if ("ParseVerifyCodeValidTime".equals(gVar.b)) {
                        DexUtil.removeClassLoaderBySubname(gVar.b);
                        DexUtil.initParseVerifyCodeValidTime();
                    } else {
                        DexUtil.getClassBymap(null, "cn.com.xy.sms.sdk.Iservice." + gVar.b, true);
                    }
                    f.c(Constant.getFilePath() + str);
                    if (System.currentTimeMillis() > b + DexUtil.getUpdateCycleByType(14, c)) {
                        obj = null;
                    }
                    if (obj == null) {
                        MatchCacheManager.resetLastParseTime(0);
                        b = System.currentTimeMillis();
                    }
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", String.format("download %s complete", new Object[]{gVar.b}), null);
                    return;
                }
                return;
            }
            return;
        }
        boolean z3;
        if (gVar.f != 1) {
            z3 = LogManager.debug;
        } else {
            z3 = LogManager.debug;
        }
    }

    static /* synthetic */ void a(String str) {
        Map a = i.a(str);
        if (a != null) {
            long currentTimeMillis = System.currentTimeMillis();
            Object obj = a.get("updataJars");
            if (obj != null) {
                JSONArray jSONArray = (JSONArray) obj;
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jSONObject = jSONArray.getJSONObject(i);
                    String string = jSONObject.getString("name");
                    String string2 = jSONObject.getString(NumberInfo.VERSION_KEY);
                    String string3 = jSONObject.getString(Constant.URLS);
                    long currentTimeMillis2 = System.currentTimeMillis();
                    long j = ((long) jSONObject.getInt("delayStart")) + currentTimeMillis;
                    long j2 = ((long) jSONObject.getInt("delayEnd")) + currentTimeMillis;
                    String optString = jSONObject.optString("pver");
                    try {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(NumberInfo.VERSION_KEY, string2);
                        contentValues.put(Constant.URLS, string3);
                        contentValues.put("status", Integer.valueOf(0));
                        contentValues.put("update_time", new StringBuilder(String.valueOf(currentTimeMillis2)).toString());
                        contentValues.put("delaystart", new StringBuilder(String.valueOf(j)).toString());
                        contentValues.put("delayend", new StringBuilder(String.valueOf(j2)).toString());
                        contentValues.put("pver", optString);
                        DBManager.update("tb_jar_list", contentValues, "name = ? ", new String[]{string});
                    } catch (Throwable th) {
                    }
                }
            }
            if (a.containsKey("emergencyArray")) {
                e.a((JSONArray) a.get("emergencyArray"));
            }
        }
    }

    private static void a(String str, SQLiteDatabase sQLiteDatabase) {
        try {
            if (!StringUtils.isNull(str)) {
                long update;
                ContentValues contentValues = new ContentValues();
                String substring = str.substring(0, str.indexOf(","));
                String substring2 = str.substring(str.indexOf(",") + 1, str.length());
                String substring3 = substring2.substring(0, substring2.indexOf(","));
                substring2 = substring2.substring(substring2.indexOf(",") + 1, substring2.length());
                String substring4 = substring2.substring(0, substring2.indexOf(","));
                substring2 = substring2.substring(substring2.indexOf(",") + 1, substring2.length());
                String substring5 = substring2.substring(0, substring2.indexOf(","));
                substring2 = substring2.substring(substring2.indexOf(",") + 1, substring2.length());
                contentValues.put(ParseItemManager.SCENE_ID, substring);
                contentValues.put(ParseItemManager.MATCH_ID, substring3);
                contentValues.put(ParseItemManager.REGEX_TYPE, substring4);
                contentValues.put(ParseItemManager.VERSION_CODE, substring5);
                contentValues.put(ParseItemManager.REGEX_TEXT, substring2);
                try {
                    update = (long) sQLiteDatabase.update(ParseItemManager.TABLE_NAME, contentValues, "scene_id= ? and match_id =?", new String[]{substring, substring3});
                } catch (Throwable th) {
                    update = 0;
                }
                if (update == 0) {
                    try {
                        sQLiteDatabase.insert(ParseItemManager.TABLE_NAME, null, contentValues);
                    } catch (Throwable th2) {
                    }
                }
            }
        } catch (Throwable th3) {
        }
    }

    private static void a(List<g> list) {
        try {
            Map b = v.b();
            String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), "EM_VERSION");
            if (StringUtils.isNull(stringParam)) {
                stringParam = ThemeUtil.SET_NULL_STR;
            }
            String a = j.a((List) list, b, stringParam);
            if (!StringUtils.isNull(a)) {
                NetUtil.executeHttpRequest(0, a, new l(), NetUtil.getPopupServiceUrl() + NetUtil.UpdateRecognitionJarRequest, null, false);
            }
        } catch (Throwable th) {
        }
    }

    public static void a(Map<String, String> map, XyCallBack xyCallBack) {
        Object obj = null;
        try {
            h();
            List a = h.a(Long.MAX_VALUE);
            if (!(a == null || a.isEmpty())) {
                int size = a.size();
                int i = 0;
                while (i < size) {
                    a((g) a.get(i), true);
                    i++;
                    int i2 = 1;
                }
            }
            if (obj != null) {
                b();
            }
            if (cn.com.xy.sms.sdk.db.i.c(cn.com.xy.sms.sdk.db.i.c()) || c()) {
                XyUtil.doXycallBack(xyCallBack, "0");
            } else {
                XyUtil.doXycallBack(xyCallBack, "1");
            }
        } catch (Throwable th) {
        }
    }

    public static void a(boolean z, boolean z2) {
        try {
            if (NetUtil.isEnhance()) {
                long currentTimeMillis;
                h();
                if (z2) {
                    int intParam = SysParamEntityManager.getIntParam(Constant.getContext(), Constant.ONLINE_UPDATE_SDK_PERIOD);
                    if (intParam <= 0) {
                        intParam = 1;
                    }
                    currentTimeMillis = System.currentTimeMillis() - DexUtil.getUpdateCycleByType(8, ((long) intParam) * 86400000);
                } else {
                    currentTimeMillis = Long.MAX_VALUE;
                }
                List a = h.a(currentTimeMillis);
                if (a != null && !a.isEmpty()) {
                    int size = a.size();
                    double ceil = Math.ceil(Double.parseDouble(String.valueOf(size)) / ((double) d));
                    int i = 0;
                    while (true) {
                        if ((((double) i) < ceil ? 1 : null) == null) {
                            break;
                        }
                        List subList = a.subList(i * d, ((double) (i + 1)) == ceil ? size : (i + 1) * d);
                        try {
                            Map b = v.b();
                            String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), "EM_VERSION");
                            if (StringUtils.isNull(stringParam)) {
                                stringParam = ThemeUtil.SET_NULL_STR;
                            }
                            String a2 = j.a(subList, b, stringParam);
                            if (!StringUtils.isNull(a2)) {
                                NetUtil.executeHttpRequest(0, a2, new l(), NetUtil.getPopupServiceUrl() + NetUtil.UpdateRecognitionJarRequest, null, false);
                            }
                        } catch (Throwable th) {
                        }
                        i++;
                    }
                    SysParamEntityManager.setParam("JARS_UPDATE_TIME", String.valueOf(System.currentTimeMillis()));
                    v.a();
                    b.a();
                    if (z) {
                        f();
                    }
                }
            }
        } catch (Throwable th2) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean a(File file) {
        BufferedReader bufferedReader;
        LineNumberReader lineNumberReader;
        Throwable th;
        Throwable th2;
        SQLiteDatabase sQLiteDatabase;
        BufferedReader bufferedReader2;
        if (!file.exists()) {
            return false;
        }
        SQLiteDatabase sQLiteDatabase2 = null;
        LineNumberReader lineNumberReader2 = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            try {
                lineNumberReader = new LineNumberReader(bufferedReader);
                try {
                    SQLiteDatabase a = a.a();
                    try {
                        a.beginTransaction();
                        while (true) {
                            String readLine = lineNumberReader.readLine();
                            if (readLine != null) {
                                try {
                                    if (StringUtils.isNull(readLine)) {
                                        continue;
                                    } else {
                                        ContentValues contentValues = new ContentValues();
                                        String substring = readLine.substring(0, readLine.indexOf(","));
                                        readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length());
                                        String substring2 = readLine.substring(0, readLine.indexOf(","));
                                        readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length());
                                        String substring3 = readLine.substring(0, readLine.indexOf(","));
                                        readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length());
                                        String substring4 = readLine.substring(0, readLine.indexOf(","));
                                        readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length());
                                        contentValues.put(ParseItemManager.SCENE_ID, substring);
                                        contentValues.put(ParseItemManager.MATCH_ID, substring2);
                                        contentValues.put(ParseItemManager.REGEX_TYPE, substring3);
                                        contentValues.put(ParseItemManager.VERSION_CODE, substring4);
                                        contentValues.put(ParseItemManager.REGEX_TEXT, readLine);
                                        long j = 0;
                                        try {
                                            j = (long) a.update(ParseItemManager.TABLE_NAME, contentValues, "scene_id= ? and match_id =?", new String[]{substring, substring2});
                                        } catch (Throwable th3) {
                                            th = th3;
                                            sQLiteDatabase2 = a;
                                            lineNumberReader2 = lineNumberReader;
                                            th2 = th;
                                        }
                                        if (j == 0) {
                                            a.insert(ParseItemManager.TABLE_NAME, null, contentValues);
                                        } else {
                                            continue;
                                        }
                                    }
                                } catch (Throwable th32) {
                                    th = th32;
                                    sQLiteDatabase2 = a;
                                    lineNumberReader2 = lineNumberReader;
                                    th2 = th;
                                }
                            } else {
                                try {
                                    break;
                                } catch (Throwable th4) {
                                }
                            }
                        }
                        file.getPath();
                        f.a(file);
                        a.a(null, false, lineNumberReader, bufferedReader, a);
                        return true;
                    } catch (Throwable th322) {
                        th = th322;
                        sQLiteDatabase2 = a;
                        lineNumberReader2 = lineNumberReader;
                        th2 = th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    lineNumberReader2 = lineNumberReader;
                    th2 = th;
                    try {
                        file.getPath();
                        f.a(file);
                    } catch (Throwable th6) {
                    }
                    a.a(null, false, lineNumberReader2, bufferedReader, sQLiteDatabase2);
                    throw th2;
                }
            } catch (Throwable th7) {
                th2 = th7;
                file.getPath();
                f.a(file);
                a.a(null, false, lineNumberReader2, bufferedReader, sQLiteDatabase2);
                throw th2;
            }
        } catch (Throwable th8) {
            th = th8;
            bufferedReader = null;
            th2 = th;
            file.getPath();
            f.a(file);
            a.a(null, false, lineNumberReader2, bufferedReader, sQLiteDatabase2);
            throw th2;
        }
    }

    public static void b() {
        try {
            int parseVersion = ParseManager.getParseVersion(Constant.getContext(), null) + 1;
            SdkParamUtil.setParamValue(Constant.getContext(), "PARSE_VERSION", new StringBuilder(String.valueOf(parseVersion)).toString());
            if ((System.currentTimeMillis() < SysParamEntityManager.getLongParam(Constant.CONFIG_NOTIFY_TIMEMS, 600000, Constant.getContext()) + Constant.lastVersionChangeTime ? 1 : null) == null) {
                DuoquUtils.getSdkDoAction().parseVersionChange(Constant.getContext(), parseVersion);
                Constant.lastVersionChangeTime = System.currentTimeMillis();
            }
        } catch (Throwable th) {
        }
    }

    private static void b(String str) {
        Map a = i.a(str);
        if (a != null) {
            long currentTimeMillis = System.currentTimeMillis();
            Object obj = a.get("updataJars");
            if (obj != null) {
                JSONArray jSONArray = (JSONArray) obj;
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jSONObject = jSONArray.getJSONObject(i);
                    String string = jSONObject.getString("name");
                    String string2 = jSONObject.getString(NumberInfo.VERSION_KEY);
                    String string3 = jSONObject.getString(Constant.URLS);
                    long currentTimeMillis2 = System.currentTimeMillis();
                    long j = ((long) jSONObject.getInt("delayStart")) + currentTimeMillis;
                    long j2 = ((long) jSONObject.getInt("delayEnd")) + currentTimeMillis;
                    String optString = jSONObject.optString("pver");
                    try {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(NumberInfo.VERSION_KEY, string2);
                        contentValues.put(Constant.URLS, string3);
                        contentValues.put("status", Integer.valueOf(0));
                        contentValues.put("update_time", new StringBuilder(String.valueOf(currentTimeMillis2)).toString());
                        contentValues.put("delaystart", new StringBuilder(String.valueOf(j)).toString());
                        contentValues.put("delayend", new StringBuilder(String.valueOf(j2)).toString());
                        contentValues.put("pver", optString);
                        DBManager.update("tb_jar_list", contentValues, "name = ? ", new String[]{string});
                    } catch (Throwable th) {
                    }
                }
            }
            if (a.containsKey("emergencyArray")) {
                e.a((JSONArray) a.get("emergencyArray"));
            }
        }
    }

    public static boolean b(boolean z, boolean z2) {
        try {
            h();
            a(false, false);
            return h.g();
        } catch (Throwable th) {
            return false;
        }
    }

    private static void c(String str) {
        try {
            if (!StringUtils.isNull(str)) {
                ParseItemManager.deleteTimeOutMatchId(str.substring(2));
            }
        } catch (Throwable th) {
        }
    }

    public static boolean c() {
        try {
            return h.g();
        } catch (Throwable th) {
            return false;
        }
    }

    private static void d() {
        try {
            int intParam = SysParamEntityManager.getIntParam(Constant.getContext(), Constant.ONLINE_UPDATE_SDK);
            KeyManager.initAppKey();
            if ((!"5Mj22a4wHUAWEICARD".equals(KeyManager.channel) || intParam != 0) && NetUtil.checkAccessNetWork(2)) {
                a(true, true);
                if (intParam != 0) {
                    E.a(H.UPDATE_PUBINFO);
                    g.b();
                }
            }
        } catch (Throwable th) {
        }
    }

    private static String e() {
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), "EM_VERSION");
        return !StringUtils.isNull(stringParam) ? stringParam : ThemeUtil.SET_NULL_STR;
    }

    private static void f() {
        if (NetUtil.checkAccessNetWork(2)) {
            List<g> a = h.a(new String[]{"id", "name", NumberInfo.VERSION_KEY, Constant.URLS, "status", "last_load_time", "update_time", "delaystart", "delayend", "count"}, "is_use = 1 AND length(name) > 7  AND url IS NOT NULL AND url <> '' AND status = 0", null, "name desc");
            if (a != null && !a.isEmpty()) {
                int i;
                String config = DuoquUtils.getSdkDoAction().getConfig(6, null);
                if (StringUtils.isNull(config) || "true".equals(config)) {
                    i = 1;
                } else {
                    boolean z = false;
                }
                int i2 = i;
                for (g gVar : a) {
                    a(gVar, false);
                    if (!StringUtils.isNull(gVar.b) && gVar.b.startsWith("PU")) {
                        i2 = 1;
                    }
                }
                if (i2 != 0) {
                    b();
                }
            }
        }
    }

    private static long g() {
        int intParam = SysParamEntityManager.getIntParam(Constant.getContext(), Constant.ONLINE_UPDATE_SDK_PERIOD);
        if (intParam <= 0) {
            intParam = 1;
        }
        return System.currentTimeMillis() - DexUtil.getUpdateCycleByType(8, ((long) intParam) * 86400000);
    }

    private static void h() {
        if (ParseManager.isInitData() && h.a("parseUtilMain") == null) {
            h.a("parseUtilMain", ThemeUtil.SET_NULL_STR, 1);
            h.a("ScenesScanner", ThemeUtil.SET_NULL_STR, 1);
            h.a("ParseSimpleBubbleUtil", ThemeUtil.SET_NULL_STR, 1);
            h.a("ParseUtilBubble", ThemeUtil.SET_NULL_STR, 1);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        try {
            Process.setThreadPriority(10);
            Thread.sleep(20000);
            int intParam = SysParamEntityManager.getIntParam(Constant.getContext(), Constant.ONLINE_UPDATE_SDK);
            KeyManager.initAppKey();
            if ("5Mj22a4wHUAWEICARD".equals(KeyManager.channel) && intParam == 0) {
                a = false;
            }
            if (NetUtil.checkAccessNetWork(2)) {
                a(true, true);
                if (intParam != 0) {
                    E.a(H.UPDATE_PUBINFO);
                    g.b();
                }
            }
            a = false;
        } catch (Throwable th) {
            a = false;
        }
    }
}
