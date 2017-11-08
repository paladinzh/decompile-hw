package com.loc;

import android.content.Context;
import java.lang.Thread.UncaughtExceptionHandler;

/* compiled from: BasicLogHandler */
public class aa {
    protected static aa a;
    protected UncaughtExceptionHandler b;
    protected boolean c = true;

    public static void a(Throwable th, String str, String str2) {
        th.printStackTrace();
        if (a != null) {
            a.a(th, 1, str, str2);
        }
    }

    protected void a(Context context, v vVar, boolean z) {
    }

    protected void a(v vVar, String str) {
    }

    protected void a(Throwable th, int i, String str, String str2) {
    }
}
