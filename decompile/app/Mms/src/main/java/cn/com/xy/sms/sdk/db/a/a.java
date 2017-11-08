package cn.com.xy.sms.sdk.db.a;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.f;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Hashtable;

/* compiled from: Unknown */
public final class a {
    public static Object a = new Object();
    private static final String b = "bizport.db";
    private static int c = 4;
    private static c d = null;
    private static int e = 1000;
    private static int f = 100;
    private static Hashtable<SQLiteDatabase, Integer> g = new Hashtable();

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
    private static long a(String str, ContentValues contentValues) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized SQLiteDatabase a() {
        SQLiteDatabase sQLiteDatabase = null;
        synchronized (a.class) {
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
                    return sQLiteDatabase;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static SQLiteDatabase a(Context context) {
        synchronized (g) {
            if (g.size() < 10) {
                SQLiteDatabase readableDatabase = b(context.getApplicationContext()).getReadableDatabase();
                if (readableDatabase != null) {
                    Integer num = (Integer) g.get(readableDatabase);
                    g.put(readableDatabase, num != null ? Integer.valueOf(num.intValue() + 1) : Integer.valueOf(1));
                    if (!readableDatabase.isOpen()) {
                        g.remove(readableDatabase);
                        return null;
                    }
                }
            }
            return null;
        }
    }

    public static XyCursor a(SQLiteDatabase sQLiteDatabase, boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        try {
            return new XyCursor(null, sQLiteDatabase.query(false, str, strArr, str2, strArr2, null, null, null, str6), 1);
        } catch (Throwable th) {
            return null;
        }
    }

    private static XyCursor a(String str, String[] strArr) {
        SQLiteDatabase a;
        try {
            a = a();
            try {
                return new XyCursor(a, a.rawQuery(str, strArr), 1);
            } catch (Throwable th) {
                a(a);
                return null;
            }
        } catch (Throwable th2) {
            a = null;
            a(a);
            return null;
        }
    }

    public static XyCursor a(String str, String[] strArr, String str2, String[] strArr2) {
        return a(false, str, strArr, str2, strArr2, null, null, null, null);
    }

    public static XyCursor a(String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        SQLiteDatabase sQLiteDatabase = null;
        try {
            sQLiteDatabase = a();
            return new XyCursor(sQLiteDatabase, sQLiteDatabase.query(str, strArr, str2, strArr2, null, null, str5, str6), 1);
        } catch (Throwable th) {
            a(sQLiteDatabase);
            return null;
        }
    }

    public static XyCursor a(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        SQLiteDatabase sQLiteDatabase = null;
        try {
            sQLiteDatabase = a();
            return new XyCursor(sQLiteDatabase, sQLiteDatabase.query(false, str, strArr, str2, strArr2, null, null, null, str6), 1);
        } catch (Throwable th) {
            a(sQLiteDatabase);
            return null;
        }
    }

    private static void a(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public static synchronized void a(SQLiteDatabase sQLiteDatabase) {
        synchronized (a.class) {
            if (sQLiteDatabase != null) {
                try {
                    synchronized (g) {
                        Integer num = (Integer) g.get(sQLiteDatabase);
                        if (num != null) {
                            num = Integer.valueOf(num.intValue() - 1);
                            if (num.intValue() != 0) {
                                g.put(sQLiteDatabase, num);
                            } else {
                                g.remove(sQLiteDatabase);
                                sQLiteDatabase.close();
                            }
                        } else {
                            new StringBuilder("$$$$$ db close cnt is null ").append(sQLiteDatabase.hashCode());
                        }
                    }
                    if (g.size() == 0) {
                        if (d != null) {
                            d.close();
                            return;
                        }
                    }
                } catch (Throwable th) {
                    new StringBuilder("BizportDBManager close ").append(th.getMessage());
                }
            } else {
                return;
            }
        }
    }

    private static void a(SQLiteDatabase sQLiteDatabase, String str) {
        try {
            sQLiteDatabase.execSQL(str);
        } catch (Throwable th) {
        }
    }

    private static void a(String str) {
        if (!StringUtils.isNull(str)) {
            cn.com.xy.sms.sdk.a.a.e.execute(new b(str));
        }
    }

    public static void a(String str, boolean z) {
        BufferedReader bufferedReader;
        LineNumberReader lineNumberReader;
        BufferedReader bufferedReader2;
        SQLiteDatabase sQLiteDatabase;
        Throwable th;
        Throwable th2;
        LineNumberReader lineNumberReader2 = null;
        if (f.a(str)) {
            SQLiteDatabase sQLiteDatabase2;
            try {
                bufferedReader = new BufferedReader(new FileReader(new File(str)));
                try {
                    lineNumberReader = new LineNumberReader(bufferedReader);
                } catch (Throwable th3) {
                    sQLiteDatabase2 = null;
                    th = th3;
                    lineNumberReader = null;
                    a(str, false, lineNumberReader, bufferedReader, sQLiteDatabase2);
                    throw th;
                }
                try {
                    sQLiteDatabase2 = a();
                    try {
                        sQLiteDatabase2.beginTransaction();
                        while (true) {
                            String readLine = lineNumberReader.readLine();
                            if (readLine == null) {
                                lineNumberReader.close();
                                a(str, false, lineNumberReader, bufferedReader, sQLiteDatabase2);
                                return;
                            } else if (!StringUtils.isNull(readLine)) {
                                sQLiteDatabase2.execSQL(readLine);
                            }
                        }
                    } catch (Throwable th4) {
                        lineNumberReader2 = lineNumberReader;
                        bufferedReader2 = bufferedReader;
                        sQLiteDatabase = sQLiteDatabase2;
                    }
                } catch (Throwable th5) {
                    th2 = th5;
                    sQLiteDatabase2 = null;
                    th = th2;
                    a(str, false, lineNumberReader, bufferedReader, sQLiteDatabase2);
                    throw th;
                }
            } catch (Throwable th32) {
                bufferedReader = null;
                sQLiteDatabase2 = null;
                th2 = th32;
                lineNumberReader = null;
                th = th2;
                a(str, false, lineNumberReader, bufferedReader, sQLiteDatabase2);
                throw th;
            }
        }
    }

    public static void a(String str, boolean z, LineNumberReader lineNumberReader, BufferedReader bufferedReader, SQLiteDatabase sQLiteDatabase) {
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

    private static synchronized c b(Context context) {
        c cVar;
        synchronized (a.class) {
            if (d == null) {
                d = new c(context, b, null, 4);
            }
            cVar = d;
        }
        return cVar;
    }

    public static void b() {
        try {
            a(ParseItemManager.TABLE_NAME, null, null);
            a("tb_phone_pubid", null, null);
        } catch (Throwable th) {
        }
    }

    static /* synthetic */ void b(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.execSQL(ParseItemManager.CREATE_TABLE);
            sQLiteDatabase.execSQL(ParseItemManager.CREATE_INDEX);
            sQLiteDatabase.execSQL(ParseItemManager.CREATE_INDEX_SID);
            sQLiteDatabase.execSQL("create table  if not exists tb_phone_pubid(id INTEGER PRIMARY KEY AUTOINCREMENT,phonenum TEXT,publd TEXT,queryflag TEXT,querytime number(24))");
            sQLiteDatabase.execSQL("create index if not exists indx_phone on tb_phone_pubid (phonenum)");
            try {
                sQLiteDatabase.execSQL(ParseItemManager.ADD_LAST_USE_TIME);
            } catch (Throwable th) {
            }
        } catch (Throwable th2) {
        }
    }

    private static SQLiteDatabase c() {
        return a();
    }

    private static void c(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.execSQL(ParseItemManager.CREATE_TABLE);
            sQLiteDatabase.execSQL(ParseItemManager.CREATE_INDEX);
            sQLiteDatabase.execSQL(ParseItemManager.CREATE_INDEX_SID);
            sQLiteDatabase.execSQL("create table  if not exists tb_phone_pubid(id INTEGER PRIMARY KEY AUTOINCREMENT,phonenum TEXT,publd TEXT,queryflag TEXT,querytime number(24))");
            sQLiteDatabase.execSQL("create index if not exists indx_phone on tb_phone_pubid (phonenum)");
            try {
                sQLiteDatabase.execSQL(ParseItemManager.ADD_LAST_USE_TIME);
            } catch (Throwable th) {
            }
        } catch (Throwable th2) {
        }
    }
}
