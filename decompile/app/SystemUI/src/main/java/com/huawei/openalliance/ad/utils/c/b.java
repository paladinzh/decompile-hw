package com.huawei.openalliance.ad.utils.c;

import android.os.Build.VERSION;
import com.huawei.openalliance.ad.utils.b.d;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import org.apache.http.conn.ssl.SSLSocketFactory;

/* compiled from: Unknown */
public class b extends SSLSocketFactory {
    SSLContext a = SSLContext.getInstance("TLS");

    public b(KeyStore keyStore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(keyStore);
        this.a.init(null, null, new SecureRandom());
    }

    public static void a(SSLSocket sSLSocket) {
        int i = 0;
        if (sSLSocket != null) {
            String[] enabledCipherSuites = sSLSocket.getEnabledCipherSuites();
            List arrayList = new ArrayList();
            int length = enabledCipherSuites.length;
            while (i < length) {
                String str = enabledCipherSuites[i];
                if (!(str.contains("aNull") || str.contains("eNull") || str.contains("LOW") || str.contains("MD5") || str.contains("EXP") || str.contains("SRP") || str.contains("DSS") || str.contains("PSK") || str.contains("RC4") || str.contains("DES"))) {
                    arrayList.add(str);
                }
                i++;
            }
            sSLSocket.setEnabledCipherSuites((String[]) arrayList.toArray(new String[arrayList.size()]));
            return;
        }
        d.c("SNSSSL", "socket param is null.");
    }

    static void b(SSLSocket sSLSocket) {
        if (sSLSocket != null && VERSION.SDK_INT >= 16) {
            sSLSocket.setEnabledProtocols(new String[]{"TLSv1.1", "TLSv1.2"});
        }
    }

    public Socket createSocket() throws IOException {
        Socket createSocket = this.a.getSocketFactory().createSocket();
        if (createSocket instanceof SSLSocket) {
            SSLSocket sSLSocket = (SSLSocket) createSocket;
        } else {
            createSocket = null;
        }
        a(createSocket);
        b(createSocket);
        return createSocket;
    }

    public Socket createSocket(Socket socket, String str, int i, boolean z) throws IOException, UnknownHostException {
        Socket createSocket = this.a.getSocketFactory().createSocket(socket, str, i, z);
        if (createSocket instanceof SSLSocket) {
            SSLSocket sSLSocket = (SSLSocket) createSocket;
        } else {
            createSocket = null;
        }
        a(createSocket);
        b(createSocket);
        return createSocket;
    }
}
