package cn.com.xy.sms.sdk.util;

import android.content.Context;
import android.database.sqlite.SQLiteClosable;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.AirManager;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.TrainManager;
import cn.com.xy.sms.sdk.db.a.a;
import cn.com.xy.sms.sdk.db.entity.L;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.h;
import cn.com.xy.sms.sdk.db.entity.z;
import cn.com.xy.sms.sdk.db.i;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.service.e.b;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.util.SdkCallBack;
import cn.com.xy.sms.util.SdkParamUtil;
import com.autonavi.amap.mapcore.VTMCDataCache;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/* compiled from: Unknown */
public final class g {
    public static boolean a = false;
    private static boolean b = false;
    private static final Object c = new Object();
    private static boolean d = false;
    private static final Object e = new Object();
    private static boolean f = false;

    public static void a() {
        try {
            synchronized (e) {
                if (d) {
                    d = false;
                    return;
                }
                d = true;
                k.a();
                SceneconfigUtil.updateData();
                b();
                b.b();
                d = false;
            }
        } catch (Throwable th) {
            d = false;
        }
    }

    public static void a(Context context, SdkCallBack sdkCallBack) {
        synchronized (c) {
            if (b) {
                return;
            }
            b = true;
            Constant.initContext(context);
            Thread thread = new Thread(new j(sdkCallBack));
            thread.setName("xythread-reInitAlgorithm");
            thread.start();
        }
    }

