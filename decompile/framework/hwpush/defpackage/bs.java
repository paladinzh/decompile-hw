package defpackage;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/* renamed from: bs */
class bs extends SSLSocketFactory {
    private SSLSocketFactory cm;

    public bs(SSLSocketFactory sSLSocketFactory) {
        this.cm = sSLSocketFactory;
    }

    private void d(Socket socket) {
        aw.d("PushLog2841", "enter setEnableSafeCipherSuites");
        if (socket instanceof SSLSocket) {
            SSLSocket sSLSocket = (SSLSocket) socket;
            String[] enabledCipherSuites = sSLSocket.getEnabledCipherSuites();
            if (enabledCipherSuites == null || enabledCipherSuites.length == 0) {
                aw.w("PushLog2841", "Current enabled cipherSuites is invalid!");
                return;
            }
            List arrayList = new ArrayList();
            for (String str : enabledCipherSuites) {
                if (!str.contains("RC4")) {
                    arrayList.add(str);
                }
            }
            sSLSocket.setEnabledCipherSuites((String[]) arrayList.toArray(new String[arrayList.size()]));
            return;
        }
        aw.e("PushLog2841", "socket is not instanceof SSLSocket");
    }

    public Socket createSocket() {
        Socket createSocket = this.cm.createSocket();
        d(createSocket);
        return createSocket;
    }

    public Socket createSocket(String str, int i) {
        Socket createSocket = this.cm.createSocket(str, i);
        d(createSocket);
        return createSocket;
    }

    public Socket createSocket(String str, int i, InetAddress inetAddress, int i2) {
        Socket createSocket = this.cm.createSocket(str, i, inetAddress, i2);
        d(createSocket);
        return createSocket;
    }

    public Socket createSocket(InetAddress inetAddress, int i) {
        Socket createSocket = this.cm.createSocket(inetAddress, i);
        d(createSocket);
        return createSocket;
    }

    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) {
        Socket createSocket = this.cm.createSocket(inetAddress, i, inetAddress2, i2);
        d(createSocket);
        return createSocket;
    }

    public Socket createSocket(Socket socket, String str, int i, boolean z) {
        Socket createSocket = this.cm.createSocket(socket, str, i, z);
        d(createSocket);
        return createSocket;
    }

    public String[] getDefaultCipherSuites() {
        return this.cm.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return this.cm.getSupportedCipherSuites();
    }
}
