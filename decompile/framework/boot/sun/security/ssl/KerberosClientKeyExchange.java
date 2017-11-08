package sun.security.ssl;

import java.io.IOException;
import java.io.PrintStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import javax.crypto.SecretKey;

public class KerberosClientKeyExchange extends HandshakeMessage {
    private static final String IMPL_CLASS = "sun.security.ssl.krb5.KerberosClientKeyExchangeImpl";
    private static final Class<?> implClass = ((Class) AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
        public Class<?> run() {
            try {
                return Class.forName(KerberosClientKeyExchange.IMPL_CLASS, true, null);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }));
    private final KerberosClientKeyExchange impl = createImpl();

    private KerberosClientKeyExchange createImpl() {
        if (getClass() != KerberosClientKeyExchange.class) {
            return null;
        }
        try {
            return (KerberosClientKeyExchange) implClass.newInstance();
        } catch (Object e) {
            throw new AssertionError(e);
        } catch (Object e2) {
            throw new AssertionError(e2);
        }
    }

    public KerberosClientKeyExchange(String serverName, boolean isLoopback, AccessControlContext acc, ProtocolVersion protocolVersion, SecureRandom rand) throws IOException {
        if (this.impl != null) {
            init(serverName, isLoopback, acc, protocolVersion, rand);
            return;
        }
        throw new IllegalStateException("Kerberos is unavailable");
    }

    public KerberosClientKeyExchange(ProtocolVersion protocolVersion, ProtocolVersion clientVersion, SecureRandom rand, HandshakeInStream input, SecretKey[] serverKeys) throws IOException {
        if (this.impl != null) {
            init(protocolVersion, clientVersion, rand, input, serverKeys);
            return;
        }
        throw new IllegalStateException("Kerberos is unavailable");
    }

    int messageType() {
        return 16;
    }

    public int messageLength() {
        return this.impl.messageLength();
    }

    public void send(HandshakeOutStream s) throws IOException {
        this.impl.send(s);
    }

    public void print(PrintStream p) throws IOException {
        this.impl.print(p);
    }

    public void init(String serverName, boolean isLoopback, AccessControlContext acc, ProtocolVersion protocolVersion, SecureRandom rand) throws IOException {
        if (this.impl != null) {
            this.impl.init(serverName, isLoopback, acc, protocolVersion, rand);
        }
    }

    public void init(ProtocolVersion protocolVersion, ProtocolVersion clientVersion, SecureRandom rand, HandshakeInStream input, SecretKey[] serverKeys) throws IOException {
        if (this.impl != null) {
            this.impl.init(protocolVersion, clientVersion, rand, input, serverKeys);
        }
    }

    public byte[] getUnencryptedPreMasterSecret() {
        return this.impl.getUnencryptedPreMasterSecret();
    }

    public Principal getPeerPrincipal() {
        return this.impl.getPeerPrincipal();
    }

    public Principal getLocalPrincipal() {
        return this.impl.getLocalPrincipal();
    }
}
