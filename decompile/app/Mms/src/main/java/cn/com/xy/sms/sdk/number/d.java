package cn.com.xy.sms.sdk.number;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.b;

/* compiled from: Unknown */
public class d extends b {
    private static b b = null;
    private static HandlerThread c = null;
    private static Handler d = null;
    private static boolean e = true;

    private d() {
        this.a = 1200000;
    }

    public static b a() {
        synchronized (d.class) {
            if (b == null) {
                b = new d();
            }
        }
        return b;
    }

    private void g() {
        HandlerThread handlerThread = new HandlerThread("LocationHandlerThread");
        c = handlerThread;
        handlerThread.start();
        d = new e(this, c.getLooper());
    }

    public final void b() {
        try {
            if (d == null) {
                HandlerThread handlerThread = new HandlerThread("LocationHandlerThread");
                c = handlerThread;
                handlerThread.start();
                d = new e(this, c.getLooper());
            }
            DuoquUtils.getSdkDoAction().getLocation(Constant.getContext(), d);
        } catch (Throwable th) {
        }
    }

    public final void c() {
        Thread.currentThread().setPriority(1);
        Process.setThreadPriority(10);
    }
}
