package cn.com.xy.sms.sdk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.f;
import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.util.Hashtable;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class e {
    public static Object a = new Object();
    private static String b = "conversation.db";
    private static int c = 2;
    private static g d = null;
    private static int e = 1000;
    private static int f = 100;
    private static Hashtable<SQLiteDatabase, Integer> g = new Hashtable();
    private static String h = "create table  if not exists tb_conversation (id INTEGER PRIMARY KEY, msgId TEXT,phone TEXT,type INTEGER,value TEXT, updateTime INTEGER DEFAULT '0',extend TEXT)";
    private static String i = "create table  if not exists tb_key (id INTEGER PRIMARY KEY,cId TEXT, key TEXT, value TEXT,extend TEXT)";
    private static String j = "create index if not exists indx_key_value on tb_key (key,value)";
    private static String k = "create table  if not exists tb_base_value (id INTEGER PRIMARY KEY, msgId TEXT,phone TEXT,value TEXT,updateTime INTEGER DEFAULT '0',flag INTEGER DEFAULT '0',extend TEXT)";
    private static String l = "create table  if not exists t_log (log_id TEXT,date_time DATETIME DEFAULT CURRENT_TIMESTAMP,cls_name TEXT,method_name TEXT,log_name TEXT,log_json TEXT)";
    private static String m = "create table  if not exists t_log_exception (except_id  TEXT,date_time DATETIME DEFAULT CURRENT_TIMESTAMP,cls_name TEXT,method_name TEXT,log_name TEXT,log_exception TEXT)";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int a(String str, ContentValues contentValues, String str2, String[] strArr) {
        int i;
        synchronized (a) {
            int update;
            SQLiteDatabase sQLiteDatabase = null;
            try {
                sQLiteDatabase = a();
                update = sQLiteDatabase.update(str, contentValues, str2, strArr);
                a(sQLiteDatabase);
            } catch (Throwable th) {
                Throwable th2 = th;
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                Throwable th3 = th2;
                a(sQLiteDatabase2);
                throw th3;
            }
            i = update;
        }
        return i;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int a(String str, String str2, String[] strArr) {
        int i;
        synchronized (a) {
            int delete;
            SQLiteDatabase sQLiteDatabase = null;
            try {
                sQLiteDatabase = a();
                delete = sQLiteDatabase.delete(str, str2, strArr);
                a(sQLiteDatabase);
            } catch (Throwable th) {
                Throwable th2 = th;
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                Throwable th3 = th2;
                a(sQLiteDatabase2);
                throw th3;
            }
            i = delete;
        }
        return i;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long a(String str, ContentValues contentValues) {
        long j;
        SQLiteDatabase sQLiteDatabase = null;
        synchronized (a) {
            long insert;
            try {
                sQLiteDatabase = a();
                insert = sQLiteDatabase.insert(str, null, contentValues);
                a(sQLiteDatabase);
            } catch (Throwable th) {
                Throwable th2 = th;
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                Throwable th3 = th2;
                a(sQLiteDatabase2);
                throw th3;
            }
            j = insert;
        }
        return j;
    }

    public static SQLiteDatabase a() {
        SQLiteDatabase sQLiteDatabase = null;
        long currentTimeMillis = System.currentTimeMillis();
        while (sQLiteDatabase == null) {
            sQLiteDatabase = a(Constant.getContext());
            if (sQLiteDatabase == null) {
                if ((System.currentTimeMillis() - currentTimeMillis >= ((long) e) ? 1 : null) != null) {
                    break;
                }
                try {
                    Thread.sleep((long) f);
                } catch (InterruptedException e) {
                }
            } else {
                sQLiteDatabase.inTransaction();
                return sQLiteDatabase;
            }
        }
        return sQLiteDatabase;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static SQLiteDatabase a(Context context) {
        synchronized (g) {
            if (g.size() >= 10) {
                return null;
            } else if (context != null) {
                SQLiteDatabase readableDatabase = b(context).getReadableDatabase();
                if (readableDatabase != null) {
                    Integer num = (Integer) g.get(readableDatabase);
                    g.put(readableDatabase, num != null ? Integer.valueOf(num.intValue() + 1) : Integer.valueOf(1));
                    if (!readableDatabase.isOpen()) {
                        g.remove(readableDatabase);
                        return null;
                    }
                }
            } else {
                return null;
            }
        }
    }

    public static XyCursor a(String str, String[] strArr) {
        XyCursor xyCursor = null;
        SQLiteDatabase a;
        try {
            a = a();
            try {
                if (c(a)) {
                    return null;
                }
                xyCursor = new XyCursor(a, a.rawQuery(str, strArr), 3);
                return xyCursor;
            } catch (Throwable th) {
                a(a);
                return xyCursor;
            }
        } catch (Throwable th2) {
            a = null;
            a(a);
            return xyCursor;
        }
    }

    public static XyCursor a(String str, String[] strArr, String str2, String[] strArr2) {
        return a(false, str, strArr, str2, strArr2, null, null, null, null);
    }

    public static XyCursor a(String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        XyCursor xyCursor;
        try {
            SQLiteDatabase a = a();
            if (c(a)) {
                return null;
            }
            xyCursor = new XyCursor(a, a.query(str, strArr, str2, strArr2, str3, str4, str5, str6), 3);
            return xyCursor;
        } catch (Throwable th) {
            a(null);
            xyCursor = null;
        }
    }

    public static XyCursor a(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        XyCursor xyCursor;
        try {
            SQLiteDatabase a = a();
            if (a == null) {
                return null;
            }
            if (c(a)) {
                return null;
            }
            xyCursor = new XyCursor(a, a.query(z, str, strArr, str2, strArr2, str3, str4, str5, str6), 3);
            return xyCursor;
        } catch (Throwable th) {
            a(null);
            xyCursor = null;
        }
    }

    private static void a(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public static void a(SQLiteDatabase sQLiteDatabase) {
        if (sQLiteDatabase != null) {
            try {
                synchronized (g) {
                    if (sQLiteDatabase.isOpen()) {
                        Integer num = (Integer) g.get(sQLiteDatabase);
                        if (num != null) {
                            num = Integer.valueOf(num.intValue() - 1);
                            if (num.intValue() != 0) {
                                g.put(sQLiteDatabase, num);
                            } else {
                                g.remove(sQLiteDatabase);
                                sQLiteDatabase.close();
                            }
                        }
                    } else {
                        g.remove(sQLiteDatabase);
                    }
                }
                if (g.size() == 0) {
                }
            } catch (Throwable th) {
                new StringBuilder("DBManager close error: ").append(th.getMessage());
            }
        }
    }

    private static void a(SQLiteDatabase sQLiteDatabase, String str) {
        try {
            sQLiteDatabase.execSQL(str);
        } catch (Throwable th) {
        }
    }

    public static void a(String str) {
        if (!StringUtils.isNull(str)) {
            a.e.execute(new f(str));
        }
    }

    private static void a(String str, boolean z, LineNumberReader lineNumberReader, BufferedReader bufferedReader, SQLiteDatabase sQLiteDatabase) {
        if (z) {
            try {
                f.d(str);
            } catch (Throwable th) {
            }
        }
        if (lineNumberReader != null) {
            try {
                lineNumberReader.close();
            } catch (Throwable th2) {
            }
        }
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (Throwable th3) {
            }
        }
        if (sQLiteDatabase != null) {
            try {
                if (sQLiteDatabase.inTransaction()) {
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                }
                a(sQLiteDatabase);
            } catch (Throwable th4) {
            }
        }
    }

    private static long b(String str, ContentValues contentValues, String str2, String[] strArr) {
        try {
            long a = (long) a(str, contentValues, str2, strArr);
            return ((a > 1 ? 1 : (a == 1 ? 0 : -1)) >= 0 ? 1 : null) == null ? a(str, contentValues) : -a;
        } catch (Throwable th) {
            return 0;
        }
    }

    private static SQLiteDatabase b() {
        return a();
    }

    private static synchronized g b(Context context) {
        g gVar;
        synchronized (e.class) {
            if (d == null) {
                d = new g(context, "conversation.db", null, 2);
            }
            gVar = d;
        }
        return gVar;
    }

    public static void b(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.execSQL("create table  if not exists tb_base_value (id INTEGER PRIMARY KEY, msgId TEXT,phone TEXT,value TEXT,updateTime INTEGER DEFAULT '0',flag INTEGER DEFAULT '0',extend TEXT)");
            sQLiteDatabase.execSQL("create table  if not exists tb_conversation (id INTEGER PRIMARY KEY, msgId TEXT,phone TEXT,type INTEGER,value TEXT, updateTime INTEGER DEFAULT '0',extend TEXT)");
            sQLiteDatabase.execSQL("create table  if not exists tb_key (id INTEGER PRIMARY KEY,cId TEXT, key TEXT, value TEXT,extend TEXT)");
            sQLiteDatabase.execSQL("create index if not exists indx_key_value on tb_key (key,value)");
            sQLiteDatabase.execSQL("create table  if not exists t_log (log_id TEXT,date_time DATETIME DEFAULT CURRENT_TIMESTAMP,cls_name TEXT,method_name TEXT,log_name TEXT,log_json TEXT)");
            sQLiteDatabase.execSQL("create table  if not exists t_log_exception (except_id  TEXT,date_time DATETIME DEFAULT CURRENT_TIMESTAMP,cls_name TEXT,method_name TEXT,log_name TEXT,log_exception TEXT)");
        } catch (Throwable th) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void b(String str) {
        if (!StringUtils.isNull(str)) {
            try {
                synchronized (a) {
                    SQLiteDatabase sQLiteDatabase = null;
                    try {
                        sQLiteDatabase = a();
                        sQLiteDatabase.execSQL(str);
                        a(sQLiteDatabase);
                    } catch (Throwable th) {
                        Throwable th2 = th;
                        SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                        Throwable th3 = th2;
                        a(sQLiteDatabase2);
                        throw th3;
                    }
                }
            } catch (Throwable th4) {
            }
        }
    }

    public static JSONArray c(String str) {
        Throwable th;
        XyCursor xyCursor = null;
        XyCursor a;
        try {
            a = a(str, null);
            try {
                JSONArray jSONArray = new JSONArray();
                if (a == null) {
                    XyCursor.closeCursor(a, true);
                    return null;
                }
                while (a.moveToNext()) {
                    String[] columnNames = a.getColumnNames();
                    JSONObject jSONObject = new JSONObject();
                    for (int i = 0; i < columnNames.length; i++) {
                        jSONObject.put(columnNames[i], a.getString(i));
                    }
                    jSONArray.put(jSONObject);
                }
                XyCursor.closeCursor(a, true);
                return jSONArray;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                xyCursor = a;
                th = th3;
            }
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    private static boolean c(SQLiteDatabase sQLiteDatabase) {
        if (sQLiteDatabase == null || !sQLiteDatabase.inTransaction()) {
            return false;
        }
        a(sQLiteDatabase);
        Thread.currentThread().getName();
        return true;
    }
}
