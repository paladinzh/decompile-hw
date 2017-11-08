package java.lang;

public class Shutdown {
    private static final int FINALIZERS = 2;
    private static final int HOOKS = 1;
    private static final int MAX_SYSTEM_HOOKS = 10;
    private static final int RUNNING = 0;
    private static int currentRunningHook = 0;
    private static Object haltLock = new Lock();
    private static final Runnable[] hooks = new Runnable[10];
    private static Object lock = new Lock();
    private static boolean runFinalizersOnExit = false;
    private static int state = 0;

    private static class Lock {
        private Lock() {
        }
    }

    static native void halt0(int i);

    private static native void runAllFinalizers();

    static void setRunFinalizersOnExit(boolean run) {
        synchronized (lock) {
            runFinalizersOnExit = run;
        }
    }

    public static void add(int slot, boolean registerShutdownInProgress, Runnable hook) {
        synchronized (lock) {
            if (hooks[slot] != null) {
                throw new InternalError("Shutdown hook at slot " + slot + " already registered");
            }
            if (registerShutdownInProgress) {
                if (state > 1 || (state == 1 && slot <= currentRunningHook)) {
                    throw new IllegalStateException("Shutdown in progress");
                }
            } else if (state > 0) {
                throw new IllegalStateException("Shutdown in progress");
            }
            hooks[slot] = hook;
        }
    }

    private static void runHooks() {
        for (int i = 0; i < 10; i++) {
            try {
                Runnable hook;
                synchronized (lock) {
                    currentRunningHook = i;
                    hook = hooks[i];
                }
                if (hook != null) {
                    hook.run();
                }
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath threadDeath = (ThreadDeath) t;
                }
            }
        }
    }

    static void halt(int status) {
        synchronized (haltLock) {
            halt0(status);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void sequence() {
        synchronized (lock) {
            if (state != 1) {
            }
        }
    }

    static void exit(int status) {
        boolean runMoreFinalizers = false;
        synchronized (lock) {
            if (status != 0) {
                runFinalizersOnExit = false;
            }
            switch (state) {
                case 0:
                    state = 1;
                    break;
                case 2:
                    if (status == 0) {
                        runMoreFinalizers = runFinalizersOnExit;
                        break;
                    } else {
                        halt(status);
                        break;
                    }
            }
        }
        if (runMoreFinalizers) {
            runAllFinalizers();
            halt(status);
        }
        synchronized (Shutdown.class) {
            sequence();
            halt(status);
        }
    }

    static void shutdown() {
        synchronized (lock) {
            switch (state) {
                case 0:
                    state = 1;
                    break;
            }
        }
        synchronized (Shutdown.class) {
            sequence();
        }
    }
}
