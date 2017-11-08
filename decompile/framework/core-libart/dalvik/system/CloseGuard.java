package dalvik.system;

public final class CloseGuard {
    private static volatile boolean ENABLED = true;
    private static final CloseGuard NOOP = new CloseGuard();
    private static volatile Reporter REPORTER = new DefaultReporter();
    private Throwable allocationSite;

    public interface Reporter {
        void report(String str, Throwable th);
    }

    private static final class DefaultReporter implements Reporter {
        private DefaultReporter() {
        }

        public void report(String message, Throwable allocationSite) {
            System.logW(message, allocationSite);
        }
    }

    public static CloseGuard get() {
        if (ENABLED) {
            return new CloseGuard();
        }
        return NOOP;
    }

    public static void setEnabled(boolean enabled) {
        ENABLED = enabled;
    }

    public static void setReporter(Reporter reporter) {
        if (reporter == null) {
            throw new NullPointerException("reporter == null");
        }
        REPORTER = reporter;
    }

    public static Reporter getReporter() {
        return REPORTER;
    }

    private CloseGuard() {
    }

    public void open(String closer) {
        if (closer == null) {
            throw new NullPointerException("closer == null");
        } else if (this != NOOP && ENABLED) {
            this.allocationSite = new Throwable("Explicit termination method '" + closer + "' not called");
        }
    }

    public void close() {
        this.allocationSite = null;
    }

    public void warnIfOpen() {
        if (this.allocationSite != null && ENABLED) {
            REPORTER.report("A resource was acquired at attached stack trace but never released. See java.io.Closeable for information on avoiding resource leaks.", this.allocationSite);
        }
    }
}
