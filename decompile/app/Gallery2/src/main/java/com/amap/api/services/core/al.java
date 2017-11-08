package com.amap.api.services.core;

import android.content.ContentValues;
import android.database.Cursor;

/* compiled from: LogEntity */
public abstract class al implements ap<am> {
    private static final String a = ah.l;
    private static final String b = ah.m;
    private static final String c = ah.n;
    private static final String d = ah.f;
    private am e = null;

    public /* synthetic */ Object b(Cursor cursor) {
        return a(cursor);
    }

    public ContentValues b() {
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
                contentValues.put(d, at.a(this.e.c()));
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

    public am a(Cursor cursor) {
        Throwable th;
        if (cursor == null) {
            return null;
        }
        am amVar;
        try {
            String string = cursor.getString(1);
            int i = cursor.getInt(2);
            String string2 = cursor.getString(4);
            int i2 = cursor.getInt(3);
            amVar = new am();
            try {
                amVar.a(string);
                amVar.a(i);
                amVar.b(at.b(string2));
                amVar.b(i2);
                return amVar;
            } catch (Throwable th2) {
                th = th2;
                th.printStackTrace();
                return amVar;
            }
        } catch (Throwable th3) {
            th = th3;
            amVar = null;
            th.printStackTrace();
            return amVar;
        }
    }

    public void a(am amVar) {
        this.e = amVar;
    }

    public static String a(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(a).append("='").append(str).append("'");
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return stringBuilder.toString();
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
