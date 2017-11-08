package sun.security.ssl;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

/* compiled from: SSLContextImpl */
final class DummyX509KeyManager extends X509ExtendedKeyManager {
    static final X509ExtendedKeyManager INSTANCE = new DummyX509KeyManager();

    private DummyX509KeyManager() {
    }

    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return null;
    }

    public String chooseClientAlias(String[] keyTypes, Principal[] issuers, Socket socket) {
        return null;
    }

    public String chooseEngineClientAlias(String[] keyTypes, Principal[] issuers, SSLEngine engine) {
        return null;
    }

    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return null;
    }

    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return null;
    }

    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return null;
    }

    public X509Certificate[] getCertificateChain(String alias) {
        return null;
    }

    public PrivateKey getPrivateKey(String alias) {
        return null;
    }
}
