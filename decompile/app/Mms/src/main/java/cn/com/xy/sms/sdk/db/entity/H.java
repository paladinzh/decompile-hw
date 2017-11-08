package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkParamUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* compiled from: Unknown */
public final class h {
    private static int a = 0;
    private static int b = 1;
    private static String c = "id";
    private static String d = "name";
    private static String e = "version";
    private static String f = "url";
    private static String g = "status";
    private static String h = "last_load_time";
    private static String i = "update_time";
    private static String j = "delaystart";
    private static String k = "delayend";
    private static String l = "pver";
    private static String m = "count";
    private static String n = "tb_jar_list";
    private static String o = "is_use";
    private static int p = 1;
    private static int q = 0;
    private static String r = " DROP TABLE IF EXISTS tb_jar_list";
    private static String s = "create table  if not exists tb_jar_list (id INTEGER PRIMARY KEY,name TEXT,version TEXT,url TEXT,status INTEGER DEFAULT '0',update_time INTEGER DEFAULT '0',delaystart INTEGER DEFAULT '0',delayend INTEGER DEFAULT '0',count INTEGER DEFAULT '0',last_load_time INTEGER DEFAULT '0' ,is_use INTEGER DEFAULT '0' ,pver TEXT)";
    private static String t = "ALTER TABLE tb_jar_list ADD COLUMN is_use INTEGER DEFAULT '0'";
    private static String u = "ALTER TABLE tb_jar_list ADD COLUMN pver TEXT ";

    public static int a(List<String> list) {
        try {
            if (list.size() == 0) {
                return 0;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String str : list) {
                if (!StringUtils.isNull(str)) {
                    stringBuilder.append("'");
                    stringBuilder.append(str.trim());
                    stringBuilder.append("',");
                }
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.setLength(stringBuilder.length() - 1);
                return DBManager.delete("tb_jar_list", "name IN (" + stringBuilder + ")", null);
            }
            return -1;
        } catch (Throwable th) {
        }
    }

    public static g a(String str) {
        Throwable th;
        XyCursor xyCursor = null;
        XyCursor query;
        try {
            query = DBManager.query("tb_jar_list", new String[]{"id", "name", NumberInfo.VERSION_KEY, Constant.URLS, "status", "last_load_time", "update_time", "delaystart", "delayend", "count"}, "name = ? ", new String[]{str});
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        int columnIndex = query.getColumnIndex("id");
                        int columnIndex2 = query.getColumnIndex("name");
                        int columnIndex3 = query.getColumnIndex(NumberInfo.VERSION_KEY);
                        int columnIndex4 = query.getColumnIndex(Constant.URLS);
                        int columnIndex5 = query.getColumnIndex("status");
                        int columnIndex6 = query.getColumnIndex("last_load_time");
                        int columnIndex7 = query.getColumnIndex("update_time");
                        int columnIndex8 = query.getColumnIndex("delaystart");
                        int columnIndex9 = query.getColumnIndex("delayend");
                        int columnIndex10 = query.getColumnIndex("count");
                        if (query.moveToNext()) {
                            g gVar = new g();
                            query.getLong(columnIndex);
                            gVar.b = query.getString(columnIndex2);
                            gVar.c = query.getString(columnIndex3);
                            gVar.d = query.getString(columnIndex4);
                            gVar.f = query.getInt(columnIndex5);
                            query.getLong(columnIndex6);
                            query.getLong(columnIndex7);
                            gVar.h = query.getLong(columnIndex8);
                            query.getLong(columnIndex9);
                            query.getInt(columnIndex10);
                            XyCursor.closeCursor(query, true);
                            return gVar;
                        }
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = query;
                    th = th3;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(query, true);
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String a() {
        XyCursor xyCursor = null;
        String str = "";
        try {
            xyCursor = DBManager.query("tb_jar_list", new String[]{"pver"}, "STATUS = 1 order by pver desc LIMIT 1 ", null);
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    while (xyCursor.moveToNext()) {
                        str = xyCursor.getString(0);
                    }
                }
            }
            String paramValue = SdkParamUtil.getParamValue(Constant.getContext(), Constant.SMART_ALGORITHM_PVER);
            if (StringUtils.isNull(str)) {
                XyCursor.closeCursor(xyCursor, true);
                return paramValue;
            }
            if (!StringUtils.isNull(paramValue)) {
                if (paramValue.compareTo(str) > 0) {
                    str = paramValue;
                }
            }
            XyCursor.closeCursor(xyCursor, true);
            return str;
        } catch (Throwable th) {
            str = th;
            XyCursor.closeCursor(xyCursor, true);
        }
    }

