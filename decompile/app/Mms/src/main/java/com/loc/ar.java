package com.loc;

import android.content.ContentValues;
import android.database.Cursor;
import com.loc.v.a;
import java.util.HashMap;
import java.util.Map;

/* compiled from: SDKEntity */
public class ar implements ak<v> {
    private static String a = am.f;
    private static String b = am.g;
    private static String c = am.k;
    private static String d = am.h;
    private static String e = am.i;
    private static String f = am.j;
    private v g = null;

    public static String a(String str) {
        Map hashMap = new HashMap();
        hashMap.put(a, av.a(str));
        return aj.a(hashMap);
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

    private String[] b(String str) {
        try {
            return str.split(";");
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public static String c() {
        return c + "=1";
    }

    public ContentValues a() {
        ContentValues contentValues;
        Throwable th;
        try {
            if (this.g == null) {
                return null;
            }
            contentValues = new ContentValues();
            try {
                contentValues.put(a, av.a(this.g.a()));
                contentValues.put(b, av.a(this.g.b()));
                contentValues.put(c, Boolean.valueOf(this.g.e()));
                contentValues.put(d, av.a(this.g.c()));
                contentValues.put(f, av.a(this.g.d()));
                contentValues.put(e, av.a(a(this.g.f())));
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

    public /* synthetic */ Object a(Cursor cursor) {
        return b(cursor);
    }

    public void a(v vVar) {
        this.g = vVar;
    }

    public /* synthetic */ void a(Object obj) {
        a((v) obj);
    }

    public v b(Cursor cursor) {
        boolean z = true;
        try {
            String b = av.b(cursor.getString(1));
            String b2 = av.b(cursor.getString(2));
            String b3 = av.b(cursor.getString(3));
            String[] b4 = b(av.b(cursor.getString(4)));
            String b5 = av.b(cursor.getString(5));
            if (cursor.getInt(6) == 0) {
                z = false;
            }
            return new a(b, b2, b3).a(z).a(b5).a(b4).a();
        } catch (l e) {
            e.printStackTrace();
            return null;
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public String b() {
        return am.a;
    }
}
