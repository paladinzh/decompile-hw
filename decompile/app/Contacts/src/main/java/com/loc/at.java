package com.loc;

import android.content.ContentValues;
import android.database.Cursor;

/* compiled from: UpdateLogEntity */
public class at implements ak<au> {
    private static final String b = am.o;
    private static final String c = am.p;
    private static final String d = am.q;
    private au a = null;

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

    public /* synthetic */ Object a(Cursor cursor) {
        return b(cursor);
    }

    public void a(au auVar) {
        this.a = auVar;
    }

    public /* synthetic */ void a(Object obj) {
        a((au) obj);
    }

    public au b(Cursor cursor) {
        au auVar;
        Throwable th;
        boolean z = true;
        try {
            int i = cursor.getInt(1);
            int i2 = cursor.getInt(2);
            int i3 = cursor.getInt(3);
            boolean z2 = i != 0;
            boolean z3 = i2 != 0;
            if (i3 == 0) {
                z = false;
            }
            auVar = new au();
            try {
                auVar.a(z2);
                auVar.c(z);
                auVar.b(z3);
                return auVar;
            } catch (Throwable th2) {
                th = th2;
                th.printStackTrace();
                return auVar;
            }
        } catch (Throwable th3) {
            th = th3;
            auVar = null;
            th.printStackTrace();
            return auVar;
        }
    }

    public String b() {
        return am.e;
    }
}
