package tmsdkobf;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: Unknown */
public class kc implements ThreadFactory, ki {
    private final ThreadGroup vi = new ThreadGroup("TMS-COMMON");
    private final AtomicInteger vj = new AtomicInteger(1);
    private final String vk = ("Common Thread Pool-" + vI.getAndIncrement() + "-Thread-");

    kc() {
    }

    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(this.vi, runnable, this.vk + this.vj.getAndIncrement(), 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != 5) {
            thread.setPriority(5);
        }
        return thread;
    }
}
