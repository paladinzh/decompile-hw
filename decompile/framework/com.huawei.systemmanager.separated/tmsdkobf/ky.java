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
public class ky {
    private static Object lock = new Object();
    private static ky wq;
    private lc qU;

    private ky() {
        this.qU = null;
        this.qU = ((ln) fe.ad(9)).bp("QQSecureProvider");
    }

    private ContentValues a(ao aoVar) {
        int i = 0;
        if (aoVar == null) {
            return null;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("b", aoVar.packageName);
        contentValues.put("c", aoVar.softName);
        contentValues.put("d", aoVar.certMd5);
        contentValues.put("e", Long.valueOf(aoVar.bu));
        String str = "f";
        if (aoVar.bv) {
            i = 1;
        }
        contentValues.put(str, Integer.valueOf(i));
        contentValues.put("h", aoVar.aA);
        contentValues.put("i", Long.valueOf(aoVar.bw));
        contentValues.put("j", aoVar.version);
        contentValues.put("g", aoVar.dexSha1);
        contentValues.put("k", Long.valueOf(aoVar.bx));
        return contentValues;
    }

    public static void a(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS pf_soft_list_profile_db_table_name (a INTEGER PRIMARY KEY,b TEXT,c TEXT,d TEXT,e LONG,f INTEGER,h TEXT,i LONG,g TEXT,j TEXT,k LONG)");
    }

    public static void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        d.d("SoftListProfileDB", "upgradeDB");
        if (i < 15) {
            a(sQLiteDatabase);
        }
        if (i >= 15 && i < 18) {
            try {
                sQLiteDatabase.execSQL("ALTER TABLE pf_soft_list_profile_db_table_name ADD COLUMN k LONG");
            } catch (Exception e) {
            }
        }
    }

    public static void b(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS pf_soft_list_profile_db_table_name");
    }

    public static void b(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        d.d("SoftListProfileDB", "downgradeDB");
        b(sQLiteDatabase);
        a(sQLiteDatabase);
    }

    private void clear() {
        this.qU.delete("pf_soft_list_profile_db_table_name", null, null);
    }

    public static ky du() {
        if (wq == null) {
            synchronized (lock) {
                if (wq == null) {
                    wq = new ky();
                }
            }
        }
        return wq;
    }

    private ArrayList<kx> dw() {
        Cursor a;
        Exception e;
        Throwable th;
        ArrayList<kx> arrayList = new ArrayList();
        try {
            a = this.qU.a("pf_soft_list_profile_db_table_name", null, null, null, null);
            if (a != null) {
                try {
                    a.moveToFirst();
                    while (!a.isAfterLast()) {
                        ao aoVar = new ao();
                        aoVar.bu = a.getLong(a.getColumnIndex("e"));
                        aoVar.certMd5 = a.getString(a.getColumnIndex("d"));
                        aoVar.aA = a.getString(a.getColumnIndex("h"));
                        aoVar.bv = a.getInt(a.getColumnIndex("f")) == 1;
                        aoVar.packageName = a.getString(a.getColumnIndex("b"));
                        aoVar.softName = a.getString(a.getColumnIndex("c"));
                        aoVar.version = a.getString(a.getColumnIndex("j"));
                        aoVar.bw = a.getLong(a.getColumnIndex("i"));
                        aoVar.dexSha1 = a.getString(a.getColumnIndex("g"));
                        aoVar.bx = a.getLong(a.getColumnIndex("k"));
                        arrayList.add(new kx(aoVar, a.getInt(a.getColumnIndex("a"))));
                        a.moveToNext();
                    }
                    if (a != null) {
                        try {
                            a.close();
                        } catch (Exception e2) {
                            d.c("SoftListProfileDB", "cursor.close() crash : " + e2.toString());
                        }
                    }
                } catch (Exception e3) {
                    e2 = e3;
                }
                return arrayList;
            }
            if (a != null) {
                try {
                    a.close();
                } catch (Exception e22) {
                    d.c("SoftListProfileDB", "cursor.close() crash : " + e22.toString());
                }
            }
            return arrayList;
        } catch (Exception e4) {
            e22 = e4;
            a = null;
            try {
                d.c("SoftListProfileDB", e22.toString());
                if (a != null) {
                    try {
                        a.close();
                    } catch (Exception e222) {
                        d.c("SoftListProfileDB", "cursor.close() crash : " + e222.toString());
                    }
                }
                return arrayList;
            } catch (Throwable th2) {
                th = th2;
                if (a != null) {
                    try {
                        a.close();
                    } catch (Exception e5) {
                        d.c("SoftListProfileDB", "cursor.close() crash : " + e5.toString());
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

    public ArrayList<ao> dv() {
        d.d("SoftListProfileDB", "getAllSoftImage");
        ArrayList<ao> arrayList = new ArrayList();
        ArrayList dw = dw();
        if (dw != null && dw.size() > 0) {
            Iterator it = dw.iterator();
            while (it.hasNext()) {
                kx kxVar = (kx) it.next();
                if (!(kxVar == null || kxVar.wp == null)) {
                    arrayList.add(kxVar.wp);
                }
            }
        }
        return arrayList;
    }

    public boolean s(ArrayList<a> arrayList) {
        if (arrayList == null || arrayList.size() <= 0) {
            return true;
        }
        ArrayList arrayList2 = new ArrayList();
        Iterator it = arrayList.iterator();
        boolean z = false;
        while (it.hasNext()) {
            a aVar = (a) it.next();
            if (!(aVar == null || aVar.vV == null || !(aVar.vV instanceof ao))) {
                ao aoVar = (ao) aVar.vV;
                switch (aVar.action) {
                    case 0:
                        if (!z) {
                            clear();
                            z = true;
                            break;
                        }
                        break;
                    case 1:
                        break;
                    case 2:
                        arrayList2.add(ContentProviderOperation.newDelete(this.qU.bn("pf_soft_list_profile_db_table_name")).withSelection(String.format("%s = '%s'", new Object[]{"b", aoVar.packageName}), null).build());
                        break;
                    case 3:
                        arrayList2.add(ContentProviderOperation.newUpdate(this.qU.bo("pf_soft_list_profile_db_table_name")).withValues(a(aoVar)).withSelection(String.format("%s = '%s'", new Object[]{"b", aoVar.packageName}), null).build());
                        break;
                }
                arrayList2.add(ContentProviderOperation.newInsert(this.qU.bm("pf_soft_list_profile_db_table_name")).withValues(a(aoVar)).build());
                z = z;
                continue;
            }
        }
        if (arrayList2 != null && arrayList2.size() > 0) {
            ContentProviderResult[] applyBatch = this.qU.applyBatch(arrayList2);
            if (applyBatch == null || applyBatch.length <= 0 || applyBatch[0] == null) {
                hu.h("SoftListProfileDB", "applyBatchOperation fail!!!");
                return false;
            }
        }
        return true;
    }
}
