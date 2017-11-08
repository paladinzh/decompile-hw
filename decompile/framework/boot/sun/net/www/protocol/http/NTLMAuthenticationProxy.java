package sun.net.www.protocol.http;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.PasswordAuthentication;
import java.net.URL;

class NTLMAuthenticationProxy {
    private static final String clazzStr = "sun.net.www.protocol.http.ntlm.NTLMAuthentication";
    private static Method isTrustedSite = null;
    private static final String isTrustedSiteStr = "isTrustedSite";
    static final NTLMAuthenticationProxy proxy = tryLoadNTLMAuthentication();
    static final boolean supported;
    private static Method supportsTA = null;
    private static final String supportsTAStr = "supportsTransparentAuth";
    static final boolean supportsTransparentAuth;
    private final Constructor<? extends AuthenticationInfo> fiveArgCtr;
    private final Constructor<? extends AuthenticationInfo> threeArgCtr;

    static {
        boolean z;
        boolean z2 = false;
        if (proxy != null) {
            z = true;
        } else {
            z = false;
        }
        supported = z;
        if (supported) {
            z2 = supportsTransparentAuth();
        }
        supportsTransparentAuth = z2;
    }

    private NTLMAuthenticationProxy(Constructor<? extends AuthenticationInfo> threeArgCtr, Constructor<? extends AuthenticationInfo> fiveArgCtr) {
        this.threeArgCtr = threeArgCtr;
        this.fiveArgCtr = fiveArgCtr;
    }

    AuthenticationInfo create(boolean isProxy, URL url, PasswordAuthentication pw) {
        try {
            return (AuthenticationInfo) this.threeArgCtr.newInstance(Boolean.valueOf(isProxy), url, pw);
        } catch (ReflectiveOperationException roe) {
            finest(roe);
            return null;
        }
    }

    AuthenticationInfo create(boolean isProxy, String host, int port, PasswordAuthentication pw) {
        try {
            return (AuthenticationInfo) this.fiveArgCtr.newInstance(Boolean.valueOf(isProxy), host, Integer.valueOf(port), pw);
        } catch (ReflectiveOperationException roe) {
            finest(roe);
            return null;
        }
    }

    private static boolean supportsTransparentAuth() {
        try {
            return ((Boolean) supportsTA.invoke(null, new Object[0])).booleanValue();
        } catch (ReflectiveOperationException roe) {
            finest(roe);
            return false;
        }
    }

    public static boolean isTrustedSite(URL url) {
        try {
            return ((Boolean) isTrustedSite.invoke(null, url)).booleanValue();
        } catch (ReflectiveOperationException roe) {
            finest(roe);
            return false;
        }
    }

    private static NTLMAuthenticationProxy tryLoadNTLMAuthentication() {
        try {
            Class<? extends AuthenticationInfo> cl = Class.forName(clazzStr, true, null);
            if (cl != null) {
                Constructor<? extends AuthenticationInfo> threeArg = cl.getConstructor(Boolean.TYPE, URL.class, PasswordAuthentication.class);
                Constructor<? extends AuthenticationInfo> fiveArg = cl.getConstructor(Boolean.TYPE, String.class, Integer.TYPE, PasswordAuthentication.class);
                supportsTA = cl.getDeclaredMethod(supportsTAStr, new Class[0]);
                isTrustedSite = cl.getDeclaredMethod(isTrustedSiteStr, URL.class);
                return new NTLMAuthenticationProxy(threeArg, fiveArg);
            }
        } catch (ClassNotFoundException cnfe) {
            finest(cnfe);
        } catch (Object roe) {
            throw new AssertionError(roe);
        }
        return null;
    }

    static void finest(Exception e) {
        HttpURLConnection.getHttpLogger().finest("NTLMAuthenticationProxy: " + e);
    }
}
