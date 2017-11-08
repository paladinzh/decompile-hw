package sun.security.ssl;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;
import java.security.Principal;
import java.security.PrivilegedAction;
import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

public final class Krb5Helper {
    private static final String IMPL_CLASS = "sun.security.ssl.krb5.Krb5ProxyImpl";
    private static final Krb5Proxy proxy = ((Krb5Proxy) AccessController.doPrivileged(new PrivilegedAction<Krb5Proxy>() {
        public Krb5Proxy run() {
            try {
                return (Krb5Proxy) Class.forName(Krb5Helper.IMPL_CLASS, true, null).newInstance();
            } catch (ClassNotFoundException e) {
                return null;
            } catch (Object e2) {
                throw new AssertionError(e2);
            } catch (Object e3) {
                throw new AssertionError(e3);
            }
        }
    }));

    private Krb5Helper() {
    }

    public static boolean isAvailable() {
        return proxy != null;
    }

    private static void ensureAvailable() {
        if (proxy == null) {
            throw new AssertionError((Object) "Kerberos should have been available");
        }
    }

    public static Subject getClientSubject(AccessControlContext acc) throws LoginException {
        ensureAvailable();
        return proxy.getClientSubject(acc);
    }

    public static Subject getServerSubject(AccessControlContext acc) throws LoginException {
        ensureAvailable();
        return proxy.getServerSubject(acc);
    }

    public static SecretKey[] getServerKeys(AccessControlContext acc) throws LoginException {
        ensureAvailable();
        return proxy.getServerKeys(acc);
    }

    public static String getServerPrincipalName(SecretKey kerberosKey) {
        ensureAvailable();
        return proxy.getServerPrincipalName(kerberosKey);
    }

    public static String getPrincipalHostName(Principal principal) {
        ensureAvailable();
        return proxy.getPrincipalHostName(principal);
    }

    public static Permission getServicePermission(String principalName, String action) {
        ensureAvailable();
        return proxy.getServicePermission(principalName, action);
    }
}
