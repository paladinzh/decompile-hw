package sun.net.www.protocol.http;

import java.io.Serializable;
import java.net.PasswordAuthentication;

public abstract class AuthCacheValue implements Serializable {
    protected static AuthCache cache = new AuthCacheImpl();

    public enum Type {
        Proxy,
        Server
    }

    abstract PasswordAuthentication credentials();

    abstract AuthScheme getAuthScheme();

    abstract Type getAuthType();

    abstract String getHost();

    abstract String getPath();

    abstract int getPort();

    abstract String getProtocolScheme();

    abstract String getRealm();

    public static void setAuthCache(AuthCache map) {
        cache = map;
    }

    AuthCacheValue() {
    }
}
