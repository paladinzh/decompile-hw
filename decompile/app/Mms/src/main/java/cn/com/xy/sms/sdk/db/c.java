package cn.com.xy.sms.sdk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.f;
import java.io.BufferedReader;
import java.io.File;
import java.io.LineNumberReader;
import java.util.Hashtable;

/* compiled from: Unknown */
public final class c {
    private static Object a = new Object();
    private static String b = "duoqu_contacts.db";
    private static final int c = 6;
    private static d d = null;
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

    public static XyCursor a(String str, String[] strArr, String str2, String[] strArr2) {
        return a(false, str, strArr, str2, strArr2, null, null, null, null);
    }

    public static XyCursor a(String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        XyCursor xyCursor;
        try {
            SQLiteDatabase a = a();
            if (d(a)) {
                return null;
            }
            xyCursor = new XyCursor(a, a.query(str, strArr, str2, strArr2, null, null, str5, null), 2);
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
            if (d(a)) {
                return null;
            }
            xyCursor = new XyCursor(a, a.query(false, str, strArr, str2, strArr2, null, null, str5, str6), 2);
            return xyCursor;
        } catch (Throwable th) {
            a(null);
            xyCursor = null;
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
            } catch (Throwable th) {
            }
        }
    }

    static /* synthetic */ void a(SQLiteDatabase sQLiteDatabase, String str) {
        try {
            sQLiteDatabase.execSQL(str);
        } catch (Throwable th) {
        }
    }

    private static void a(File file, boolean z, LineNumberReader lineNumberReader, BufferedReader bufferedReader, SQLiteDatabase sQLiteDatabase) {
        if (z) {
            try {
                f.a(file);
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

    private static void a(String str, boolean z, LineNumberReader lineNumberReader, BufferedReader bufferedReader, SQLiteDatabase sQLiteDatabase) {
        File file = null;
        if (z) {
            try {
                file = new File(str);
            } catch (Throwable th) {
            }
        }
        if (z) {
            try {
                f.a(file);
            } catch (Throwable th2) {
            }
        }
        if (lineNumberReader != null) {
            try {
                lineNumberReader.close();
            } catch (Throwable th3) {
            }
        }
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (Throwable th4) {
            }
        }
        if (sQLiteDatabase != null) {
            try {
                if (sQLiteDatabase.inTransaction()) {
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                }
                a(sQLiteDatabase);
            } catch (Throwable th5) {
            }
        }
    }

    private static SQLiteDatabase b() {
        return a();
    }

    private static synchronized d b(Context context) {
        d dVar;
        synchronized (c.class) {
            if (d == null) {
                d = new d(context, "duoqu_contacts.db", null, 6);
            }
            dVar = d;
        }
        return dVar;
    }

    public static void b(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS contacts(_id INTEGER PRIMARY KEY AUTOINCREMENT, phone TEXT, name TEXT, data TEXT, update_time TEXT)");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS tb_number_info (id INTEGER PRIMARY KEY, num TEXT UNIQUE, result TEXT, version TEXT, t9_flag INTEGER DEFAULT 0, last_query_time INTEGER DEFAULT 0)");
        } catch (Throwable th) {
        }
    }

    private static void b(SQLiteDatabase sQLiteDatabase, String str) {
        try {
            sQLiteDatabase.execSQL(str);
        } catch (Throwable th) {
        }
    }

    public static void c(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.execSQL("CREATE INDEX IDX_tb_number_info_last_query_time ON tb_number_info(last_query_time);");
        } catch (Throwable th) {
        }
    }

    private static boolean d(SQLiteDatabase sQLiteDatabase) {
        if (sQLiteDatabase == null || !sQLiteDatabase.inTransaction()) {
            return false;
        }
        a(sQLiteDatabase);
        Thread.currentThread().getName();
        return true;
    }
}
