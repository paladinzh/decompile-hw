package defpackage;

import android.content.Context;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Arrays;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/* renamed from: ad */
public class ad implements IPushChannel {
    private SSLSocket aK = null;
    private InputStream aL;
    private OutputStream aM;
    private Context mContext;

    public ad(Context context) {
        this.mContext = context;
    }

    public boolean a(Socket socket) {
        if (socket == null || !socket.isConnected()) {
            aw.e("PushLog2841", "when init SSL Channel, socket is not ready:" + socket);
            return false;
        }
        aw.d("PushLog2841", "enter SSLChannel:init(" + socket.getRemoteSocketAddress() + ")");
        SSLContext instance = SSLContext.getInstance("TLS");
        TrustManagerFactory instance2 = TrustManagerFactory.getInstance("X509");
        KeyStore instance3 = KeyStore.getInstance("BKS");
        InputStream byteArrayInputStream = new ByteArrayInputStream(br.cn());
        byteArrayInputStream.reset();
        instance3.load(byteArrayInputStream, bj.decrypter(ax.bO()).toCharArray());
        byteArrayInputStream.close();
        instance2.init(instance3);
        instance.init(null, instance2.getTrustManagers(), null);
        InetAddress inetAddress = socket.getInetAddress();
        if (inetAddress == null) {
            return false;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, socket.getPort());
        this.aK = (SSLSocket) instance.getSocketFactory().createSocket(socket, inetAddress.getHostAddress(), socket.getPort(), true);
        if (this.aK == null) {
            return false;
        }
        this.aK.setEnabledCipherSuites(ay.bP());
        aw.d("PushLog2841", "server ip:" + inetSocketAddress.getAddress().getHostAddress() + ",server port:" + inetSocketAddress.getPort() + ",socket ip:" + this.aK.getLocalAddress().getHostAddress() + ",socket port:" + this.aK.getLocalPort() + ",pkgName:" + this.mContext.getPackageName());
        this.aL = this.aK.getInputStream();
        this.aM = this.aK.getOutputStream();
        this.aK.setSoTimeout(0);
        return true;
    }

    public boolean a(byte[] bArr) {
        try {
            if (this.aM == null || bArr == null) {
                aw.e("PushLog2841", "when send msg:" + Arrays.toString(bArr) + " dos is null, or msg is null");
                return false;
            }
            this.aM.write(bArr);
            this.aM.flush();
            return true;
        } catch (Throwable e) {
            aw.d("PushLog2841", "call send cause:" + e.toString(), e);
            close();
        }
    }

    public ChannelType br() {
        return ChannelType.ChannelType_SSL;
    }

    public synchronized void close() {
        aw.d("PushLog2841", "enter SSLPushChannel:close()");
        try {
            if (this.aL != null) {
                this.aL.close();
            }
            this.aL = null;
        } catch (Throwable e) {
            aw.d("PushLog2841", "close dis error: " + e.toString(), e);
            this.aL = null;
        } catch (Throwable th) {
            this.aL = null;
        }
        try {
            if (this.aM != null) {
                this.aM.close();
            }
            this.aM = null;
        } catch (Throwable e2) {
            aw.d("PushLog2841", "close dos error: " + e2.toString(), e2);
            this.aM = null;
        } catch (Throwable th2) {
            this.aM = null;
        }
        try {
            if (!(this.aK == null || this.aK.isClosed())) {
                this.aK.close();
            }
            this.aK = null;
        } catch (Throwable e22) {
            aw.d("PushLog2841", "close socket error: " + e22.toString(), e22);
            this.aK = null;
        } catch (Throwable th3) {
            this.aK = null;
        }
    }

    public InputStream getInputStream() {
        return this.aL;
    }

    public Socket getSocket() {
        return this.aK;
    }

    public boolean hasConnection() {
        return (this.aK == null || !this.aK.isConnected() || this.aL == null || this.aM == null) ? false : true;
    }
}
