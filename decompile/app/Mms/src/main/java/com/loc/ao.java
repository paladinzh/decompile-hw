package com.loc;

import android.content.ContentValues;
import android.database.Cursor;
import java.util.HashMap;
import java.util.Map;

/* compiled from: LogEntity */
public class ao implements ak<ap> {
    private static final String a = am.l;
    private static final String b = am.m;
    private static final String c = am.n;
    private static final String d = am.f;
    private ap e = null;
    private int f;

    public ao(int i) {
        this.f = i;
    }

    public static String a(int i) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(b).append("=").append(i);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String a(String str) {
        Map hashMap = new HashMap();
        hashMap.put(a, str);
        return aj.a(hashMap);
    }

    public ContentValues a() {
        ContentValues contentValues;
        Throwable th;
        try {
            if (this.e == null) {
                return null;
            }
            contentValues = new ContentValues();
            try {
                contentValues.put(a, this.e.b());
                contentValues.put(b, Integer.valueOf(this.e.a()));
                contentValues.put(d, av.a(this.e.c()));
                contentValues.put(c, Integer.valueOf(this.e.d()));
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

    public void a(ap apVar) {
        this.e = apVar;
    }

    public /* synthetic */ void a(Object obj) {
        a((ap) obj);
    }

    public ap b(Cursor cursor) {
        ap apVar;
        Throwable th;
        if (cursor == null) {
            return null;
        }
        try {
            String string = cursor.getString(1);
            int i = cursor.getInt(2);
            String string2 = cursor.getString(4);
            int i2 = cursor.getInt(3);
            apVar = new ap();
            try {
                apVar.a(string);
                apVar.a(i);
                apVar.b(av.b(string2));
                apVar.b(i2);
                return apVar;
            } catch (Throwable th2) {
                th = th2;
                th.printStackTrace();
                return apVar;
            }
        } catch (Throwable th3) {
            th = th3;
            apVar = null;
            th.printStackTrace();
            return apVar;
        }
    }

    public String b() {
        return af.a(this.f);
    }
}
