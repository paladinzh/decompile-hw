package tmsdkobf;

import android.os.Debug;
import android.os.HandlerThread;
import java.util.HashMap;
import tmsdkobf.qg.a;
import tmsdkobf.qg.c;

/* compiled from: Unknown */
public class kf {
    private static HashMap<Thread, c> vD = new HashMap();
    private static a vE;
    private static kg.a vF = new kg.a() {
        public void a(Thread thread, Runnable runnable) {
            c cVar = new c();
            cVar.Jk = 3;
            cVar.dI = ((qf) thread).bI();
            cVar.name = thread.getName();
            cVar.priority = thread.getPriority();
            cVar.Jm = -1;
            cVar.Jn = -1;
            kf.vD.put(thread, cVar);
            kf.de();
            kf.vE.a(cVar, kf.activeCount());
        }

        public void b(Thread thread, Runnable runnable) {
            c cVar = (c) kf.vD.remove(thread);
            if (cVar != null) {
                cVar.Jm = System.currentTimeMillis() - cVar.Jm;
                cVar.Jn = Debug.threadCpuTimeNanos() - cVar.Jn;
                kf.de();
                kf.vE.b(cVar);
            }
        }

        public void beforeExecute(Thread thread, Runnable runnable) {
            c cVar = (c) kf.vD.get(thread);
            if (cVar != null) {
                kf.de();
                kf.vE.a(cVar);
                cVar.Jm = System.currentTimeMillis();
                cVar.Jn = Debug.threadCpuTimeNanos();
            }
        }
    };

    public static HandlerThread a(String str, int i, long j) {
        return new qf(str, i, j);
    }

    public static int activeCount() {
        return vD.size();
    }

    private static void de() {
        if (vE == null) {
            vE = ke.cZ();
        }
    }

    public static kg.a df() {
        return vF;
    }
}
