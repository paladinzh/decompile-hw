package tmsdkobf;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.common.utils.d;
import tmsdkobf.kp.a;

/* compiled from: Unknown */
public class kt {
    private static Object lock = new Object();
    private static kt wg;
    private lc qU;

    private kt() {
        this.qU = null;
        this.qU = ((ln) fe.ad(9)).bp("QQSecureProvider");
    }

    private ContentValues a(al alVar) {
        int i = 0;
        ContentValues contentValues = new ContentValues();
        if (alVar == null) {
            return contentValues;
        }
        contentValues.put("b", Integer.valueOf(alVar.bm));
        contentValues.put("c", Integer.valueOf(alVar.valueType));
        switch (alVar.valueType) {
            case 1:
                contentValues.put("d", Integer.valueOf(alVar.i));
                break;
            case 2:
                contentValues.put("e", Long.valueOf(alVar.bn));
                break;
            case 3:
                contentValues.put("f", alVar.bo);
                break;
            case 4:
                contentValues.put("g", alVar.bp);
                break;
            case 5:
                String str = "h";
                if (alVar.bq) {
                    i = 1;
                }
                contentValues.put(str, Integer.valueOf(i));
                break;
            case 6:
                contentValues.put("i", Integer.valueOf(alVar.br));
                break;
        }
        return contentValues;
    }

    public static void a(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS pf_key_value_profile_db_table_name (a INTEGER PRIMARY KEY,b INTEGER,c INTEGER,d INTEGER,e LONG,f TEXT,h INTEGER,i INTEGER,g BLOB)");
    }

    public static void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i < 15) {
            a(sQLiteDatabase);
        }
    }

    public static void b(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS pf_key_value_profile_db_table_name");
    }

    public static void b(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        b(sQLiteDatabase);
        a(sQLiteDatabase);
    }

    private ArrayList<kw> bP(String str) {
        Exception e;
        Throwable th;
        ArrayList<kw> arrayList = new ArrayList();
        Cursor a;
        try {
            a = this.qU.a("pf_key_value_profile_db_table_name", null, str, null, null);
            if (a != null) {
                a.moveToFirst();
                while (!a.isAfterLast()) {
                    al alVar = new al();
                    alVar.valueType = a.getInt(a.getColumnIndex("c"));
                    alVar.bm = a.getInt(a.getColumnIndex("b"));
                    int i = a.getInt(a.getColumnIndex("a"));
                    switch (alVar.valueType) {
                        case 1:
                            try {
                                alVar.i = a.getInt(a.getColumnIndex("d"));
                                break;
                            } catch (Exception e2) {
                                e = e2;
                                break;
                            }
                        case 2:
                            alVar.bn = a.getLong(a.getColumnIndex("e"));
                            break;
                        case 3:
                            alVar.bo = a.getString(a.getColumnIndex("f"));
                            break;
                        case 4:
                            alVar.bp = a.getBlob(a.getColumnIndex("g"));
                            break;
                        case 5:
                            alVar.bq = a.getInt(a.getColumnIndex("h")) == 1;
                            break;
                        case 6:
                            alVar.br = (short) ((short) a.getInt(a.getColumnIndex("i")));
                            break;
                    }
                    arrayList.add(new kw(alVar, i));
                    a.moveToNext();
                }
                if (a != null) {
                    try {
                        a.close();
                    } catch (Exception e3) {
                        d.c("KeyValueProfileDB", "cursor.close() crash : " + e3.toString());
                    }
                }
                return arrayList;
            }
            if (a != null) {
                try {
                    a.close();
                } catch (Exception e32) {
                    d.c("KeyValueProfileDB", "cursor.close() crash : " + e32.toString());
                }
            }
            return arrayList;
        } catch (Exception e4) {
            e32 = e4;
            a = null;
            try {
                d.c("KeyValueProfileDB", e32.toString());
                if (a != null) {
                    try {
                        a.close();
                    } catch (Exception e322) {
                        d.c("KeyValueProfileDB", "cursor.close() crash : " + e322.toString());
                    }
                }
                return arrayList;
            } catch (Throwable th2) {
                th = th2;
                if (a != null) {
                    try {
                        a.close();
                    } catch (Exception e5) {
                        d.c("KeyValueProfileDB", "cursor.close() crash : " + e5.toString());
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            a = null;
            if (a != null) {
                a.close();
            }
            throw th;
        }
    }

    public static kt dp() {
        if (wg == null) {
            synchronized (lock) {
                if (wg == null) {
                    wg = new kt();
                }
            }
        }
        return wg;
    }

    public boolean a(ArrayList<a> arrayList, ArrayList<Boolean> arrayList2) {
        if (arrayList == null || arrayList.size() <= 0) {
            return true;
        }
        ArrayList arrayList3 = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            a aVar = (a) it.next();
            if (!(aVar == null || aVar.vV == null || !(aVar.vV instanceof al))) {
                al alVar = (al) aVar.vV;
                arrayList3.add(ContentProviderOperation.newDelete(this.qU.bn("pf_key_value_profile_db_table_name")).withSelection(String.format("%s = %s", new Object[]{"b", Integer.valueOf(alVar.bm)}), null).build());
            }
        }
        it = arrayList.iterator();
        while (it.hasNext()) {
            aVar = (a) it.next();
            if (!(aVar == null || aVar.vV == null || !(aVar.vV instanceof al))) {
                arrayList3.add(ContentProviderOperation.newInsert(this.qU.bm("pf_key_value_profile_db_table_name")).withValues(a((al) aVar.vV)).build());
            }
        }
        if (arrayList3 != null && arrayList3.size() > 0) {
            ContentProviderResult[] applyBatch = this.qU.applyBatch(arrayList3);
            if (applyBatch == null || applyBatch.length <= 0 || applyBatch[0] == null) {
                hu.h("KeyValueProfileDB", "applyBatchOperation fail!!!");
                return false;
            } else if (arrayList2 != null) {
                int length = applyBatch.length / 2;
                for (int i = 0; i < length; i++) {
                    if (applyBatch[i] != null) {
                        arrayList2.add(Boolean.valueOf(applyBatch[i].count.intValue() > 0));
                    }
                }
            }
        }
        return true;
    }

    public int bc(int i) {
        return this.qU.delete("pf_key_value_profile_db_table_name", "b = " + i, null);
    }

    public ArrayList<fs> getAll() {
        ArrayList<fs> arrayList = new ArrayList();
        ArrayList bP = bP(null);
        if (bP == null || bP.size() <= 0) {
            return arrayList;
        }
        Iterator it = bP.iterator();
        while (it.hasNext()) {
            arrayList.add(((kw) it.next()).wo);
        }
        return arrayList;
    }
}
