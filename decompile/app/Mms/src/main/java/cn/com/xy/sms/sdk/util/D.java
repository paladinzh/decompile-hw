package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.dex.DexUtil;
import java.lang.Thread.UncaughtExceptionHandler;

/* compiled from: Unknown */
public final class d implements UncaughtExceptionHandler {
    private static d b;
    private UncaughtExceptionHandler a;

    private static d a() {
        if (b == null) {
            b = new d();
        }
        return b;
    }

    private static boolean a(Throwable th) {
        if (th == null) {
            return false;
        }
        try {
            DexUtil.saveExceptionLog(th);
        } catch (Exception e) {
        }
        return true;
    }

    private void b() {
        this.a = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public final void uncaughtException(Thread thread, Throwable th) {
        a(th);
        this.a.uncaughtException(thread, th);
    }
}
