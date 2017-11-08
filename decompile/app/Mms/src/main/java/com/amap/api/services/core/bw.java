package com.amap.api.services.core;

import android.content.ContentValues;
import android.database.Cursor;

/* compiled from: UpdateLogEntity */
public class bw implements bk<bx> {
    private static final String b = bp.o;
    private static final String c = bp.p;
    private static final String d = bp.q;
    private bx a = null;

    public /* synthetic */ Object a(Cursor cursor) {
        return b(cursor);
    }

    public /* synthetic */ void a(Object obj) {
        a((bx) obj);
    }

    public ContentValues a() {
        Throwable th;
        ContentValues contentValues;
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

    public bx b(Cursor cursor) {
        bx bxVar;
        Throwable th;
        boolean z = true;
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
            bxVar = new bx();
            try {
                bxVar.a(z2);
                bxVar.c(z);
                bxVar.b(z3);
                return bxVar;
            } catch (Throwable th2) {
                th = th2;
                th.printStackTrace();
                return bxVar;
            }
        } catch (Throwable th3) {
            th = th3;
            bxVar = null;
            th.printStackTrace();
            return bxVar;
        }
    }

    public void a(bx bxVar) {
        this.a = bxVar;
    }

    public String b() {
        return bp.e;
    }
}
