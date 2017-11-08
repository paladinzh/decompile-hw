package java.lang;

import java.util.Collection;
import java.util.IdentityHashMap;

class ApplicationShutdownHooks {
    private static IdentityHashMap<Thread, Thread> hooks;

    static {
        try {
            Shutdown.add(1, false, new Runnable() {
                public void run() {
                    ApplicationShutdownHooks.runHooks();
                }
            });
            hooks = new IdentityHashMap();
        } catch (IllegalStateException e) {
            hooks = null;
        }
    }

    private ApplicationShutdownHooks() {
    }

    static synchronized void add(Thread hook) {
        synchronized (ApplicationShutdownHooks.class) {
            if (hooks == null) {
                throw new IllegalStateException("Shutdown in progress");
            } else if (hook.isAlive()) {
                throw new IllegalArgumentException("Hook already running");
            } else if (hooks.containsKey(hook)) {
                throw new IllegalArgumentException("Hook previously registered");
            } else {
                hooks.put(hook, hook);
            }
        }
    }

    static synchronized boolean remove(Thread hook) {
        boolean z;
        synchronized (ApplicationShutdownHooks.class) {
            if (hooks == null) {
                throw new IllegalStateException("Shutdown in progress");
            } else if (hook == null) {
                throw new NullPointerException();
            } else {
                z = hooks.remove(hook) != null;
            }
        }
        return z;
    }

    static void runHooks() {
        synchronized (ApplicationShutdownHooks.class) {
            Collection<Thread> threads = hooks.keySet();
            hooks = null;
        }
        for (Thread hook : threads) {
            hook.start();
        }
        for (Thread hook2 : threads) {
            try {
                hook2.join();
            } catch (InterruptedException e) {
            }
        }
    }
}
