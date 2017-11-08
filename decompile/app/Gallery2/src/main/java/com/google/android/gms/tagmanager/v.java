package com.google.android.gms.tagmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.google.android.gms.internal.fl;
import com.google.android.gms.internal.fn;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/* compiled from: Unknown */
class v implements c {
    private static final String UD = String.format("CREATE TABLE IF NOT EXISTS %s ( '%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '%s' STRING NOT NULL, '%s' BLOB NOT NULL, '%s' INTEGER NOT NULL);", new Object[]{"datalayer", "ID", "key", "value", "expires"});
    private fl Ty;
    private final Executor UE;
    private a UF;
    private int UG;
    private final Context mContext;

    /* compiled from: Unknown */
    class a extends SQLiteOpenHelper {
        final /* synthetic */ v UJ;

        a(v vVar, Context context, String str) {
            this.UJ = vVar;
            super(context, str, null, 1);
        }

        private void a(SQLiteDatabase sQLiteDatabase) {
            Cursor rawQuery = sQLiteDatabase.rawQuery("SELECT * FROM datalayer WHERE 0", null);
            Set hashSet = new HashSet();
            try {
                String[] columnNames = rawQuery.getColumnNames();
                for (Object add : columnNames) {
                    hashSet.add(add);
                }
                if (!hashSet.remove("key") || !hashSet.remove("value") || !hashSet.remove("ID") || !hashSet.remove("expires")) {
                    throw new SQLiteException("Database column missing");
                } else if (!hashSet.isEmpty()) {
                    throw new SQLiteException("Database has extra columns");
                }
            } finally {
                rawQuery.close();
            }
        }

