package com.amap.api.services.core;

import android.content.ContentValues;
import android.database.Cursor;

/* compiled from: UpdateLogEntity */
public class ar implements ap<as> {
    private static final String b = ah.o;
    private static final String c = ah.p;
    private static final String d = ah.q;
    private as a = null;

    public /* synthetic */ Object b(Cursor cursor) {
        return a(cursor);
    }

    public ContentValues b() {
        ContentValues contentValues;
        Throwable th;
        try {
            if (this.a == null) {
                return null;
            }
            contentValues = new ContentValues();
            try {
                contentValues.put(b, Boolean.valueOf(this.a.a()));
                contentValues.put(c, Boolean.valueOf(this.a.b()));
                contentValues.put(d, Boolean.valueOf(this.a.c()));
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

    public as a(Cursor cursor) {
        Throwable th;
        boolean z = true;
        as asVar;
        try {
            boolean z2;
            boolean z3;
            int i = cursor.getInt(1);
            int i2 = cursor.getInt(2);
            int i3 = cursor.getInt(3);
            if (i != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            if (i2 != 0) {
                z3 = true;
            } else {
                z3 = false;
            }
            if (i3 == 0) {
                z = false;
            }
            asVar = new as();
            try {
                asVar.a(z2);
                asVar.c(z);
                asVar.b(z3);
                return asVar;
            } catch (Throwable th2) {
                th = th2;
                th.printStackTrace();
                return asVar;
            }
        } catch (Throwable th3) {
            th = th3;
            asVar = null;
            th.printStackTrace();
            return asVar;
        }
    }

    public void a(as asVar) {
        this.a = asVar;
    }

    public String a() {
        return ah.e;
    }
}
