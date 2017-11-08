package java.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.util.logging.PlatformLogger;

final class Tripwire {
    static final boolean ENABLED = ((Boolean) AccessController.doPrivileged(new -void__clinit___LambdaImpl0())).booleanValue();
    private static final String TRIPWIRE_PROPERTY = "org.openjdk.java.util.stream.tripwire";

    final /* synthetic */ class -void__clinit___LambdaImpl0 implements PrivilegedAction {
        public Object run() {
            return Boolean.valueOf(Boolean.getBoolean(Tripwire.TRIPWIRE_PROPERTY));
        }
    }

    private Tripwire() {
    }

    static void trip(Class<?> trippingClass, String msg) {
        PlatformLogger.getLogger(trippingClass.getName()).warning(msg, trippingClass.getName());
    }
}
