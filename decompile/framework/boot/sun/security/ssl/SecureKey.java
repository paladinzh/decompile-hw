package sun.security.ssl;

/* compiled from: SSLSessionImpl */
class SecureKey {
    private static Object nullObject = new Object();
    private Object appKey;
    private Object securityCtx = getCurrentSecurityContext();

    static Object getCurrentSecurityContext() {
        SecurityManager sm = System.getSecurityManager();
        Object context = null;
        if (sm != null) {
            context = sm.getSecurityContext();
        }
        if (context == null) {
            return nullObject;
        }
        return context;
    }

    SecureKey(Object key) {
        this.appKey = key;
    }

    Object getAppKey() {
        return this.appKey;
    }

    Object getSecurityContext() {
        return this.securityCtx;
    }

    public int hashCode() {
        return this.appKey.hashCode() ^ this.securityCtx.hashCode();
    }

    public boolean equals(Object o) {
        if ((o instanceof SecureKey) && ((SecureKey) o).appKey.equals(this.appKey)) {
            return ((SecureKey) o).securityCtx.equals(this.securityCtx);
        }
        return false;
    }
}
