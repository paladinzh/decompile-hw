package sun.net;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;
import sun.security.action.GetIntegerAction;

public final class InetAddressCachePolicy {
    public static final int DEFAULT_POSITIVE = 2;
    public static final int FOREVER = -1;
    public static final int NEVER = 0;
    private static int cachePolicy = 0;
    private static final String cachePolicyProp = "networkaddress.cache.ttl";
    private static final String cachePolicyPropFallback = "sun.net.inetaddr.ttl";
    private static int negativeCachePolicy = 0;
    private static final String negativeCachePolicyProp = "networkaddress.cache.negative.ttl";
    private static final String negativeCachePolicyPropFallback = "sun.net.inetaddr.negative.ttl";
    private static boolean propertyNegativeSet;
    private static boolean propertySet;

    static {
        Integer tmp;
        cachePolicy = -1;
        negativeCachePolicy = 0;
        Integer num = null;
        try {
            num = new Integer((String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return Security.getProperty(InetAddressCachePolicy.cachePolicyProp);
                }
            }));
        } catch (NumberFormatException e) {
        }
        if (num != null) {
            cachePolicy = num.intValue();
            if (cachePolicy < 0) {
                cachePolicy = -1;
            }
            propertySet = true;
            tmp = num;
        } else {
            num = (Integer) AccessController.doPrivileged(new GetIntegerAction(cachePolicyPropFallback));
            if (num != null) {
                cachePolicy = num.intValue();
                if (cachePolicy < 0) {
                    cachePolicy = -1;
                }
                propertySet = true;
                tmp = num;
            } else if (System.getSecurityManager() == null) {
                cachePolicy = 2;
                tmp = num;
            } else {
                tmp = num;
            }
        }
        try {
            num = new Integer((String) AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return Security.getProperty(InetAddressCachePolicy.negativeCachePolicyProp);
                }
            }));
        } catch (NumberFormatException e2) {
            num = tmp;
        }
        if (num != null) {
            negativeCachePolicy = num.intValue();
            if (negativeCachePolicy < 0) {
                negativeCachePolicy = -1;
            }
            propertyNegativeSet = true;
            return;
        }
        num = (Integer) AccessController.doPrivileged(new GetIntegerAction(negativeCachePolicyPropFallback));
        if (num != null) {
            negativeCachePolicy = num.intValue();
            if (negativeCachePolicy < 0) {
                negativeCachePolicy = -1;
            }
            propertyNegativeSet = true;
        }
    }

    public static synchronized int get() {
        int i;
        synchronized (InetAddressCachePolicy.class) {
            i = cachePolicy;
        }
        return i;
    }

    public static synchronized int getNegative() {
        int i;
        synchronized (InetAddressCachePolicy.class) {
            i = negativeCachePolicy;
        }
        return i;
    }

    public static synchronized void setIfNotSet(int newPolicy) {
        synchronized (InetAddressCachePolicy.class) {
            if (!propertySet) {
                checkValue(newPolicy, cachePolicy);
                cachePolicy = newPolicy;
            }
        }
    }

    public static synchronized void setNegativeIfNotSet(int newPolicy) {
        synchronized (InetAddressCachePolicy.class) {
            if (!propertyNegativeSet) {
                negativeCachePolicy = newPolicy;
            }
        }
    }

    private static void checkValue(int newPolicy, int oldPolicy) {
        if (newPolicy != -1) {
            if (oldPolicy == -1 || newPolicy < oldPolicy || newPolicy < -1) {
                throw new SecurityException("can't make InetAddress cache more lax");
            }
        }
    }
}
