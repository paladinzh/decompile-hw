package com.huawei.openalliance.ad.utils.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.huawei.openalliance.ad.utils.b;
import com.huawei.openalliance.ad.utils.b.d;
import com.huawei.openalliance.ad.utils.db.bean.MaterialRecord;
import com.huawei.openalliance.ad.utils.j;
import com.huawei.openalliance.ad.utils.k;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: Unknown */
public class a extends SQLiteOpenHelper {
    private static final String a = a.class.getSimpleName();
    private static AtomicInteger b = new AtomicInteger();
    private static a e;
    private Context c;
    private SQLiteDatabase d = null;

    protected a(Context context) {
        super(context, "hiad.db", null, 11);
        this.c = context;
    }

    public static synchronized a a(Context context) {
        a aVar;
        synchronized (a.class) {
            Context applicationContext = context.getApplicationContext();
            if (e == null) {
                e = new a(applicationContext);
            }
            int incrementAndGet = b.incrementAndGet();
            d.b(a, "getInstance, count is:", BuildConfig.FLAVOR + incrementAndGet);
            aVar = e;
        }
        return aVar;
    }

    private void a() {
        Throwable th;
        Cursor cursor;
        Throwable th2;
        Cursor cursor2 = null;
        ArrayList arrayList = new ArrayList(4);
        try {
            cursor2 = this.d.rawQuery("select htmlStr from " + MaterialRecord.class.getSimpleName() + " where adType = " + 1, null);
            while (cursor2.moveToNext()) {
                try {
                    String a = b.a(cursor2.getString(cursor2.getColumnIndex("htmlStr")));
                    if (a != null) {
                        arrayList.add(a);
                    }
                } catch (Exception e) {
                    d.c(a, "delete invalid pics fail");
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                } catch (Throwable th3) {
                    th = th3;
                    cursor = cursor2;
                    th2 = th;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th2;
                }
            }
            try {
                k.c.execute(new b(this, arrayList));
            } catch (Exception e2) {
                d.c(a, "excute thread fail");
            }
            if (cursor2 != null) {
                cursor2.close();
            }
        } catch (Exception e3) {
            d.c(a, "delete invalid pics fail");
            if (cursor2 != null) {
                cursor2.close();
            }
        } catch (Throwable th32) {
            th = th32;
            cursor = cursor2;
            th2 = th;
            if (cursor != null) {
                cursor.close();
            }
            throw th2;
        }
    }

    private void a(int i, boolean z) {
        try {
            c cVar = new c(this, this.c);
            this.d.beginTransaction();
            if (z) {
                cVar.a();
            } else {
                cVar.b();
            }
            this.d.setTransactionSuccessful();
        } catch (Exception e) {
            d.c(a, "initTables error");
        } finally {
            this.d.endTransaction();
        }
    }

    private boolean g(String str) {
        return (str == null || BuildConfig.FLAVOR.equals(str) || str.length() > 30) ? false : true;
    }

    public synchronized int a(String str, ContentValues contentValues, String str2, String[] strArr) {
        int i = 0;
        synchronized (this) {
            d.b(a, "update()");
            try {
                i = getWritableDatabase().update(str, contentValues, str2, strArr);
            } catch (Exception e) {
                d.c(a, "update ex");
            }
        }
        return i;
    }

    public synchronized int a(String str, String str2, String[] strArr) {
        int i = 0;
        synchronized (this) {
            d.b(a, "delete()");
            try {
                i = getWritableDatabase().delete(str, str2, strArr);
            } catch (Exception e) {
                d.c(a, "delete ex");
            }
        }
        return i;
    }

    public synchronized long a(String str, ContentValues contentValues) throws Exception {
        long j;
        d.b(a, "insert()");
        j = -1;
        try {
            j = getWritableDatabase().insertOrThrow(str, null, contentValues);
        } catch (Exception e) {
            d.c(a, "insert ex");
        }
        return j;
    }

    public synchronized Cursor a(String str, String[] strArr, String str2, String[] strArr2, String str3) {
        return getReadableDatabase().query(str, strArr, str2, strArr2, null, null, str3);
    }

    public synchronized Cursor a(String str, String[] strArr, String str2, String[] strArr2, String str3, String str4) {
        return getReadableDatabase().query(str, strArr, str2, strArr2, null, null, str3, str4);
    }

