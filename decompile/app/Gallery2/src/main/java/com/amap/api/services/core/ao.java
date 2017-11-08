package com.amap.api.services.core;

import android.content.ContentValues;
import android.database.Cursor;
import com.amap.api.services.core.ad.a;

/* compiled from: SDKEntity */
public class ao implements ap<ad> {
    private static String a = ah.f;
    private static String b = ah.g;
    private static String c = ah.k;
    private static String d = ah.h;
    private static String e = ah.i;
    private static String f = ah.j;
    private ad g = null;

    public /* synthetic */ Object b(Cursor cursor) {
        return a(cursor);
    }

    public ContentValues b() {
        Throwable th;
        ContentValues contentValues;
        try {
            if (this.g == null) {
                return null;
            }
            contentValues = new ContentValues();
            try {
                contentValues.put(a, at.a(this.g.a()));
                contentValues.put(b, at.a(this.g.b()));
                contentValues.put(c, Boolean.valueOf(this.g.e()));
                contentValues.put(d, at.a(this.g.c()));
                contentValues.put(f, at.a(this.g.d()));
                contentValues.put(e, at.a(a(this.g.f())));
                return contentValues;
            } catch (Throwable th2) {
                th = th2;
                th.printStackTrace();
                return contentValues;
            }
        } catch (Throwable th3) {
            th = th3;
            contentValues = null;
            th.printStackTrace();
            return contentValues;
        }
    }

    public ad a(Cursor cursor) {
        boolean z = true;
        try {
            String b = at.b(cursor.getString(1));
            String b2 = at.b(cursor.getString(2));
            String b3 = at.b(cursor.getString(3));
            String[] b4 = b(at.b(cursor.getString(4)));
            String b5 = at.b(cursor.getString(5));
            if (cursor.getInt(6) == 0) {
                z = false;
            }
            return new a(b, b2, b3).a(z).a(b5).a(b4).a();
        } catch (v e) {
            e.printStackTrace();
            return null;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public void a(ad adVar) {
        this.g = adVar;
    }

    public String a() {
        return ah.a;
    }

    private String[] b(String str) {
        try {
            return str.split(";");
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    private String a(String[] strArr) {
        if (strArr == null) {
            return null;
        }
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (String append : strArr) {
                stringBuilder.append(append).append(";");
            }
            return stringBuilder.toString();
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public static String a(String str) {
        return a + "='" + at.a(str) + "'";
    }

    public static String c() {
        return c + "=1";
    }
}
