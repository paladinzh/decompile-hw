package com.huawei.hwid.core.b.b;

import android.content.Context;
import android.os.Build.VERSION;
import com.huawei.hwid.core.d.b.e;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import org.apache.http.conn.ssl.SSLSocketFactory;

class g extends SSLSocketFactory {
    private static Object b = new Object();
    SSLContext a = SSLContext.getInstance("TLS");

    public g(KeyStore keyStore, Context context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        super(keyStore);
        this.a.init(null, null, null);
    }

    public Socket createSocket(Socket socket, String str, int i, boolean z) throws IOException, UnknownHostException {
        Socket createSocket = this.a.getSocketFactory().createSocket(socket, str, i, z);
        if (createSocket instanceof SSLSocket) {
            SSLSocket sSLSocket = (SSLSocket) createSocket;
        } else {
            createSocket = null;
        }
        e.b("MySSLSocketFactory", "host =" + str + "port=" + i + "autoclouse=" + z);
        b.a((SSLSocket) createSocket);
        e.a(createSocket);
        if (str.contains("hicloud.com")) {
            if (VERSION.SDK_INT >= 22) {
                getHostnameVerifier().verify(str, createSocket);
            } else {
                synchronized (b) {
                    getHostnameVerifier().verify(str, createSocket);
                }
            }
        }
        return createSocket;
    }

    public Socket createSocket() throws IOException {
        Socket createSocket = this.a.getSocketFactory().createSocket();
        if (createSocket instanceof SSLSocket) {
            SSLSocket sSLSocket = (SSLSocket) createSocket;
        } else {
            createSocket = null;
        }
        e.a((SSLSocket) createSocket);
        e.a(createSocket);
        return createSocket;
    }
}