    public void a(String str) throws Exception {
        if (g(str)) {
            try {
                this.d.execSQL(" DROP TABLE _temp_" + str);
                return;
            } catch (Exception e) {
                d.c(a, "delete temp table fail");
                throw e;
            }
        }
        throw new Exception();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(String str, String str2, List<String> list) {
        if (list != null) {
            if (!list.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder("delete from ");
                stringBuilder.append(str);
                stringBuilder.append(" where ");
                stringBuilder.append(str2);
                stringBuilder.append(" in (");
                stringBuilder.append(j.a((List) list, ",", "'"));
                stringBuilder.append(")");
                try {
                    getWritableDatabase().execSQL(stringBuilder.toString());
                } catch (Exception e) {
                    d.c(a, "delete record fail");
                }
            }
        }
    }

    public synchronized void a(String str, List<String> list, long j) {
        StringBuilder stringBuilder = new StringBuilder("update ");
        stringBuilder.append(str);
        stringBuilder.append(" set lockTime = ");
        stringBuilder.append(j);
        stringBuilder.append(" where _id in ( ");
        stringBuilder.append(j.a(list, ","));
        stringBuilder.append(")");
        try {
            getWritableDatabase().execSQL(stringBuilder.toString());
        } catch (Exception e) {
            d.c(a, "update record fail");
        }
    }

    public synchronized Cursor b(String str, String str2, List<String> list) {
        Cursor cursor = null;
        synchronized (this) {
            StringBuilder stringBuilder = new StringBuilder("select * from ");
            stringBuilder.append(str);
            stringBuilder.append(" where ");
            stringBuilder.append(str2);
            stringBuilder.append(" in (");
            stringBuilder.append(j.a((List) list, ",", "'"));
            stringBuilder.append(")");
            try {
                cursor = getWritableDatabase().rawQuery(stringBuilder.toString(), null);
            } catch (Exception e) {
                d.c(a, "query record fail");
            }
        }
        return cursor;
    }

    public void b(String str) throws Exception {
        if (g(str)) {
            try {
                this.d.execSQL(" DROP TABLE " + str);
                return;
            } catch (Exception e) {
                d.c(a, "delete table fail");
                throw e;
            }
        }
        throw new Exception();
    }

    public void c(String str) throws Exception {
        try {
            this.d.execSQL(str);
        } catch (Exception e) {
            d.c(a, "executeSQL error");
            throw e;
        }
    }

    public synchronized void close() {
        int decrementAndGet = b.decrementAndGet();
        d.a(a, "close, count is:", BuildConfig.FLAVOR + decrementAndGet);
        if (decrementAndGet == 0) {
            super.close();
        }
    }

    public String[] d(String str) throws Exception {
        Cursor cursor = null;
        if (g(str)) {
            try {
                cursor = this.d.rawQuery(" select * from " + str + " order by _id asc LIMIT 1", null);
                cursor.moveToNext();
                String[] columnNames = cursor.getColumnNames();
                if (cursor != null) {
                    cursor.close();
                }
                return columnNames;
            } catch (Exception e) {
                d.c(a, "getColumnNames error");
                throw e;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            throw new Exception();
        }
    }

    public boolean e(String str) throws Exception {
        Cursor cursor = null;
        boolean z = false;
        if (!g(str)) {
            throw new Exception();
        } else if (str == null) {
            return false;
        } else {
            try {
                cursor = this.d.rawQuery("select count(1) as c from sqlite_master where type ='table' and name = ?", new String[]{str.trim()});
                if (cursor.moveToNext()) {
                    if (cursor.getInt(0) > 0) {
                        z = true;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return z;
            } catch (Exception e) {
                throw e;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public void f(String str) throws Exception {
        if (g(str)) {
            try {
                this.d.execSQL(" ALTER TABLE " + str + " RENAME TO _temp_" + str);
                return;
            } catch (Exception e) {
                d.c(a, "modifyTableName fail");
                throw e;
            }
        }
        throw new Exception();
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        b.incrementAndGet();
        this.d = sQLiteDatabase;
        a(0, false);
        b.decrementAndGet();
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        b.incrementAndGet();
        this.d = sQLiteDatabase;
        if (i >= 6) {
            a(i2, true);
        } else {
            a(i2, false);
        }
        a();
        b.decrementAndGet();
    }
}
