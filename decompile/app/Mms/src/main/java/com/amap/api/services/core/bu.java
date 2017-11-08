package com.amap.api.services.core;

import android.content.ContentValues;
import android.database.Cursor;
import com.amap.api.services.core.ar.a;
import java.util.HashMap;
import java.util.Map;

/* compiled from: SDKEntity */
public class bu implements bk<ar> {
    private static String a = bp.f;
    private static String b = bp.g;
    private static String c = bp.k;
    private static String d = bp.h;
    private static String e = bp.i;
    private static String f = bp.j;
    private ar g = null;

    public /* synthetic */ Object a(Cursor cursor) {
        return b(cursor);
    }

    public /* synthetic */ void a(Object obj) {
        a((ar) obj);
    }

    public ContentValues a() {
        Throwable th;
        ContentValues contentValues;
        try {
            if (this.g == null) {
                return null;
            }
            contentValues = new ContentValues();
            try {
                contentValues.put(a, by.a(this.g.a()));
                contentValues.put(b, by.a(this.g.b()));
                contentValues.put(c, Boolean.valueOf(this.g.e()));
                contentValues.put(d, by.a(this.g.c()));
                contentValues.put(f, by.a(this.g.d()));
                contentValues.put(e, by.a(a(this.g.f())));
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

    public ar b(Cursor cursor) {
        boolean z = true;
        try {
            String b = by.b(cursor.getString(1));
            String b2 = by.b(cursor.getString(2));
            String b3 = by.b(cursor.getString(3));
            String[] b4 = b(by.b(cursor.getString(4)));
            String b5 = by.b(cursor.getString(5));
            if (cursor.getInt(6) == 0) {
                z = false;
            }
            return new a(b, b2, b3).a(z).a(b5).a(b4).a();
        } catch (ai e) {
            e.printStackTrace();
            return null;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public void a(ar arVar) {
        this.g = arVar;
    }

    public String b() {
        return bp.a;
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
        Map hashMap = new HashMap();
        hashMap.put(a, by.a(str));
        return bj.a(hashMap);
    }

    public static String c() {
        return c + "=1";
    }
}