    private static void a(String str) {
        BufferedReader bufferedReader;
        SQLiteDatabase a;
        BufferedReader bufferedReader2;
        SQLiteDatabase sQLiteDatabase;
        SQLiteStatement sQLiteStatement;
        Throwable th;
        try {
            List<File> e = f.e(str, "PU", ".sql");
            if (e != null && e.size() != 0) {
                for (File file : e) {
                    if (a(file) && file != null) {
                        SQLiteStatement compileStatement;
                        try {
                            bufferedReader = new BufferedReader(new FileReader(file));
                            try {
                                a = a.a();
                                try {
                                    compileStatement = a.compileStatement("INSERT INTO tb_regex(scene_id,match_id,regex_text,version_code,regex_type)VALUES(?,?,?,?,?)");
                                    try {
                                        a.beginTransaction();
                                        while (true) {
                                            String readLine = bufferedReader.readLine();
                                            if (readLine == null) {
                                                break;
                                            }
                                            try {
                                                String trim = readLine.substring(0, readLine.indexOf(",")).trim();
                                                readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length());
                                                String trim2 = readLine.substring(0, readLine.indexOf(",")).trim();
                                                readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length());
                                                String trim3 = readLine.substring(0, readLine.indexOf(",")).trim();
                                                readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length());
                                                String trim4 = readLine.substring(0, readLine.indexOf(",")).trim();
                                                readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length()).trim();
                                                compileStatement.bindString(1, trim);
                                                compileStatement.bindString(2, trim2);
                                                compileStatement.bindString(3, readLine);
                                                compileStatement.bindString(4, trim4);
                                                compileStatement.bindString(5, trim3);
                                                compileStatement.executeInsert();
                                            } catch (Throwable th2) {
                                                Throwable th3 = th2;
                                                bufferedReader2 = bufferedReader;
                                                sQLiteDatabase = a;
                                                sQLiteStatement = compileStatement;
                                                th = th3;
                                            }
                                        }
                                        if (a != null) {
                                            try {
                                                if (a.inTransaction()) {
                                                    a.setTransactionSuccessful();
                                                    a.endTransaction();
                                                }
                                            } catch (Throwable th4) {
                                            }
                                            if (compileStatement != null) {
                                                compileStatement.close();
                                            }
                                            a.a(a);
                                        }
                                        try {
                                            bufferedReader.close();
                                        } catch (IOException e2) {
                                        }
                                    } catch (Throwable th22) {
                                        Throwable th32 = th22;
                                        bufferedReader2 = bufferedReader;
                                        sQLiteDatabase = a;
                                        sQLiteStatement = compileStatement;
                                        th = th32;
                                    }
                                } catch (Throwable th5) {
                                    th = th5;
                                    bufferedReader2 = bufferedReader;
                                    sQLiteDatabase = a;
                                    sQLiteStatement = null;
                                    if (sQLiteDatabase == null) {
                                        try {
                                            if (sQLiteDatabase.inTransaction()) {
                                                sQLiteDatabase.setTransactionSuccessful();
                                                sQLiteDatabase.endTransaction();
                                            }
                                        } catch (Throwable th6) {
                                        }
                                        if (sQLiteStatement != null) {
                                            sQLiteStatement.close();
                                        }
                                        a.a(sQLiteDatabase);
                                    }
                                    if (bufferedReader2 != null) {
                                        try {
                                            bufferedReader2.close();
                                        } catch (IOException e3) {
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th7) {
                                th = th7;
                                sQLiteStatement = null;
                                bufferedReader2 = bufferedReader;
                                sQLiteDatabase = null;
                                if (sQLiteDatabase == null) {
                                    if (sQLiteDatabase.inTransaction()) {
                                        sQLiteDatabase.setTransactionSuccessful();
                                        sQLiteDatabase.endTransaction();
                                    }
                                    if (sQLiteStatement != null) {
                                        sQLiteStatement.close();
                                    }
                                    a.a(sQLiteDatabase);
                                }
                                if (bufferedReader2 != null) {
                                    bufferedReader2.close();
                                }
                                throw th;
                            }
                        } catch (Throwable th8) {
                            th = th8;
                            sQLiteStatement = null;
                            sQLiteDatabase = null;
                            bufferedReader2 = null;
                            if (sQLiteDatabase == null) {
                                if (sQLiteDatabase.inTransaction()) {
                                    sQLiteDatabase.setTransactionSuccessful();
                                    sQLiteDatabase.endTransaction();
                                }
                                if (sQLiteStatement != null) {
                                    sQLiteStatement.close();
                                }
                                a.a(sQLiteDatabase);
                            }
                            if (bufferedReader2 != null) {
                                bufferedReader2.close();
                            }
                            throw th;
                        }
                    }
                }
            }
        } catch (Throwable th9) {
        }
    }

    public static void a(String str, int i) {
        if (f.a(Constant.getDRAWBLE_PATH() + str)) {
            if (i == 0) {
                TrainManager.importTrainData(Constant.getContext());
            } else if (i == 1) {
                AirManager.importAirData(Constant.getContext());
            }
        }
    }

    private static void a(StringBuilder stringBuilder, String str, String str2) {
        stringBuilder.append("SELECT '");
        stringBuilder.append(str);
        stringBuilder.append("'name,'");
        stringBuilder.append(str2.trim());
        stringBuilder.append("'version,");
        stringBuilder.append(h.c(str));
        stringBuilder.append(" is_use UNION ALL ");
    }

    private static void a(List<String> list, String str, String str2, String str3, String str4, String str5) {
        if (list != null && list.contains(str4)) {
            f.a(str2, new StringBuilder(String.valueOf(str4)).append("_").toString(), ".jar", null);
            f.b(new StringBuilder(String.valueOf(str4)).append("_").toString(), ".dex", null);
            DexUtil.removeClassLoaderBySubname(str4);
        }
        InputStream b = f.b(new StringBuilder(String.valueOf(str)).append(str3).toString());
        if (b != null) {
            f.c(new StringBuilder(String.valueOf(str2)).append(str3).toString());
            f.a(str2, str3, b);
            f.a(str2, new StringBuilder(String.valueOf(str4)).append(".jar").toString(), new StringBuilder(String.valueOf(str4)).append("_").append(str5).append(".jar").toString());
            if ("parseUtilMain".equals(str4)) {
                DexUtil.init();
            } else if ("OnlineUpdateCycleConfig".equals(str4)) {
                DexUtil.initOnlineUpdateCycleConfig();
            } else {
                if ("ParseVerifyCodeValidTime".equals(str4)) {
                    DexUtil.initParseVerifyCodeValidTime();
                }
            }
        }
    }

    private static void a(List<String> list, StringBuilder stringBuilder) {
        if (stringBuilder.length() > 0) {
            stringBuilder.setLength(stringBuilder.length() - 10);
            list.add(stringBuilder.toString());
        }
        if (list.size() > 0) {
            h.b((List) list);
        }
    }

    private static boolean a(File file) {
        if (file == null) {
            return false;
        }
        try {
            String replace = file.getPath().toString().replace(file.getName(), "");
            String replace2 = file.getName().replace(".sql", "");
            String stringBuilder = new StringBuilder(String.valueOf(replace)).append(replace2).append(".sql").toString();
            File file2 = new File(stringBuilder);
            if (file2.exists() && file2.isFile()) {
                stringBuilder = StringUtils.getFileMD5(stringBuilder);
                if (!StringUtils.isNull(stringBuilder)) {
                    replace = n.a(replace, new StringBuilder(String.valueOf(replace2)).append(".txt").toString());
                    if (!StringUtils.isNull(replace) && stringBuilder.equals(replace)) {
                        if (file2 != null) {
                            return true;
                        }
                        return false;
                    }
                }
            }
            file2 = null;
            if (file2 != null) {
                return true;
            }
        } catch (Throwable th) {
        }
        return false;
    }

    private static boolean a(String str, String str2) {
        if (StringUtils.isNull(str) || str.length() > 15) {
            return true;
        }
        try {
            try {
                return !((Long.valueOf(StringUtils.trim(str2)).longValue() > Long.valueOf(StringUtils.trim(str)).longValue() ? 1 : (Long.valueOf(StringUtils.trim(str2)).longValue() == Long.valueOf(StringUtils.trim(str)).longValue() ? 0 : -1)) <= 0);
            } catch (NumberFormatException e) {
                return false;
            }
        } catch (NumberFormatException e2) {
            return true;
        }
    }

    private static boolean a(HashMap<String, String> hashMap, String str, String str2) {
        return (hashMap == null || hashMap.isEmpty() || !hashMap.containsKey(str)) ? true : a((String) hashMap.get(str), str2);
    }

    private static File b(String str, String str2) {
        String stringBuilder = new StringBuilder(String.valueOf(str)).append(str2).append(".sql").toString();
        File file = new File(stringBuilder);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        stringBuilder = StringUtils.getFileMD5(stringBuilder);
        if (StringUtils.isNull(stringBuilder)) {
            return null;
        }
        String a = n.a(str, new StringBuilder(String.valueOf(str2)).append(".txt").toString());
        return (StringUtils.isNull(a) || !stringBuilder.equals(a)) ? null : file;
    }

    private static List<String> b(String str) {
        if (str == null) {
            return null;
        }
        File file = new File(str);
        if (!file.exists() || !file.isDirectory()) {
            return null;
        }
        String[] list = file.list();
        if (list == null || list.length == 0) {
            return null;
        }
        List<String> arrayList = new ArrayList();
        for (String str2 : list) {
            int lastIndexOf = str2.lastIndexOf("_");
            if (lastIndexOf != -1) {
                arrayList.add(str2.substring(0, lastIndexOf));
            }
        }
        return arrayList;
    }

    public static void b() {
        boolean z = true;
        try {
            String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.SCENE_CENSUS_ONLINE);
            boolean geOnOffByType = DexUtil.geOnOffByType(0);
            boolean geOnOffByType2 = DexUtil.geOnOffByType(1);
            if ("1".equals(stringParam) || geOnOffByType) {
                String currentTimeString = DateUtils.getCurrentTimeString("yyyyMMdd");
                stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), "LastSceneCountActionUpdate");
                if (stringParam != null ? DateUtils.compareDateString(currentTimeString, DateUtils.addDays(stringParam, "yyyyMMdd", 1), "yyyyMMdd") : true) {
                    try {
                        stringParam = w.a(currentTimeString);
                        if (!StringUtils.isNull(stringParam)) {
                            XyCallBack xVar = new x(currentTimeString);
                            if (NetUtil.isEnhance()) {
                                NetUtil.executeLoginBeforeHttpRequest(stringParam, "990005", xVar, NetUtil.STATSERVICE_URL, true);
                            }
                        }
                    } catch (Throwable th) {
                    }
                }
                String currentTimeString2 = DateUtils.getCurrentTimeString("yyyyMMdd");
                stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), "LastMenuActionCountActionUpdate");
                if (stringParam != null) {
                    z = DateUtils.compareDateString(currentTimeString2, DateUtils.addDays(stringParam, "yyyyMMdd", 1), "yyyyMMdd");
                }
                NetUtil.requestNewTokenIfNeed(null);
                if (z) {
                    try {
                        stringParam = L.a(currentTimeString2).toString();
                        if (!StringUtils.isNull(stringParam)) {
                            NetUtil.executeNewServiceHttpRequest(NetUtil.URL_MENU_CLICKED, stringParam, new r(stringParam, NetUtil.getToken(), currentTimeString2), true, false, true, null);
                        }
                    } catch (Throwable th2) {
                    }
                }
            } else if ("2".equals(stringParam) || geOnOffByType2) {
                SceneconfigUtil.postqueryIccidScene();
            }
        } catch (Throwable th3) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void b(File file) {
        BufferedReader bufferedReader;
        SQLiteDatabase a;
        SQLiteClosable sQLiteClosable;
        BufferedReader bufferedReader2;
        Throwable th;
        SQLiteDatabase sQLiteDatabase;
        SQLiteStatement sQLiteStatement;
        Throwable th2;
        SQLiteClosable sQLiteClosable2 = null;
        if (file != null) {
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                try {
                    a = a.a();
                } catch (Throwable th3) {
                    bufferedReader2 = bufferedReader;
                    SQLiteClosable sQLiteClosable3 = sQLiteClosable2;
                    SQLiteClosable sQLiteClosable4 = sQLiteClosable2;
                    th = th3;
                    sQLiteClosable = sQLiteClosable4;
                    if (sQLiteDatabase != null) {
                        try {
                            if (sQLiteDatabase.inTransaction()) {
                                sQLiteDatabase.setTransactionSuccessful();
                                sQLiteDatabase.endTransaction();
                            }
                        } catch (Throwable th4) {
                        }
                        if (sQLiteStatement != null) {
                            sQLiteStatement.close();
                        }
                        a.a(sQLiteDatabase);
                    }
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (IOException e) {
                        }
                    }
                    throw th;
                }
                try {
                    SQLiteStatement compileStatement = a.compileStatement("INSERT INTO tb_regex(scene_id,match_id,regex_text,version_code,regex_type)VALUES(?,?,?,?,?)");
                    a.beginTransaction();
                    while (true) {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            break;
                        }
                        try {
                            String trim = readLine.substring(0, readLine.indexOf(",")).trim();
                            readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length());
                            String trim2 = readLine.substring(0, readLine.indexOf(",")).trim();
                            readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length());
                            String trim3 = readLine.substring(0, readLine.indexOf(",")).trim();
                            readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length());
                            String trim4 = readLine.substring(0, readLine.indexOf(",")).trim();
                            readLine = readLine.substring(readLine.indexOf(",") + 1, readLine.length()).trim();
                            compileStatement.bindString(1, trim);
                            compileStatement.bindString(2, trim2);
                            compileStatement.bindString(3, readLine);
                            compileStatement.bindString(4, trim4);
                            compileStatement.bindString(5, trim3);
                            compileStatement.executeInsert();
                        } catch (Throwable th5) {
                            th2 = th5;
                            bufferedReader2 = bufferedReader;
                            sQLiteDatabase = a;
                            sQLiteStatement = compileStatement;
                            th = th2;
                        }
                    }
                    if (a != null) {
                        try {
                            if (a.inTransaction()) {
                                a.setTransactionSuccessful();
                                a.endTransaction();
                            }
                        } catch (Throwable th6) {
                        }
                        if (compileStatement != null) {
                            compileStatement.close();
                        }
                        a.a(a);
                    }
                    try {
                        bufferedReader.close();
                    } catch (IOException e2) {
                    }
                } catch (Throwable th52) {
                    th2 = th52;
                    bufferedReader2 = bufferedReader;
                    sQLiteDatabase = a;
                    sQLiteClosable = sQLiteClosable2;
                    th = th2;
                    if (sQLiteDatabase != null) {
                        if (sQLiteDatabase.inTransaction()) {
                            sQLiteDatabase.setTransactionSuccessful();
                            sQLiteDatabase.endTransaction();
                        }
                        if (sQLiteStatement != null) {
                            sQLiteStatement.close();
                        }
                        a.a(sQLiteDatabase);
                    }
                    if (bufferedReader2 != null) {
                        bufferedReader2.close();
                    }
                    throw th;
                }
            } catch (Throwable th32) {
                sQLiteDatabase = sQLiteClosable2;
                bufferedReader2 = sQLiteClosable2;
                th2 = th32;
                sQLiteStatement = sQLiteClosable2;
                th = th2;
                if (sQLiteDatabase != null) {
                    if (sQLiteDatabase.inTransaction()) {
                        sQLiteDatabase.setTransactionSuccessful();
                        sQLiteDatabase.endTransaction();
                    }
                    if (sQLiteStatement != null) {
                        sQLiteStatement.close();
                    }
                    a.a(sQLiteDatabase);
                }
                if (bufferedReader2 != null) {
                    bufferedReader2.close();
                }
                throw th;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void c() {
        synchronized (e) {
            if (f) {
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initInerData running or complete", null);
                return;
            }
            f = true;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void d() {
        Object obj = 1;
        try {
            String paramValue;
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm run", null);
            String h = f.h(Constant.ALGORITHM_VERSION_FILE);
            if (!StringUtils.isNull(h)) {
                paramValue = SdkParamUtil.getParamValue(Constant.getContext(), Constant.SMART_ALGORITHM_PVER);
                if (!StringUtils.isNull(paramValue)) {
                    if (h.compareTo(paramValue) <= 0) {
                    }
                }
                SdkParamUtil.setParamValue(Constant.getContext(), Constant.SMART_ALGORITHM_PVER, h);
            }
            h = f.f(Constant.ALGORITHM_VERSION_FILE);
            if (!ThemeUtil.SET_NULL_STR.equals(h) && h.contains("=")) {
                obj = null;
            }
            String str;
            if (obj == null) {
                HashMap l = l();
                if (l == null) {
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm data is null", null);
                    try {
                        h.b("ParseUtilCard", 1);
                        f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.idx", Constant.getPARSE_PATH() + "ScenesScanner.idx");
                        f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.obj", Constant.getPARSE_PATH() + "ScenesScanner.obj");
                        f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm.so", Constant.getPARSE_PATH() + "libxy-algorithm.so");
                        f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm-64.so", Constant.getPARSE_PATH() + "libxy-algorithm-64.so");
                        j();
                        f.d(Constant.getTempPARSE_PATH());
                    } catch (Throwable th) {
                    }
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm end", null);
                    return;
                } else if (l.isEmpty()) {
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm complete", null);
                    try {
                        h.b("ParseUtilCard", 1);
                        f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.idx", Constant.getPARSE_PATH() + "ScenesScanner.idx");
                        f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.obj", Constant.getPARSE_PATH() + "ScenesScanner.obj");
                        f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm.so", Constant.getPARSE_PATH() + "libxy-algorithm.so");
                        f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm-64.so", Constant.getPARSE_PATH() + "libxy-algorithm-64.so");
                        j();
                        f.d(Constant.getTempPARSE_PATH());
                    } catch (Throwable th2) {
                    }
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm end", null);
                    return;
                } else {
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm start", null);
                    SysParamEntityManager.setParam("sms_sdk_init", "0");
                    paramValue = Constant.getTempPARSE_PATH();
                    String parse_path = Constant.getPARSE_PATH();
                    XyUtil.unZip(f.c().open("duoqu_parse.zip"), "parse.zip", paramValue, false, null, false);
                    Set<Entry> entrySet = l.entrySet();
                    List b = b(parse_path);
                    List arrayList = new ArrayList();
                    StringBuilder stringBuilder = new StringBuilder();
                    if (b != null) {
                        ParseItemManager.updateStatue(0, -2);
                    }
                    try {
                        int i = 0;
                        for (Entry entry : entrySet) {
                            h = (String) entry.getKey();
                            str = (String) entry.getValue();
                            if (!(StringUtils.isNull(h) || StringUtils.isNull(str))) {
                                String trim = h.replace(".jar", "").trim();
                                if (b != null) {
                                    if (b.contains(trim)) {
                                        f.a(parse_path, new StringBuilder(String.valueOf(trim)).append("_").toString(), ".jar", null);
                                        f.b(new StringBuilder(String.valueOf(trim)).append("_").toString(), ".dex", null);
                                        DexUtil.removeClassLoaderBySubname(trim);
                                    }
                                }
                                InputStream b2 = f.b(new StringBuilder(String.valueOf(paramValue)).append(h).toString());
                                if (b2 != null) {
                                    f.c(new StringBuilder(String.valueOf(parse_path)).append(h).toString());
                                    f.a(parse_path, h, b2);
                                    f.a(parse_path, new StringBuilder(String.valueOf(trim)).append(".jar").toString(), new StringBuilder(String.valueOf(trim)).append("_").append(str).append(".jar").toString());
                                    if ("parseUtilMain".equals(trim)) {
                                        DexUtil.init();
                                    } else if ("OnlineUpdateCycleConfig".equals(trim)) {
                                        DexUtil.initOnlineUpdateCycleConfig();
                                    } else if ("ParseVerifyCodeValidTime".equals(trim)) {
                                        DexUtil.initParseVerifyCodeValidTime();
                                    }
                                }
                                int i2 = i + 1;
                                if (i2 > VTMCDataCache.MAX_EXPIREDTIME) {
                                    stringBuilder.setLength(stringBuilder.length() - 10);
                                    arrayList.add(stringBuilder.toString());
                                    stringBuilder.setLength(0);
                                    i2 = 0;
                                }
                                stringBuilder.append("SELECT '");
                                stringBuilder.append(trim);
                                stringBuilder.append("'name,'");
                                stringBuilder.append(str.trim());
                                stringBuilder.append("'version,");
                                stringBuilder.append(h.c(trim));
                                stringBuilder.append(" is_use UNION ALL ");
                                i = i2;
                            }
                        }
                        a(paramValue);
                        if (b != null) {
                            ParseItemManager.deleteRepeatData();
                            ParseItemManager.updateStatue(-2, 0);
                        }
                    } catch (Throwable th3) {
                        if (b != null) {
                            ParseItemManager.deleteRepeatData();
                            ParseItemManager.updateStatue(-2, 0);
                        }
                    }
                    if (stringBuilder.length() > 0) {
                        stringBuilder.setLength(stringBuilder.length() - 10);
                        arrayList.add(stringBuilder.toString());
                    }
                    if (arrayList.size() > 0) {
                        h.b(arrayList);
                    }
                    SysParamEntityManager.setParam(Constant.BEFORE_HAND_PARSE_SMS_TIME, String.valueOf(System.currentTimeMillis()));
                    if (l.containsKey("parseUtilMain.jar")) {
                        SysParamEntityManager.deleteOldFile();
                        List arrayList2 = new ArrayList();
                        arrayList2.add("ParseUtilCasual");
                        arrayList2.add("ParseUtilEC");
                        arrayList2.add("ParseUtilFinanceL");
                        arrayList2.add("ParseUtilFinanceM");
                        arrayList2.add("ParseUtilFinanceS");
                        arrayList2.add("ParseUtilLife");
                        arrayList2.add("ParseUtilMove");
                        arrayList2.add("ParseUtilTelecom");
                        arrayList2.add("ParseUtilTravel");
                        arrayList2.add("ParseUtilUnicom");
                        h.a(arrayList2);
                    }
                    str = DuoquUtils.getSdkDoAction().getConfig(6, null);
                    if (StringUtils.isNull(str) || "true".equals(str)) {
                        k.b();
                    }
                    SysParamEntityManager.setParam("sms_sdk_init", "1");
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm complete", null);
                    try {
                        h.b("ParseUtilCard", 1);
                        f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.idx", Constant.getPARSE_PATH() + "ScenesScanner.idx");
                        f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.obj", Constant.getPARSE_PATH() + "ScenesScanner.obj");
                        f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm.so", Constant.getPARSE_PATH() + "libxy-algorithm.so");
                        f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm-64.so", Constant.getPARSE_PATH() + "libxy-algorithm-64.so");
                        j();
                        f.d(Constant.getTempPARSE_PATH());
                    } catch (Throwable th4) {
                    }
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm end", null);
                    return;
                }
            }
            try {
                String f;
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm start", null);
                cn.com.xy.sms.sdk.db.entity.g a = h.a("parseUtilMain");
                if (a != null) {
                    if (!StringUtils.isNull(a.c)) {
                        str = a.c;
                        f = f.f(Constant.ALGORITHM_VERSION_FILE);
                        if (StringUtils.isNull(str) || a(str, f)) {
                            SysParamEntityManager.clearOldData(false);
                            SysParamEntityManager.setParam("sms_sdk_init", "0");
                            XyUtil.unZip(f.c().open("duoqu_parse.zip"), "parse.zip", Constant.getPARSE_PATH(), true, f, true);
                            ParseItemManager.updateParse(Constant.getContext());
                            k.b();
                            SysParamEntityManager.setParam("sms_sdk_init", "1");
                            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm complete", null);
                            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm end", null);
                            h.b("ParseUtilCard", 1);
                            f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.idx", Constant.getPARSE_PATH() + "ScenesScanner.idx");
                            f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.obj", Constant.getPARSE_PATH() + "ScenesScanner.obj");
                            f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm.so", Constant.getPARSE_PATH() + "libxy-algorithm.so");
                            f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm-64.so", Constant.getPARSE_PATH() + "libxy-algorithm-64.so");
                            j();
                            f.d(Constant.getTempPARSE_PATH());
                            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm end", null);
                            return;
                        }
                        DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm end", null);
                        h.b("ParseUtilCard", 1);
                        f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.idx", Constant.getPARSE_PATH() + "ScenesScanner.idx");
                        f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.obj", Constant.getPARSE_PATH() + "ScenesScanner.obj");
                        f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm.so", Constant.getPARSE_PATH() + "libxy-algorithm.so");
                        f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm-64.so", Constant.getPARSE_PATH() + "libxy-algorithm-64.so");
                        j();
                        f.d(Constant.getTempPARSE_PATH());
                        DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm end", null);
                        return;
                    }
                }
                str = null;
                f = f.f(Constant.ALGORITHM_VERSION_FILE);
                if (StringUtils.isNull(str)) {
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm end", null);
                    h.b("ParseUtilCard", 1);
                    f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.idx", Constant.getPARSE_PATH() + "ScenesScanner.idx");
                    f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.obj", Constant.getPARSE_PATH() + "ScenesScanner.obj");
                    f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm.so", Constant.getPARSE_PATH() + "libxy-algorithm.so");
                    f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm-64.so", Constant.getPARSE_PATH() + "libxy-algorithm-64.so");
                    j();
                    f.d(Constant.getTempPARSE_PATH());
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm end", null);
                    return;
                }
                SysParamEntityManager.clearOldData(false);
                SysParamEntityManager.setParam("sms_sdk_init", "0");
                XyUtil.unZip(f.c().open("duoqu_parse.zip"), "parse.zip", Constant.getPARSE_PATH(), true, f, true);
                ParseItemManager.updateParse(Constant.getContext());
                k.b();
                SysParamEntityManager.setParam("sms_sdk_init", "1");
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm complete", null);
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm end", null);
            } catch (Throwable th5) {
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm end", null);
            }
            try {
                h.b("ParseUtilCard", 1);
                f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.idx", Constant.getPARSE_PATH() + "ScenesScanner.idx");
                f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.obj", Constant.getPARSE_PATH() + "ScenesScanner.obj");
                f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm.so", Constant.getPARSE_PATH() + "libxy-algorithm.so");
                f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm-64.so", Constant.getPARSE_PATH() + "libxy-algorithm-64.so");
                j();
                f.d(Constant.getTempPARSE_PATH());
            } catch (Throwable th6) {
            }
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm end", null);
            return;
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm end", null);
        } catch (Throwable th7) {
            h.b("ParseUtilCard", 1);
            f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.idx", Constant.getPARSE_PATH() + "ScenesScanner.idx");
            f.a(Constant.getTempPARSE_PATH() + "ScenesScanner.obj", Constant.getPARSE_PATH() + "ScenesScanner.obj");
            f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm.so", Constant.getPARSE_PATH() + "libxy-algorithm.so");
            f.a(Constant.getTempPARSE_PATH() + "libxy-algorithm-64.so", Constant.getPARSE_PATH() + "libxy-algorithm-64.so");
            j();
            f.d(Constant.getTempPARSE_PATH());
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initAlgorithm end", null);
        }
    }

    public static void e() {
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.PUBLIC_LOGO_VERSION);
        String trim = StringUtils.trim(f.f(Constant.PUBLIC_LOGO_VERSION_FILE));
        DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initPublicLogo localVersion=" + stringParam + " assetsVersion=" + trim, null);
        if (StringUtils.isNull(stringParam) || a(stringParam, trim)) {
            try {
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initPublicLogo start", null);
                XyUtil.unZip(f.c().open("duoqu_publiclogo.zip"), "duoqu_publiclogo.zip", Constant.getPath(Constant.DUOQU_PUBLIC_LOGO_DIR));
                SysParamEntityManager.setParam(Constant.PUBLIC_LOGO_VERSION, trim);
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initPublicLogo complete", null);
            } catch (Throwable th) {
            } finally {
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initPublicLogo end", null);
            }
        } else {
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initPublicLogo complete", null);
        }
    }

    public static void f() {
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.DRAWABLE_VERSION);
        String trim = StringUtils.trim(f.f(Constant.DRAWABLE_VERSION_FILE));
        DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initDrawble localVersion=" + stringParam + " assetsVersion=" + trim, null);
        if (StringUtils.isNull(stringParam) || a(stringParam, trim)) {
            try {
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initDrawble start", null);
                XyUtil.unZip(f.c().open("duoqu_drawable.zip"), "drawable.zip", Constant.getDRAWBLE_PATH());
                File file = new File(Constant.getDRAWBLE_PATH() + File.separator + "init.sql");
                if (!file.exists()) {
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initDrawble end", null);
                } else if (z.a(10) != -1) {
                    if (DBManager.excSql(file, true)) {
                        SysParamEntityManager.setParam(Constant.HAS_IMPORT_DRAWABLE_DATA, "true");
                        SysParamEntityManager.setParam(Constant.DRAWABLE_VERSION, trim);
                    }
                    z.b();
                    z.a(0);
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initDrawble complete", null);
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initDrawble end", null);
                } else {
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initDrawble end", null);
                }
            } catch (Throwable th) {
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initDrawble end", null);
            }
        } else {
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initDrawble complete", null);
        }
    }

    public static void g() {
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.MENU_VERSION);
        String trim = StringUtils.trim(f.f(Constant.MENU_VERSION_FILE));
        DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initMenuData localVersion=" + stringParam + " assetsVersion=" + trim, null);
        if (StringUtils.isNull(stringParam) || a(stringParam, trim)) {
            try {
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initMenuData start", null);
                XyUtil.unZip(f.c().open("duoqu_nqsql.zip"), "duoqu_nqsql.zip", Constant.getINITSQL_PATH());
                i.b();
                SysParamEntityManager.setParam(Constant.MENU_VERSION, trim);
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initMenuData complete", null);
            } catch (Throwable th) {
            } finally {
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initMenuData end", null);
            }
        } else {
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "initMenuData complete", null);
        }
    }

    private static void h() {
        XyUtil.unZip(f.c().open("duoqu_nqsql.zip"), "duoqu_nqsql.zip", Constant.getINITSQL_PATH());
        i.b();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void i() {
        try {
            String str;
            String f;
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm start", null);
            cn.com.xy.sms.sdk.db.entity.g a = h.a("parseUtilMain");
            if (a != null) {
                if (!StringUtils.isNull(a.c)) {
                    str = a.c;
                    f = f.f(Constant.ALGORITHM_VERSION_FILE);
                    if (StringUtils.isNull(str) || a(str, f)) {
                        SysParamEntityManager.clearOldData(false);
                        SysParamEntityManager.setParam("sms_sdk_init", "0");
                        XyUtil.unZip(f.c().open("duoqu_parse.zip"), "parse.zip", Constant.getPARSE_PATH(), true, f, true);
                        ParseItemManager.updateParse(Constant.getContext());
                        k.b();
                        SysParamEntityManager.setParam("sms_sdk_init", "1");
                        DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm complete", null);
                        DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm end", null);
                    }
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm end", null);
                    return;
                }
            }
            str = null;
            f = f.f(Constant.ALGORITHM_VERSION_FILE);
            if (StringUtils.isNull(str)) {
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm end", null);
                return;
            }
            SysParamEntityManager.clearOldData(false);
            SysParamEntityManager.setParam("sms_sdk_init", "0");
            XyUtil.unZip(f.c().open("duoqu_parse.zip"), "parse.zip", Constant.getPARSE_PATH(), true, f, true);
            ParseItemManager.updateParse(Constant.getContext());
            k.b();
            SysParamEntityManager.setParam("sms_sdk_init", "1");
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm complete", null);
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm end", null);
        } catch (Throwable th) {
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "oldInitAlgorithm end", null);
        }
    }

    private static void j() {
        try {
            File[] listFiles = new File(Constant.getTempPARSE_PATH()).listFiles();
            File file = new File(Constant.getPARSE_PATH());
            if (!file.exists()) {
                file.mkdirs();
            }
            for (File file2 : listFiles) {
                String name = file2.getName();
                if (name.endsWith(".obj") || name.endsWith(".idx") || name.endsWith(".so")) {
                    file2.renameTo(new File(file, name));
                }
            }
        } catch (Throwable th) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String k() {
        String str;
        Throwable th;
        ZipFile zipFile = null;
        String str2 = ThemeUtil.SET_NULL_STR;
        ZipFile zipFile2;
        try {
            f.c(new StringBuilder(String.valueOf(Constant.getPath("duoqu_version"))).append("duoqu_parse.zip").toString());
            File a = f.a(Constant.getPath("duoqu_version"), "duoqu_parse.zip", f.c().open("duoqu_parse.zip"));
            if (a != null) {
                zipFile2 = new ZipFile(a);
                String trim;
                try {
                    Enumeration entries = zipFile2.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                        if (!StringUtils.isNull(zipEntry.getName())) {
                            String str3 = new String(zipEntry.getName().getBytes("8859_1"), "GB2312");
                            if (!StringUtils.isNull(str3) && str3.equalsIgnoreCase("version.txt")) {
                                trim = f.a(zipFile2.getInputStream(zipEntry)).trim();
                                f.c(new StringBuilder(String.valueOf(Constant.getPath("duoqu_version"))).append("duoqu_parse.zip").toString());
                                f.a(zipFile2);
                                return trim;
                            }
                        }
                    }
                    zipFile = zipFile2;
                } catch (Throwable th2) {
                    th = th2;
                    f.c(new StringBuilder(String.valueOf(Constant.getPath("duoqu_version"))).append("duoqu_parse.zip").toString());
                    f.a(zipFile2);
                    throw th;
                }
            }
            f.c(new StringBuilder(String.valueOf(Constant.getPath("duoqu_version"))).append("duoqu_parse.zip").toString());
            f.a(zipFile);
        } catch (Throwable th3) {
            Throwable th4 = th3;
            zipFile2 = null;
            th = th4;
            f.c(new StringBuilder(String.valueOf(Constant.getPath("duoqu_version"))).append("duoqu_parse.zip").toString());
            f.a(zipFile2);
            throw th;
        }
        return str2;
    }

    private static HashMap<String, String> l() {
        List<String> g = f.g(Constant.ALGORITHM_VERSION_FILE);
        if (g == null || g.isEmpty()) {
            return null;
        }
        HashMap d = h.d();
        HashMap<String, String> hashMap = new HashMap();
        for (String split : g) {
            String split2;
            String[] split3 = split2.split("=");
            if (split3.length == 2) {
                String str = split3[0];
                String str2 = split3[1];
                if (!(StringUtils.isNull(str) || StringUtils.isNull(str2))) {
                    split2 = str.replace(".jar", "");
                    boolean a = (d == null || d.isEmpty() || !d.containsKey(split2)) ? true : a((String) d.get(split2), str2);
                    if (a) {
                        hashMap.put(str, str2);
                    }
                }
            }
        }
        return hashMap;
    }

    private static boolean m() {
        String f = f.f(Constant.ALGORITHM_VERSION_FILE);
        return ThemeUtil.SET_NULL_STR.equals(f) || !f.contains("=");
    }

    private static void n() {
        cn.com.xy.sms.sdk.a.a.e.execute(new i());
    }

    private static void o() {
        String h = f.h(Constant.ALGORITHM_VERSION_FILE);
        if (!StringUtils.isNull(h)) {
            String paramValue = SdkParamUtil.getParamValue(Constant.getContext(), Constant.SMART_ALGORITHM_PVER);
            if (StringUtils.isNull(paramValue) || h.compareTo(paramValue) > 0) {
                SdkParamUtil.setParamValue(Constant.getContext(), Constant.SMART_ALGORITHM_PVER, h);
            }
        }
    }
}
