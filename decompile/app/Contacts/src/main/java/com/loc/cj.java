package com.loc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.autonavi.aps.amapapi.model.AmapLoc;
import java.util.ArrayList;
import org.json.JSONObject;

/* compiled from: DB */
public class cj {
    private static cj a = null;
    private String b = "2.0.201501131131".replace(".", "");
    private String c = null;

    public static synchronized cj a() {
        cj cjVar;
        synchronized (cj.class) {
            if (a == null) {
                a = new cj();
            }
            cjVar = a;
        }
        return cjVar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean a(SQLiteDatabase sQLiteDatabase, String str) {
        Cursor cursor = null;
        boolean z = false;
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        boolean z2;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SELECT count(*) as c FROM sqlite_master WHERE type = 'table' AND name = '");
            stringBuilder.append(str.trim()).append(this.b).append("' ");
            cursor = sQLiteDatabase.rawQuery(stringBuilder.toString(), null);
            if (cursor != null && cursor.moveToFirst() && cursor.getInt(0) > 0) {
                z = true;
            }
            stringBuilder.delete(0, stringBuilder.length());
            if (cursor != null) {
                cursor.close();
            }
            z2 = z;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
            z2 = true;
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
        return z2;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void a(Context context) throws Exception {
        if (context != null) {
            SQLiteDatabase openOrCreateDatabase = context.openOrCreateDatabase("hmdb", 0, null);
            if (a(openOrCreateDatabase, "hist")) {
                Cursor rawQuery;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("SELECT feature, nb, loc FROM ");
                stringBuilder.append("hist").append(this.b);
                stringBuilder.append(" WHERE time > ").append(cw.a() - 259200000);
                stringBuilder.append(" ORDER BY time ASC").append(";");
                try {
                    rawQuery = openOrCreateDatabase.rawQuery(stringBuilder.toString(), null);
                } catch (Throwable th) {
                    e.a(th, "DB", "fetchHist");
                    Object message = th.getMessage();
                    rawQuery = (!TextUtils.isEmpty(message) && message.contains("no such table")) ? null : null;
                }
                StringBuilder stringBuilder2 = new StringBuilder();
                if (this.c == null) {
                    this.c = cg.a("MD5", context.getPackageName());
                }
                if (rawQuery != null && rawQuery.moveToFirst()) {
                    int i = 0;
                    while (true) {
                        JSONObject jSONObject;
                        JSONObject jSONObject2;
                        if (rawQuery.getString(0).startsWith("{")) {
                            jSONObject = new JSONObject(rawQuery.getString(0));
                            stringBuilder2.delete(0, stringBuilder2.length());
                            if (!TextUtils.isEmpty(rawQuery.getString(1))) {
                                stringBuilder2.append(rawQuery.getString(1));
                            } else if (cw.a(jSONObject, "mmac")) {
                                stringBuilder2.append("#").append(jSONObject.getString("mmac"));
                                stringBuilder2.append(",access");
                            }
                            jSONObject2 = new JSONObject(rawQuery.getString(2));
                            if (cw.a(jSONObject2, "type")) {
                                jSONObject2.put("type", "new");
                            }
                        } else {
                            jSONObject = new JSONObject(new String(cg.d(r.b(rawQuery.getString(0)), this.c), "UTF-8"));
                            stringBuilder2.delete(0, stringBuilder2.length());
                            if (!TextUtils.isEmpty(rawQuery.getString(1))) {
                                stringBuilder2.append(new String(cg.d(r.b(rawQuery.getString(1)), this.c), "UTF-8"));
                            } else if (cw.a(jSONObject, "mmac")) {
                                stringBuilder2.append("#").append(jSONObject.getString("mmac"));
                                stringBuilder2.append(",access");
                            }
                            jSONObject2 = new JSONObject(new String(cg.d(r.b(rawQuery.getString(2)), this.c), "UTF-8"));
                            if (cw.a(jSONObject2, "type")) {
                                jSONObject2.put("type", "new");
                            }
                        }
                        int i2 = i + 1;
                        AmapLoc amapLoc = new AmapLoc(jSONObject2);
                        String str = "";
                        if (cw.a(jSONObject, "mmac") && cw.a(jSONObject, "cgi")) {
                            str = (jSONObject.getString("cgi") + "#") + "network#";
                            str = !jSONObject.getString("cgi").contains("#") ? str + "wifi" : str + "cgiwifi";
                            ci.a().a(str + "&" + amapLoc.e() + "&" + amapLoc.f(), stringBuilder2, amapLoc, context, false);
                        } else if (cw.a(jSONObject, "cgi")) {
                            str = (jSONObject.getString("cgi") + "#") + "network#";
                            if (jSONObject.getString("cgi").contains("#")) {
                                str = str + "cgi";
                                ci.a().a(str + "&" + amapLoc.e() + "&" + amapLoc.f(), stringBuilder2, amapLoc, context, false);
                            }
                        }
                        if (!rawQuery.moveToNext()) {
                            break;
                        }
                        i = i2;
                    }
                }
                stringBuilder2.delete(0, stringBuilder2.length());
                if (rawQuery != null) {
                    rawQuery.close();
                }
                stringBuilder.delete(0, stringBuilder.length());
                if (openOrCreateDatabase != null && openOrCreateDatabase.isOpen()) {
                    openOrCreateDatabase.close();
                }
            } else if (openOrCreateDatabase != null) {
                if (openOrCreateDatabase.isOpen()) {
                    openOrCreateDatabase.close();
                }
            }
        }
    }

    public void a(Context context, int i) throws Exception {
        String[] strArr = null;
        if (context != null) {
            SQLiteDatabase openOrCreateDatabase = context.openOrCreateDatabase("hmdb", 0, null);
            if (a(openOrCreateDatabase, "hist")) {
                String str;
                switch (i) {
                    case 1:
                        str = "time<?";
                        strArr = new String[]{String.valueOf(cw.a() - 259200000)};
                        break;
                    case 2:
                        str = CallInterceptDetails.BRANDED_STATE;
                        break;
                    default:
                        str = null;
                        break;
                }
                try {
                    openOrCreateDatabase.delete("hist" + this.b, str, strArr);
                } catch (Throwable th) {
                    e.a(th, "DB", "clearHist");
                    Object message = th.getMessage();
                    if (!(TextUtils.isEmpty(message) || message.contains("no such table"))) {
                    }
                }
                if (openOrCreateDatabase != null && openOrCreateDatabase.isOpen()) {
                    openOrCreateDatabase.close();
                }
                return;
            }
            if (openOrCreateDatabase != null && openOrCreateDatabase.isOpen()) {
                openOrCreateDatabase.close();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void a(Context context, String str, String str2, long j) throws Exception {
        Throwable th;
        Cursor cursor = null;
        synchronized (this) {
            if (!(TextUtils.isEmpty(str) || context == null)) {
                String c = cw.c(str);
                String c2 = cw.c(str2);
                SQLiteDatabase openOrCreateDatabase;
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    openOrCreateDatabase = context.openOrCreateDatabase("hmdb", 0, null);
                    try {
                        stringBuilder.append("CREATE TABLE IF NOT EXISTS ").append("hm");
                        stringBuilder.append(this.b);
                        stringBuilder.append(" (hash VARCHAR PRIMARY KEY, num INTEGER, extra VARCHAR, time VARCHAR);");
                        openOrCreateDatabase.execSQL(stringBuilder.toString());
                        stringBuilder.delete(0, stringBuilder.length());
                        stringBuilder.append("SELECT num FROM ").append("hm");
                        stringBuilder.append(this.b);
                        stringBuilder.append(" WHERE hash = '").append(c).append("';");
                        cursor = openOrCreateDatabase.rawQuery(stringBuilder.toString(), null);
                    } catch (Throwable th2) {
                        th = th2;
                        if (openOrCreateDatabase != null && openOrCreateDatabase.isOpen()) {
                            openOrCreateDatabase.close();
                        }
                        throw th;
                    }
                    int i = (cursor != null && cursor.moveToNext()) ? cursor.getInt(0) : 0;
                    if (i <= 0) {
                        stringBuilder.delete(0, stringBuilder.length());
                        stringBuilder.append("REPLACE INTO ");
                        stringBuilder.append("hm").append(this.b);
                        stringBuilder.append(" VALUES (?, ?, ?, ?)");
                        openOrCreateDatabase.execSQL(stringBuilder.toString(), new Object[]{c, Integer.valueOf(1), c2, Long.valueOf(j)});
                    } else {
                        i++;
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("num", Integer.valueOf(i));
                        contentValues.put("extra", c2);
                        contentValues.put("time", Long.valueOf(j));
                        openOrCreateDatabase.update("hm" + this.b, contentValues, "hash = '" + c + "'", null);
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                    if (openOrCreateDatabase != null) {
                        if (openOrCreateDatabase.isOpen()) {
                            openOrCreateDatabase.close();
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    Object obj = cursor;
                    openOrCreateDatabase.close();
                    throw th;
                }
            }
        }
    }

    protected void a(String str, AmapLoc amapLoc, StringBuilder stringBuilder, Context context) throws Exception {
        if (context != null) {
            if (this.c == null) {
                this.c = cg.a("MD5", context.getPackageName());
            }
            JSONObject jSONObject = new JSONObject();
            if (str.contains("&")) {
                str = str.substring(0, str.indexOf("&"));
            }
            String substring = str.substring(str.lastIndexOf("#") + 1);
            if (substring.equals("cgi")) {
                jSONObject.put("cgi", str.substring(0, str.length() - (("network".length() + 2) + "cgi".length())));
            } else if (!(TextUtils.isEmpty(stringBuilder) || stringBuilder.indexOf("access") == -1)) {
                jSONObject.put("cgi", str.substring(0, str.length() - (substring.length() + ("network".length() + 2))));
                String[] split = stringBuilder.toString().split(",access");
                jSONObject.put("mmac", !split[0].contains("#") ? split[0] : split[0].substring(split[0].lastIndexOf("#") + 1));
            }
            if (cw.a(jSONObject, "cgi") || cw.a(jSONObject, "mmac")) {
                StringBuilder stringBuilder2 = new StringBuilder();
                SQLiteDatabase openOrCreateDatabase = context.openOrCreateDatabase("hmdb", 0, null);
                stringBuilder2.append("CREATE TABLE IF NOT EXISTS ").append("hist");
                stringBuilder2.append(this.b);
                stringBuilder2.append(" (feature VARCHAR PRIMARY KEY, nb VARCHAR, loc VARCHAR, time VARCHAR);");
                openOrCreateDatabase.execSQL(stringBuilder2.toString());
                stringBuilder2.delete(0, stringBuilder2.length());
                stringBuilder2.append("REPLACE INTO ");
                stringBuilder2.append("hist").append(this.b);
                stringBuilder2.append(" VALUES (?, ?, ?, ?)");
                Object[] objArr = new Object[]{cg.c(jSONObject.toString().getBytes("UTF-8"), this.c), cg.c(stringBuilder.toString().getBytes("UTF-8"), this.c), cg.c(amapLoc.F().getBytes("UTF-8"), this.c), Long.valueOf(amapLoc.k())};
                for (int i = 0; i < objArr.length - 1; i++) {
                    objArr[i] = r.b((byte[]) objArr[i]);
                }
                openOrCreateDatabase.execSQL(stringBuilder2.toString(), objArr);
                stringBuilder2.delete(0, stringBuilder2.length());
                stringBuilder2.append("SELECT COUNT(*) AS total FROM ");
                stringBuilder2.append("hist").append(this.b).append(";");
                Cursor rawQuery = openOrCreateDatabase.rawQuery(stringBuilder2.toString(), null);
                if (!(rawQuery == null || rawQuery.moveToFirst())) {
                }
                if (rawQuery != null) {
                    rawQuery.close();
                }
                stringBuilder2.delete(0, stringBuilder2.length());
                if (openOrCreateDatabase != null && openOrCreateDatabase.isOpen()) {
                    openOrCreateDatabase.close();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized ArrayList<String> b(Context context, int i) throws Exception {
        Object d;
        Throwable th;
        if (context == null) {
            return null;
        }
        SQLiteDatabase openOrCreateDatabase;
        try {
            openOrCreateDatabase = context.openOrCreateDatabase("hmdb", 0, null);
            try {
                if (a(openOrCreateDatabase, "hm")) {
                    String str;
                    String[] strArr;
                    String str2;
                    StringBuilder stringBuilder;
                    Cursor rawQuery;
                    String string;
                    ArrayList<String> arrayList = new ArrayList();
                    switch (i) {
                        case 1:
                            str = "time<?";
                            strArr = new String[]{String.valueOf(cw.a() - 1209600000)};
                        case 2:
                            str = CallInterceptDetails.BRANDED_STATE;
                            strArr = null;
                            str2 = "hm" + this.b;
                            if (strArr == null) {
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("SELECT hash, num, extra FROM ");
                                stringBuilder.append(str2);
                                stringBuilder.append(" WHERE time < ").append(strArr[0]).append(";");
                                rawQuery = openOrCreateDatabase.rawQuery(stringBuilder.toString(), null);
                                if (rawQuery != null) {
                                    if (rawQuery.moveToFirst()) {
                                        while (true) {
                                            string = rawQuery.getString(0);
                                            str2 = rawQuery.getString(2);
                                            if (!str2.startsWith("{")) {
                                                d = cw.d(string);
                                                cw.d(str2);
                                            }
                                            arrayList.add(d);
                                            if (rawQuery.moveToNext()) {
                                            }
                                        }
                                    }
                                }
                                if (rawQuery != null) {
                                    rawQuery.close();
                                }
                                if (openOrCreateDatabase != null) {
                                    if (openOrCreateDatabase.isOpen()) {
                                        openOrCreateDatabase.close();
                                        break;
                                    }
                                }
                            }
                            openOrCreateDatabase.delete(str2, str, strArr);
                            if (openOrCreateDatabase != null && openOrCreateDatabase.isOpen()) {
                                openOrCreateDatabase.close();
                                break;
                            }
                            break;
                        default:
                            strArr = null;
                            str = null;
                    }
                    str2 = "hm" + this.b;
                    if (strArr == null) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("SELECT hash, num, extra FROM ");
                        stringBuilder.append(str2);
                        stringBuilder.append(" WHERE time < ").append(strArr[0]).append(";");
                        rawQuery = openOrCreateDatabase.rawQuery(stringBuilder.toString(), null);
                        if (rawQuery != null) {
                            if (rawQuery.moveToFirst()) {
                                while (true) {
                                    string = rawQuery.getString(0);
                                    str2 = rawQuery.getString(2);
                                    if (str2.startsWith("{")) {
                                        d = cw.d(string);
                                        cw.d(str2);
                                    }
                                    arrayList.add(d);
                                    if (rawQuery.moveToNext()) {
                                    }
                                }
                            }
                        }
                        if (rawQuery != null) {
                            rawQuery.close();
                        }
                        if (openOrCreateDatabase != null) {
                            if (openOrCreateDatabase.isOpen()) {
                                openOrCreateDatabase.close();
                            }
                        }
                    } else {
                        openOrCreateDatabase.delete(str2, str, strArr);
                        openOrCreateDatabase.close();
                    }
                } else if (openOrCreateDatabase != null) {
                    if (openOrCreateDatabase.isOpen()) {
                        openOrCreateDatabase.close();
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                openOrCreateDatabase.close();
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            openOrCreateDatabase = null;
            if (openOrCreateDatabase != null && openOrCreateDatabase.isOpen()) {
                openOrCreateDatabase.close();
            }
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void b(Context context) throws Exception {
        Throwable th;
        SQLiteDatabase sQLiteDatabase = null;
        synchronized (this) {
            if (by.a && context != null) {
                try {
                    SQLiteDatabase openOrCreateDatabase = context.openOrCreateDatabase("hmdb", 0, null);
                    try {
                        if (a(openOrCreateDatabase, "hm")) {
                            long a = cw.a() - 1209600000;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("SELECT hash, num, extra, time FROM ");
                            stringBuilder.append("hm").append(this.b);
                            stringBuilder.append(" WHERE time > ").append(a);
                            stringBuilder.append(" ORDER BY num DESC LIMIT 0,");
                            stringBuilder.append(VTMCDataCache.MAXSIZE).append(";");
                            Cursor rawQuery = openOrCreateDatabase.rawQuery(stringBuilder.toString(), null);
                            stringBuilder.delete(0, stringBuilder.length());
                            if (rawQuery != null) {
                                rawQuery.moveToFirst();
                                int i = 0;
                                while (true) {
                                    i++;
                                    String string = rawQuery.getString(0);
                                    int i2 = rawQuery.getInt(1);
                                    String string2 = rawQuery.getString(2);
                                    long j = rawQuery.getLong(3);
                                    if (!string2.startsWith("{")) {
                                        string = cw.d(string);
                                        string2 = cw.d(string2);
                                    }
                                    cl.a().a(context, string, string2, i2, j, false);
                                    if (!rawQuery.moveToNext()) {
                                        break;
                                    }
                                }
                            }
                            if (rawQuery != null) {
                                rawQuery.close();
                            }
                            if (openOrCreateDatabase != null) {
                                if (openOrCreateDatabase.isOpen()) {
                                    openOrCreateDatabase.close();
                                }
                            }
                        } else if (openOrCreateDatabase != null) {
                            if (openOrCreateDatabase.isOpen()) {
                                openOrCreateDatabase.close();
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        sQLiteDatabase = openOrCreateDatabase;
                        sQLiteDatabase.close();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (sQLiteDatabase != null && sQLiteDatabase.isOpen()) {
                        sQLiteDatabase.close();
                    }
                    throw th;
                }
            }
        }
    }
}
