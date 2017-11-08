package com.amap.api.services.core;

import android.content.ContentValues;
import android.database.Cursor;
import java.util.HashMap;
import java.util.Map;

/* compiled from: LogEntity */
public abstract class br implements bk<bs> {
    private static final String a = bp.l;
    private static final String b = bp.m;
    private static final String c = bp.n;
    private static final String d = bp.f;
    private bs e = null;

    public /* synthetic */ Object a(Cursor cursor) {
        return b(cursor);
    }

    public /* synthetic */ void a(Object obj) {
        a((bs) obj);
    }

    public ContentValues a() {
        Throwable th;
        ContentValues contentValues;
        try {
            if (this.e == null) {
                return null;
            }
            contentValues = new ContentValues();
            try {
                contentValues.put(a, this.e.b());
                contentValues.put(b, Integer.valueOf(this.e.a()));
                contentValues.put(d, by.a(this.e.c()));
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

    public bs b(Cursor cursor) {
        bs bsVar;
        Throwable th;
        if (cursor == null) {
            return null;
        }
        try {
            String string = cursor.getString(1);
            int i = cursor.getInt(2);
            String string2 = cursor.getString(4);
            int i2 = cursor.getInt(3);
            bsVar = new bs();
            try {
                bsVar.a(string);
                bsVar.a(i);
                bsVar.b(by.b(string2));
                bsVar.b(i2);
                return bsVar;
            } catch (Throwable th2) {
                th = th2;
                th.printStackTrace();
                return bsVar;
            }
        } catch (Throwable th3) {
            th = th3;
            bsVar = null;
            th.printStackTrace();
            return bsVar;
        }
    }

    public void a(bs bsVar) {
        this.e = bsVar;
    }

    public static String a(String str) {
        Map hashMap = new HashMap();
        hashMap.put(a, str);
        return bj.a(hashMap);
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
}