    public static List<g> a(long j) {
        return a(new String[]{"id", "name", NumberInfo.VERSION_KEY, Constant.URLS, "status", "last_load_time", "update_time", "delaystart", "delayend", "count"}, "(is_use = ? or name = ? ) AND length(name) > 7  AND update_time < ?", new String[]{"1", "parseUtilMain", String.valueOf(j)}, "name desc");
    }

    private static List<g> a(XyCursor xyCursor) {
        if (xyCursor == null || xyCursor.getCount() == 0) {
            return null;
        }
        List<g> arrayList = new ArrayList();
        int columnIndex = xyCursor.getColumnIndex("id");
        int columnIndex2 = xyCursor.getColumnIndex("name");
        int columnIndex3 = xyCursor.getColumnIndex(NumberInfo.VERSION_KEY);
        int columnIndex4 = xyCursor.getColumnIndex(Constant.URLS);
        int columnIndex5 = xyCursor.getColumnIndex("status");
        int columnIndex6 = xyCursor.getColumnIndex("last_load_time");
        int columnIndex7 = xyCursor.getColumnIndex("update_time");
        int columnIndex8 = xyCursor.getColumnIndex("delaystart");
        int columnIndex9 = xyCursor.getColumnIndex("delayend");
        int columnIndex10 = xyCursor.getColumnIndex("count");
        while (xyCursor.moveToNext()) {
            g gVar = new g();
            xyCursor.getLong(columnIndex);
            gVar.b = xyCursor.getString(columnIndex2);
            gVar.c = xyCursor.getString(columnIndex3);
            gVar.d = xyCursor.getString(columnIndex4);
            gVar.f = xyCursor.getInt(columnIndex5);
            xyCursor.getLong(columnIndex6);
            xyCursor.getLong(columnIndex7);
            gVar.h = xyCursor.getLong(columnIndex8);
            xyCursor.getLong(columnIndex9);
            xyCursor.getInt(columnIndex10);
            arrayList.add(gVar);
        }
        return arrayList;
    }

    public static List<g> a(String[] strArr, String str, String[] strArr2, String str2) {
        XyCursor query;
        XyCursor xyCursor;
        Throwable th;
        try {
            List<g> arrayList;
            query = DBManager.query("tb_jar_list", strArr, str, strArr2, null, null, str2, null);
            if (query != null) {
                try {
                    if (query.getCount() != 0) {
                        arrayList = new ArrayList();
                        int columnIndex = query.getColumnIndex("id");
                        int columnIndex2 = query.getColumnIndex("name");
                        int columnIndex3 = query.getColumnIndex(NumberInfo.VERSION_KEY);
                        int columnIndex4 = query.getColumnIndex(Constant.URLS);
                        int columnIndex5 = query.getColumnIndex("status");
                        int columnIndex6 = query.getColumnIndex("last_load_time");
                        int columnIndex7 = query.getColumnIndex("update_time");
                        int columnIndex8 = query.getColumnIndex("delaystart");
                        int columnIndex9 = query.getColumnIndex("delayend");
                        int columnIndex10 = query.getColumnIndex("count");
                        while (query.moveToNext()) {
                            g gVar = new g();
                            query.getLong(columnIndex);
                            gVar.b = query.getString(columnIndex2);
                            gVar.c = query.getString(columnIndex3);
                            gVar.d = query.getString(columnIndex4);
                            gVar.f = query.getInt(columnIndex5);
                            query.getLong(columnIndex6);
                            query.getLong(columnIndex7);
                            gVar.h = query.getLong(columnIndex8);
                            query.getLong(columnIndex9);
                            query.getInt(columnIndex10);
                            arrayList.add(gVar);
                        }
                        XyCursor.closeCursor(query, true);
                        return arrayList;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    XyCursor.closeCursor(query, true);
                    throw th;
                }
            }
            arrayList = null;
            XyCursor.closeCursor(query, true);
            return arrayList;
        } catch (Throwable th3) {
            th = th3;
            query = null;
            XyCursor.closeCursor(query, true);
            throw th;
        }
    }

    public static void a(String str, int i) {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();
            contentValues.put("last_load_time", new StringBuilder(String.valueOf(currentTimeMillis)).toString());
            contentValues.put("status", new StringBuilder("1").toString());
            DBManager.update("tb_jar_list", contentValues, "name = ? ", new String[]{str});
            SdkParamUtil.setParamValue(Constant.getContext(), Constant.SMART_DATA_UPDATE_TIME, new StringBuilder(String.valueOf(currentTimeMillis)).toString());
        } catch (Throwable th) {
        }
    }

