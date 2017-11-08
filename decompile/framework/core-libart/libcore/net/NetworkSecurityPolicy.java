package libcore.net;

public abstract class NetworkSecurityPolicy {
    private static volatile NetworkSecurityPolicy instance = new DefaultNetworkSecurityPolicy();

    public static final class DefaultNetworkSecurityPolicy extends NetworkSecurityPolicy {
        public boolean isCleartextTrafficPermitted() {
            return true;
        }

        public boolean isCleartextTrafficPermitted(String hostname) {
            return isCleartextTrafficPermitted();
        }
    }

    public abstract boolean isCleartextTrafficPermitted();

    public abstract boolean isCleartextTrafficPermitted(String str);

    public static NetworkSecurityPolicy getInstance() {
        return instance;
    }

    public static void setInstance(NetworkSecurityPolicy policy) {
        if (policy == null) {
            throw new NullPointerException("policy == null");
        }
        instance = policy;
    }
}
