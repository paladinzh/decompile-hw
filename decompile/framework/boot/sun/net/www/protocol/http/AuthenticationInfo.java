package sun.net.www.protocol.http;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.AccessController;
import java.util.HashMap;
import sun.net.www.HeaderParser;
import sun.net.www.protocol.http.AuthCacheValue.Type;
import sun.security.action.GetBooleanAction;

public abstract class AuthenticationInfo extends AuthCacheValue implements Cloneable {
    static final /* synthetic */ boolean -assertionsDisabled;
    public static final char PROXY_AUTHENTICATION = 'p';
    public static final char SERVER_AUTHENTICATION = 's';
    private static HashMap<String, Thread> requests = new HashMap();
    static boolean serializeAuth = ((Boolean) AccessController.doPrivileged(new GetBooleanAction("http.auth.serializeRequests"))).booleanValue();
    AuthScheme authScheme;
    String host;
    String path;
    int port;
    String protocol;
    protected transient PasswordAuthentication pw;
    String realm;
    String s1;
    String s2;
    char type;

    public abstract String getHeaderValue(URL url, String str);

    public abstract boolean isAuthorizationStale(String str);

    public abstract boolean setHeaders(HttpURLConnection httpURLConnection, HeaderParser headerParser, String str);

    public abstract boolean supportsPreemptiveAuthorization();

    static {
        boolean z;
        if (AuthenticationInfo.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    public PasswordAuthentication credentials() {
        return this.pw;
    }

    public Type getAuthType() {
        if (this.type == SERVER_AUTHENTICATION) {
            return Type.Server;
        }
        return Type.Proxy;
    }

    AuthScheme getAuthScheme() {
        return this.authScheme;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getRealm() {
        return this.realm;
    }

    public String getPath() {
        return this.path;
    }

    public String getProtocolScheme() {
        return this.protocol;
    }

    private static boolean requestIsInProgress(String key) {
        if (!serializeAuth) {
            return false;
        }
        synchronized (requests) {
            Thread c = Thread.currentThread();
            Thread t = (Thread) requests.get(key);
            if (t == null) {
                requests.put(key, c);
                return false;
            } else if (t == c) {
                return false;
            } else {
                while (requests.containsKey(key)) {
                    try {
                        requests.wait();
                    } catch (InterruptedException e) {
                    }
                }
                return true;
            }
        }
    }

    private static void requestCompleted(String key) {
        synchronized (requests) {
            Thread thread = (Thread) requests.get(key);
            if (thread != null && thread == Thread.currentThread()) {
                boolean waspresent = requests.remove(key) != null;
                if (!(-assertionsDisabled || waspresent)) {
                    throw new AssertionError();
                }
            }
            requests.notifyAll();
        }
    }

    public AuthenticationInfo(char type, AuthScheme authScheme, String host, int port, String realm) {
        this.type = type;
        this.authScheme = authScheme;
        this.protocol = "";
        this.host = host.toLowerCase();
        this.port = port;
        this.realm = realm;
        this.path = null;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public AuthenticationInfo(char type, AuthScheme authScheme, URL url, String realm) {
        this.type = type;
        this.authScheme = authScheme;
        this.protocol = url.getProtocol().toLowerCase();
        this.host = url.getHost().toLowerCase();
        this.port = url.getPort();
        if (this.port == -1) {
            this.port = url.getDefaultPort();
        }
        this.realm = realm;
        String urlPath = url.getPath();
        if (urlPath.length() == 0) {
            this.path = urlPath;
        } else {
            this.path = reducePath(urlPath);
        }
    }

    static String reducePath(String urlPath) {
        int sepIndex = urlPath.lastIndexOf(47);
        int targetSuffixIndex = urlPath.lastIndexOf(46);
        if (sepIndex == -1 || sepIndex >= targetSuffixIndex) {
            return urlPath;
        }
        return urlPath.substring(0, sepIndex + 1);
    }

    static AuthenticationInfo getServerAuth(URL url) {
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        return getAuth("s:" + url.getProtocol().toLowerCase() + ":" + url.getHost().toLowerCase() + ":" + port, url);
    }

    static String getServerAuthKey(URL url, String realm, AuthScheme scheme) {
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        return "s:" + scheme + ":" + url.getProtocol().toLowerCase() + ":" + url.getHost().toLowerCase() + ":" + port + ":" + realm;
    }

    static AuthenticationInfo getServerAuth(String key) {
        AuthenticationInfo cached = getAuth(key, null);
        if (cached == null && requestIsInProgress(key)) {
            return getAuth(key, null);
        }
        return cached;
    }

    static AuthenticationInfo getAuth(String key, URL url) {
        if (url == null) {
            return (AuthenticationInfo) cache.get(key, null);
        }
        return (AuthenticationInfo) cache.get(key, url.getPath());
    }

    static AuthenticationInfo getProxyAuth(String host, int port) {
        return (AuthenticationInfo) cache.get("p::" + host.toLowerCase() + ":" + port, null);
    }

    static String getProxyAuthKey(String host, int port, String realm, AuthScheme scheme) {
        return "p:" + scheme + "::" + host.toLowerCase() + ":" + port + ":" + realm;
    }

    static AuthenticationInfo getProxyAuth(String key) {
        AuthenticationInfo cached = (AuthenticationInfo) cache.get(key, null);
        if (cached == null && requestIsInProgress(key)) {
            return (AuthenticationInfo) cache.get(key, null);
        }
        return cached;
    }

    void addToCache() {
        String key = cacheKey(true);
        cache.put(key, this);
        if (supportsPreemptiveAuthorization()) {
            cache.put(cacheKey(false), this);
        }
        endAuthRequest(key);
    }

    static void endAuthRequest(String key) {
        if (serializeAuth) {
            synchronized (requests) {
                requestCompleted(key);
            }
        }
    }

    void removeFromCache() {
        cache.remove(cacheKey(true), this);
        if (supportsPreemptiveAuthorization()) {
            cache.remove(cacheKey(false), this);
        }
    }

    public String getHeaderName() {
        if (this.type == SERVER_AUTHENTICATION) {
            return "Authorization";
        }
        return "Proxy-authorization";
    }

    String cacheKey(boolean includeRealm) {
        if (includeRealm) {
            return this.type + ":" + this.authScheme + ":" + this.protocol + ":" + this.host + ":" + this.port + ":" + this.realm;
        }
        return this.type + ":" + this.protocol + ":" + this.host + ":" + this.port;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.pw = new PasswordAuthentication(this.s1, this.s2.toCharArray());
        this.s1 = null;
        this.s2 = null;
    }

    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        this.s1 = this.pw.getUserName();
        this.s2 = new String(this.pw.getPassword());
        s.defaultWriteObject();
    }
}