    private static void a(String str, long j, long j2) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("update_time", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
            contentValues.put("delaystart", new StringBuilder(String.valueOf(j)).toString());
            contentValues.put("delayend", new StringBuilder(String.valueOf(j2)).toString());
            DBManager.update("tb_jar_list", contentValues, "name = ? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void a(String str, String str2, int i) {
        XyCursor xyCursor = null;
        Object obj = 1;
        synchronized (h.class) {
            try {
                long update;
                xyCursor = DBManager.query("tb_jar_list", new String[]{Constant.URLS, NumberInfo.VERSION_KEY}, "name = ? ", new String[]{str});
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", str);
                if (i == 1) {
                    contentValues.put("is_use", Integer.valueOf(i));
                }
                if (xyCursor != null) {
                    if (xyCursor.getCount() > 0) {
                        if (!(StringUtils.isNull(str2) || ThemeUtil.SET_NULL_STR.equalsIgnoreCase(str2))) {
                            contentValues.put(NumberInfo.VERSION_KEY, str2);
                        }
                        update = (long) DBManager.update("tb_jar_list", contentValues, "name = ? ", new String[]{str});
                        if (update > 0) {
                            obj = null;
                        }
                        if (obj == null && str.startsWith("PU")) {
                            if (str.replaceFirst("PU", "").length() < 8) {
                            }
                        }
                        XyCursor.closeCursor(xyCursor, true);
                    }
                }
                contentValues.put(NumberInfo.VERSION_KEY, str2);
                update = DBManager.insert("tb_jar_list", contentValues);
                if (update > 0) {
                    obj = null;
                }
                if (str.replaceFirst("PU", "").length() < 8) {
                }
                XyCursor.closeCursor(xyCursor, true);
            } catch (Throwable th) {
                Throwable th2 = th;
                XyCursor xyCursor2 = xyCursor;
                Throwable th3 = th2;
                XyCursor.closeCursor(xyCursor2, true);
                throw th3;
            }
        }
    }

    public static void a(String str, String str2, String str3, long j, int i, long j2, long j3, String str4) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(NumberInfo.VERSION_KEY, str2);
            contentValues.put(Constant.URLS, str3);
            contentValues.put("status", Integer.valueOf(0));
            contentValues.put("update_time", new StringBuilder(String.valueOf(j)).toString());
            contentValues.put("delaystart", new StringBuilder(String.valueOf(j2)).toString());
            contentValues.put("delayend", new StringBuilder(String.valueOf(j3)).toString());
            contentValues.put("pver", str4);
            DBManager.update("tb_jar_list", contentValues, "name = ? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    public static List<g> b() {
        return a(Long.MAX_VALUE);
    }

    public static void b(String str) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("update_time", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
            DBManager.update("tb_jar_list", contentValues, "name = ? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    public static void b(String str, int i) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("is_use", Integer.valueOf(1));
            DBManager.update("tb_jar_list", contentValues, "name = ? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    public static boolean b(List<String> list) {
        SQLiteDatabase sQLiteDatabase;
        Throwable th;
        SQLiteDatabase sQLiteDatabase2 = null;
        try {
            sQLiteDatabase = DBManager.getSQLiteDatabase();
            try {
                sQLiteDatabase.beginTransaction();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("INSERT INTO tb_jar_list(name,version,is_use) ");
                stringBuilder.append("SELECT tempTb.name,tempTb.version,tempTb.is_use FROM (");
                stringBuilder.append("SELECT tbA.name,ifnull((CASE WHEN ('-1'=tbA.version OR ''=tbA.version) ");
                stringBuilder.append("THEN tbB.version ELSE tbA.version END),'-1')version,");
                stringBuilder.append("ifnull((CASE WHEN 1=tbA.is_use THEN tbA.is_use ELSE tbB.is_use END),0)is_use FROM (");
                stringBuilder.append("{SQL})tbA LEFT JOIN tb_jar_list tbB ON tbA.name=tbB.name)");
                stringBuilder.append("tempTb LEFT JOIN tb_jar_list ON tb_jar_list.name = tempTb.name");
                String stringBuilder2 = stringBuilder.toString();
                for (String replace : list) {
                    sQLiteDatabase.execSQL(stringBuilder2.replace("{SQL}", replace));
                }
                if (sQLiteDatabase != null) {
                    try {
                        sQLiteDatabase.execSQL("DELETE FROM tb_jar_list WHERE id NOT IN (SELECT MAX(id) FROM tb_jar_list GROUP BY name)");
                    } catch (Throwable th2) {
                    }
                    try {
                        if (sQLiteDatabase.inTransaction()) {
                            sQLiteDatabase.setTransactionSuccessful();
                            sQLiteDatabase.endTransaction();
                        }
                    } catch (Throwable th3) {
                    }
                    DBManager.close(sQLiteDatabase);
                }
                return true;
            } catch (Throwable th4) {
                th = th4;
                if (sQLiteDatabase != null) {
                    try {
                        sQLiteDatabase.execSQL("DELETE FROM tb_jar_list WHERE id NOT IN (SELECT MAX(id) FROM tb_jar_list GROUP BY name)");
                    } catch (Throwable th5) {
                    }
                    try {
                        if (sQLiteDatabase.inTransaction()) {
                            sQLiteDatabase.setTransactionSuccessful();
                            sQLiteDatabase.endTransaction();
                        }
                    } catch (Throwable th6) {
                    }
                    DBManager.close(sQLiteDatabase);
                }
                throw th;
            }
        } catch (Throwable th7) {
            Throwable th8 = th7;
            sQLiteDatabase = null;
            th = th8;
            if (sQLiteDatabase != null) {
                sQLiteDatabase.execSQL("DELETE FROM tb_jar_list WHERE id NOT IN (SELECT MAX(id) FROM tb_jar_list GROUP BY name)");
                if (sQLiteDatabase.inTransaction()) {
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                }
                DBManager.close(sQLiteDatabase);
            }
            throw th;
        }
    }

    public static int c(String str) {
        return ("parseUtilMain".equals(str) || "ParseHelper".equals(str) || "ScenesScanner".equals(str)) ? 1 : 0;
    }

    public static List<g> c() {
        return a(new String[]{"id", "name", NumberInfo.VERSION_KEY, Constant.URLS, "status", "last_load_time", "update_time", "delaystart", "delayend", "count"}, "is_use = 1 AND length(name) > 7  AND url IS NOT NULL AND url <> '' AND status = 0", null, "name desc");
    }

    private static void c(String str, int i) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("count", Integer.valueOf(i + 1));
            DBManager.update("tb_jar_list", contentValues, "name = ? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    private static int d(String str) {
        try {
            if (StringUtils.isNull(str)) {
                return 0;
            }
            return DBManager.delete("tb_jar_list", "name = ?", new String[]{str});
        } catch (Throwable th) {
            return -1;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static HashMap<String, String> d() {
        XyCursor xyCursor = null;
        HashMap<String, String> hashMap = new HashMap();
        try {
            xyCursor = DBManager.query("tb_jar_list", new String[]{"name", NumberInfo.VERSION_KEY}, null, null);
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    int columnIndex = xyCursor.getColumnIndex("name");
                    int columnIndex2 = xyCursor.getColumnIndex(NumberInfo.VERSION_KEY);
                    while (xyCursor.moveToNext()) {
                        hashMap.put(xyCursor.getString(columnIndex), xyCursor.getString(columnIndex2));
                    }
                }
            }
            XyCursor.closeCursor(xyCursor, true);
        } catch (Throwable th) {
            Throwable th2 = th;
            XyCursor xyCursor2 = xyCursor;
            Throwable th3 = th2;
            XyCursor.closeCursor(xyCursor2, true);
            throw th3;
        }
        return hashMap;
    }

    public static int e() {
        try {
            return DBManager.delete("tb_jar_list", null, null);
        } catch (Throwable th) {
            return -1;
        }
    }

    public static int f() {
        List arrayList = new ArrayList();
        arrayList.add("ParseUtilCasual");
        arrayList.add("ParseUtilEC");
        arrayList.add("ParseUtilFinanceL");
        arrayList.add("ParseUtilFinanceM");
        arrayList.add("ParseUtilFinanceS");
        arrayList.add("ParseUtilLife");
        arrayList.add("ParseUtilMove");
        arrayList.add("ParseUtilTelecom");
        arrayList.add("ParseUtilTravel");
        arrayList.add("ParseUtilUnicom");
        return a(arrayList);
    }

    public static boolean g() {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            query = DBManager.query("tb_jar_list", new String[]{"name"}, "(is_use = ? or name = ? ) AND length(name) > 7 AND status = ? AND url IS NOT NULL AND url <> '' ", new String[]{"1", "parseUtilMain", "0"}, null, null, null, " 1 ");
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        XyCursor.closeCursor(query, true);
                        return true;
                    }
                } catch (Throwable th2) {
                    xyCursor = query;
                    th = th2;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(query, true);
        } catch (Throwable th3) {
            th = th3;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return false;
    }
}
