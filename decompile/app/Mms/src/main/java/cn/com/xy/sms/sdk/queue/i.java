package cn.com.xy.sms.sdk.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/* compiled from: Unknown */
public final class i {
    public static BlockingQueue<k> a = new LinkedBlockingQueue();
    public static int b = 10;
    private static Thread c = null;

    public static synchronized void a() {
        synchronized (i.class) {
            if (c == null) {
                Thread jVar = new j();
                c = jVar;
                jVar.start();
            }
        }
    }

    public static void a(k kVar) {
        try {
            a.put(kVar);
        } catch (Throwable th) {
        }
    }
}