        private boolean a(String str, SQLiteDatabase sQLiteDatabase) {
            Cursor query;
            Throwable th;
            Cursor cursor = null;
            try {
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                query = sQLiteDatabase2.query("SQLITE_MASTER", new String[]{"name"}, "name=?", new String[]{str}, null, null, null);
                try {
                    boolean moveToFirst = query.moveToFirst();
                    if (query != null) {
                        query.close();
                    }
                    return moveToFirst;
                } catch (SQLiteException e) {
                    try {
                        bh.w("Error querying for table " + str);
                        if (query != null) {
                            query.close();
                        }
                        return false;
                    } catch (Throwable th2) {
                        cursor = query;
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            } catch (SQLiteException e2) {
                query = null;
                bh.w("Error querying for table " + str);
                if (query != null) {
                    query.close();
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }

        public SQLiteDatabase getWritableDatabase() {
            SQLiteDatabase sQLiteDatabase = null;
            try {
                sQLiteDatabase = super.getWritableDatabase();
            } catch (SQLiteException e) {
                this.UJ.mContext.getDatabasePath("google_tagmanager.db").delete();
            }
            return sQLiteDatabase != null ? sQLiteDatabase : super.getWritableDatabase();
        }

        public void onCreate(SQLiteDatabase db) {
            ak.B(db.getPath());
        }

        public void onOpen(SQLiteDatabase db) {
            if (VERSION.SDK_INT < 15) {
                Cursor rawQuery = db.rawQuery("PRAGMA journal_mode=memory", null);
                try {
                    rawQuery.moveToFirst();
                } finally {
                    rawQuery.close();
                }
            }
            if (a("datalayer", db)) {
                a(db);
            } else {
                db.execSQL(v.UD);
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    /* compiled from: Unknown */
    private static class b {
        final String UA;
        final byte[] UM;

        b(String str, byte[] bArr) {
            this.UA = str;
            this.UM = bArr;
        }

        public String toString() {
            return "KeyAndSerialized: key = " + this.UA + " serialized hash = " + Arrays.hashCode(this.UM);
        }
    }

    public v(Context context) {
        this(context, fn.eI(), "google_tagmanager.db", 2000, Executors.newSingleThreadExecutor());
    }

    v(Context context, fl flVar, String str, int i, Executor executor) {
        this.mContext = context;
        this.Ty = flVar;
        this.UG = i;
        this.UE = executor;
        this.UF = new a(this, this.mContext, str);
    }

    private SQLiteDatabase G(String str) {
        try {
            return this.UF.getWritableDatabase();
        } catch (SQLiteException e) {
            bh.w(str);
            return null;
        }
    }

    private synchronized void b(List<b> list, long j) {
        try {
            long currentTimeMillis = this.Ty.currentTimeMillis();
            t(currentTimeMillis);
            bQ(list.size());
            c(list, currentTimeMillis + j);
            iW();
        } catch (Throwable th) {
            iW();
        }
    }

    private void bQ(int i) {
        int iV = (iV() - this.UG) + i;
        if (iV > 0) {
            List bR = bR(iV);
            bh.u("DataLayer store full, deleting " + bR.size() + " entries to make room.");
            g((String[]) bR.toArray(new String[0]));
        }
    }

    private List<String> bR(int i) {
        SQLiteException e;
        Throwable th;
        List<String> arrayList = new ArrayList();
        if (i > 0) {
            SQLiteDatabase G = G("Error opening database for peekEntryIds.");
            if (G == null) {
                return arrayList;
            }
            Cursor query;
            try {
                query = G.query("datalayer", new String[]{"ID"}, null, null, null, null, String.format("%s ASC", new Object[]{"ID"}), Integer.toString(i));
                try {
                    if (query.moveToFirst()) {
                        while (true) {
                            arrayList.add(String.valueOf(query.getLong(0)));
                            if (!query.moveToNext()) {
                                break;
                            }
                        }
                    }
                    if (query != null) {
                        query.close();
                    }
                } catch (SQLiteException e2) {
                    e = e2;
                }
            } catch (SQLiteException e3) {
                e = e3;
                query = null;
                try {
                    bh.w("Error in peekEntries fetching entryIds: " + e.getMessage());
                    if (query != null) {
                        query.close();
                    }
                    return arrayList;
                } catch (Throwable th2) {
                    th = th2;
                    if (query != null) {
                        query.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                query = null;
                if (query != null) {
                    query.close();
                }
                throw th;
            }
            return arrayList;
        }
        bh.w("Invalid maxEntries specified. Skipping.");
        return arrayList;
    }

    private List<a> c(List<b> list) {
        List<a> arrayList = new ArrayList();
        for (b bVar : list) {
            arrayList.add(new a(bVar.UA, j(bVar.UM)));
        }
        return arrayList;
    }

    private void c(List<b> list, long j) {
        SQLiteDatabase G = G("Error opening database for writeEntryToDatabase.");
        if (G != null) {
            for (b bVar : list) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("expires", Long.valueOf(j));
                contentValues.put("key", bVar.UA);
                contentValues.put("value", bVar.UM);
                G.insert("datalayer", null, contentValues);
            }
        }
    }

    private List<b> d(List<a> list) {
        List<b> arrayList = new ArrayList();
        for (a aVar : list) {
            arrayList.add(new b(aVar.UA, j(aVar.UB)));
        }
        return arrayList;
    }

    private void g(String[] strArr) {
        if (strArr != null && strArr.length != 0) {
            SQLiteDatabase G = G("Error opening database for deleteEntries.");
            if (G != null) {
                try {
                    G.delete("datalayer", String.format("%s in (%s)", new Object[]{"ID", TextUtils.join(",", Collections.nCopies(strArr.length, "?"))}), strArr);
                } catch (SQLiteException e) {
                    bh.w("Error deleting entries " + Arrays.toString(strArr));
                }
            }
        }
    }

    private List<a> iT() {
        try {
            t(this.Ty.currentTimeMillis());
            List<a> c = c(iU());
            return c;
        } finally {
            iW();
        }
    }

    private List<b> iU() {
        SQLiteDatabase G = G("Error opening database for loadSerialized.");
        List<b> arrayList = new ArrayList();
        if (G == null) {
            return arrayList;
        }
        Cursor query = G.query("datalayer", new String[]{"key", "value"}, null, null, null, null, "ID", null);
        while (query.moveToNext()) {
            try {
                arrayList.add(new b(query.getString(0), query.getBlob(1)));
            } finally {
                query.close();
            }
        }
        return arrayList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int iV() {
        int i = 0;
        Cursor cursor = null;
        SQLiteDatabase G = G("Error opening database for getNumStoredEntries.");
        if (G == null) {
            return 0;
        }
        try {
            cursor = G.rawQuery("SELECT COUNT(*) from datalayer", null);
            if (cursor.moveToFirst()) {
                i = (int) cursor.getLong(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            bh.w("Error getting numStoredEntries");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            r1 = cursor;
            Throwable th3 = th2;
            Cursor cursor2;
            if (cursor2 != null) {
                cursor2.close();
            }
            throw th3;
        }
        return i;
    }

    private void iW() {
        try {
            this.UF.close();
        } catch (SQLiteException e) {
        }
    }

    private Object j(byte[] bArr) {
        Throwable th;
        ObjectInputStream objectInputStream = null;
        InputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        ObjectInputStream objectInputStream2;
        try {
            objectInputStream2 = new ObjectInputStream(byteArrayInputStream);
            try {
                Object readObject = objectInputStream2.readObject();
                if (objectInputStream2 != null) {
                    objectInputStream2.close();
                }
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                }
                return readObject;
            } catch (IOException e2) {
                if (objectInputStream2 != null) {
                    objectInputStream2.close();
                }
                try {
                    byteArrayInputStream.close();
                } catch (IOException e3) {
                }
                return objectInputStream;
            } catch (ClassNotFoundException e4) {
                if (objectInputStream2 != null) {
                    objectInputStream2.close();
                }
                try {
                    byteArrayInputStream.close();
                } catch (IOException e5) {
                }
                return objectInputStream;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                objectInputStream = objectInputStream2;
                th = th3;
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                try {
                    byteArrayInputStream.close();
                } catch (IOException e6) {
                }
                throw th;
            }
        } catch (IOException e7) {
            objectInputStream2 = objectInputStream;
            if (objectInputStream2 != null) {
                objectInputStream2.close();
            }
            byteArrayInputStream.close();
            return objectInputStream;
        } catch (ClassNotFoundException e8) {
            objectInputStream2 = objectInputStream;
            if (objectInputStream2 != null) {
                objectInputStream2.close();
            }
            byteArrayInputStream.close();
            return objectInputStream;
        } catch (Throwable th4) {
            th = th4;
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            byteArrayInputStream.close();
            throw th;
        }
    }

    private byte[] j(Object obj) {
        ObjectOutputStream objectOutputStream;
        Throwable th;
        ObjectOutputStream objectOutputStream2 = null;
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            try {
                objectOutputStream.writeObject(obj);
                byte[] toByteArray = byteArrayOutputStream.toByteArray();
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                }
                return toByteArray;
            } catch (IOException e2) {
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e3) {
                }
                return objectOutputStream2;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                objectOutputStream2 = objectOutputStream;
                th = th3;
                if (objectOutputStream2 != null) {
                    objectOutputStream2.close();
                }
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        } catch (IOException e5) {
            objectOutputStream = objectOutputStream2;
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            byteArrayOutputStream.close();
            return objectOutputStream2;
        } catch (Throwable th4) {
            th = th4;
            if (objectOutputStream2 != null) {
                objectOutputStream2.close();
            }
            byteArrayOutputStream.close();
            throw th;
        }
    }

    private void t(long j) {
        SQLiteDatabase G = G("Error opening database for deleteOlderThan.");
        if (G != null) {
            try {
                bh.v("Deleted " + G.delete("datalayer", "expires <= ?", new String[]{Long.toString(j)}) + " expired items");
            } catch (SQLiteException e) {
                bh.w("Error deleting old entries.");
            }
        }
    }

    public void a(final com.google.android.gms.tagmanager.DataLayer.c.a aVar) {
        this.UE.execute(new Runnable(this) {
            final /* synthetic */ v UJ;

            public void run() {
                aVar.b(this.UJ.iT());
            }
        });
    }

    public void a(List<a> list, final long j) {
        final List d = d(list);
        this.UE.execute(new Runnable(this) {
            final /* synthetic */ v UJ;

            public void run() {
                this.UJ.b(d, j);
            }
        });
    }
}
