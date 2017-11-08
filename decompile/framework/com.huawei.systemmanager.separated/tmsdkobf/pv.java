package tmsdkobf;

import android.content.Context;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import tmsdk.common.ErrorCode;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;
import tmsdkobf.pr.b;

/* compiled from: Unknown */
public class pv {
    static final /* synthetic */ boolean fJ;
    private byte IJ;
    private boolean IK;
    private boolean IL;
    private Thread IM;
    private Object IN;
    private Socket IO;
    private DataOutputStream IP;
    private DataInputStream IQ;
    protected pp IR;
    private b IS;
    private a IT;
    private boolean IU;
    private Handler IV;
    private Context mContext;

    /* compiled from: Unknown */
    public interface a {
        void a(int i, Object obj);

        void b(int i, byte[] bArr);
    }

    static {
        boolean z = false;
        if (!pv.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public pv() {
        this((byte) 0, false);
    }

    public pv(byte b, boolean z) {
        this.IJ = (byte) 0;
        this.IK = true;
        this.IL = true;
        this.IN = new Object();
        this.IU = false;
        this.IV = new Handler(this, Looper.getMainLooper()) {
            final /* synthetic */ pv IW;

            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        this.IW.IU = false;
                        return;
                    default:
                        return;
                }
            }
        };
        this.IJ = (byte) b;
        this.IK = z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized int a(Context context, pp ppVar, boolean z) {
        d.e("TcpNetwork", "start() isRestart " + z);
        if (isStarted()) {
            d.e("TcpNetwork", "start() isStarted() " + isStarted());
            return 0;
        } else if (ppVar != null) {
            this.mContext = context;
            if (hv()) {
                this.IR = ppVar;
                if (this.IT != null) {
                    this.IT.a(3, null);
                }
                int b = b(this.IR);
                if (b == 0) {
                    this.IL = false;
                    if (this.IJ == (byte) 0) {
                        d.e("TcpNetwork", "start() startRcvThread()");
                        ho();
                    }
                    if (this.IT != null) {
                        if (z) {
                            this.IT.a(5, null);
                        } else {
                            this.IT.a(4, null);
                        }
                    }
                } else {
                    d.e("TcpNetwork", "start() checkSocket() !ret");
                    return b;
                }
            }
            d.e("TcpNetwork", "start() !NetworkUtil.isNetworkConnected()");
            return -220000;
        } else {
            d.e("TcpNetwork", "start() null == ipPlot");
            return -240000;
        }
    }

    private boolean a(b bVar) throws IOException {
        if (!hr()) {
            hq();
        }
        this.IS = bVar;
        this.IO = a(InetAddress.getByName(bVar.fZ()), bVar.getPort());
        if (this.IO == null) {
            return false;
        }
        switch (this.IJ) {
            case (byte) 0:
                this.IP = new DataOutputStream(this.IO.getOutputStream());
                this.IQ = new DataInputStream(this.IO.getInputStream());
                break;
            case (byte) 1:
                this.IO.setSoTimeout(60000);
                break;
        }
        return hs();
    }

    public static byte[] a(InputStream inputStream, int i, int i2, tmsdkobf.pr.a aVar) throws IOException {
        byte[] bArr = new byte[i2];
        int i3 = 0;
        int i4 = i2;
        while (i3 < i2 && i4 > 0) {
            int read = inputStream.read(bArr, i, i4);
            if (read >= 0) {
                i3 += read;
                i += read;
                i4 -= read;
                if (aVar != null) {
                    aVar.a(true, i3, i2);
                }
            } else if (aVar != null) {
                aVar.a(true, i3, i2);
            }
        }
        return i3 == i2 ? bArr : null;
    }

    private int b(pp ppVar) {
        boolean z = false;
        d.e("TcpNetwork", "checkSocketWithRetry()");
        ppVar.gN();
        int gO = ppVar.gO() * ppVar.gP();
        int i = 0;
        int i2 = 0;
        while (i < gO) {
            b gM = ppVar.gM();
            if (gM != null) {
                pu.a(new pu());
                long currentTimeMillis = System.currentTimeMillis();
                i2 = b(gM);
                pu.hm().IF = System.currentTimeMillis() - currentTimeMillis;
                pu.hm().AR = i2;
                pu.hm().v = f.A(this.mContext);
                pu.hm().ja = gM.fZ();
                pu.hm().port = gM.getPort();
                if (i != 0) {
                    pu.hm().IH = true;
                }
                pu.hm().hn();
                pu.release();
                pa.a("TcpNetwork", "checkSocketWithRetry() ipPoint " + gM.toString() + " localIp " + ht() + " localPort " + hu() + " success ? " + i2, null, null);
                if (i2 == 0) {
                    break;
                }
                ppVar.gL();
                i++;
            } else {
                d.e("TcpNetwork", "checkSocketWithRetry() getPlotIPPoint() is null");
                return -240000;
            }
        }
        if (i2 == 0) {
            z = true;
        }
        ppVar.K(z);
        return i2;
    }

    private int b(b bVar) {
        int i = -900000;
        d.e("TcpNetwork", "checkSocket()");
        if (bVar == null) {
            return -240000;
        }
        if (hs()) {
            return 0;
        }
        try {
            if (a(bVar)) {
                i = 0;
            }
        } catch (UnknownHostException e) {
            i = -70000;
            mj.bB(-10010);
            d.e("TcpNetwork", "checkSocket() UnknownHostException " + e.toString());
            if (this.IT != null) {
                this.IT.a(7, bVar);
            }
        } catch (SocketTimeoutException e2) {
            i = -130000;
            mj.bB(-10011);
            d.e("TcpNetwork", "checkSocket() SocketTimeoutException " + e2.toString());
            if (this.IT != null) {
                this.IT.a(8, bVar);
            }
        } catch (Throwable th) {
            pu.hm().IG = th.toString();
            mj.bB(-10012);
            d.e("TcpNetwork", "checkSocket() Throwable " + th.toString());
            if (this.IT != null) {
                this.IT.a(9, bVar);
            }
        }
        return i;
    }

    private void b(int i, byte[] bArr) {
        if (this.IT != null) {
            try {
                this.IT.b(i, bArr);
            } catch (Throwable th) {
                mj.bB(-10016);
                pa.c("ocean", "[ocean]ERR: " + th.toString(), null, null);
                d.c("TcpNetwork", "recv() handleData() Throwable " + th.toString());
                this.IT.a(6, null);
            }
        }
    }

    private synchronized boolean d(boolean z, boolean z2) {
        d.e("TcpNetwork", "stop() bySvr " + z + " isRestart " + z2);
        if (!z) {
            this.IU = true;
        }
        this.IL = true;
        if (!hq()) {
            return false;
        }
        if (this.IT != null) {
            if (z) {
                this.IT.a(0, null);
            } else if (z2) {
                this.IT.a(2, null);
            } else {
                this.IT.a(1, null);
            }
        }
        d.e("TcpNetwork", "stop() bySvr " + z + " isRestart " + z2 + " stop() done");
        return true;
    }

    private void ho() {
        this.IM = new Thread(this, "RcvThread") {
            final /* synthetic */ pv IW;

            public void run() {
                d.e("TcpNetwork", "RcvThread start...");
                this.IW.hp();
                d.e("TcpNetwork", "RcvThread stop...");
            }
        };
        this.IM.setPriority(10);
        this.IM.start();
    }

    private void hp() {
        d.e("TcpNetwork", "recv start...");
        while (!this.IL) {
            try {
                if (!fJ) {
                    if (this.IQ == null) {
                        throw new AssertionError("null != mSocketReader");
                    }
                }
                int readInt = !this.IK ? 0 : this.IQ.readInt();
                int readInt2 = this.IQ.readInt();
                if (!fJ && readInt2 < 0) {
                    throw new AssertionError("recv() size < 4");
                } else if (readInt2 < 1000000) {
                    byte[] a = a(this.IQ, 0, readInt2, null);
                    if (a == null) {
                        d.e("TcpNetwork", "recv(), respData == null");
                    }
                    b(readInt, a);
                } else {
                    d.c("TcpNetwork", "包有误，数据过大，size >= 1000000");
                    return;
                }
            } catch (SocketException e) {
                d.c("TcpNetwork", "recv() SocketException " + e.toString());
                if (this.IU) {
                    d.e("TcpNetwork", "ignore stop exption");
                    this.IL = true;
                } else {
                    d(true, false);
                    if (this.IT != null) {
                        this.IT.a(10, null);
                    }
                }
            } catch (EOFException e2) {
                d.c("TcpNetwork", "recv() EOFException " + e2.toString());
                if (this.IU) {
                    d.e("TcpNetwork", "ignore stop exption");
                    this.IL = true;
                } else {
                    d(true, false);
                    if (this.IT != null) {
                        this.IT.a(11, null);
                    }
                }
            } catch (Throwable th) {
                d.c("TcpNetwork", "recv() Throwable " + th.toString());
                if (this.IU) {
                    d.e("TcpNetwork", "ignore stop exption");
                    this.IL = true;
                } else {
                    d(true, false);
                    if (this.IT != null) {
                        this.IT.a(12, null);
                    }
                }
            }
        }
        if (!this.IU) {
            fH();
        }
        this.IU = false;
        d.e("TcpNetwork", "recv stop...");
    }

    private boolean hq() {
        boolean z;
        pa.h("TcpNetwork", "stop socket");
        if (hr()) {
            pa.h("TcpNetwork", "stop socket success:true");
            return true;
        }
        if (!this.IO.isInputShutdown()) {
            try {
                this.IO.shutdownInput();
            } catch (Throwable th) {
                mj.bB(ErrorCode.ERR_CORRECTION_PROFILE_UPLOAD_FAIL);
                d.e("TcpNetwork", "stopSocket() mSocket.shutdownInput() " + th);
            }
        }
        try {
            this.IQ.close();
        } catch (Throwable th2) {
            mj.bB(ErrorCode.ERR_CORRECTION_LOCAL_TEMPLATE_UNMATCH);
            d.e("TcpNetwork", th2);
        }
        if (!this.IO.isOutputShutdown()) {
            try {
                this.IO.shutdownOutput();
            } catch (Throwable th22) {
                mj.bB(ErrorCode.ERR_CORRECTION_LOCAL_NO_TEMPLATE);
                d.e("TcpNetwork", "stopSocket() mSocket.shutdownOutput() " + th22);
            }
        }
        try {
            this.IP.close();
        } catch (Throwable th222) {
            mj.bB(ErrorCode.ERR_CORRECTION_PROFILE_ILLEGAL);
            d.e("TcpNetwork", "stopSocket() mSocketWriter.close() " + th222);
        }
        try {
            this.IO.close();
            synchronized (this.IN) {
                this.IO = null;
            }
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            mj.bB(-10008);
            e.printStackTrace();
            d.e("TcpNetwork", "stopSocket() InterruptedException " + e);
        } catch (IOException e2) {
            mj.bB(-10009);
            d.e("TcpNetwork", "stopSocket() mSocket.close() " + e2);
            z = false;
            pa.h("TcpNetwork", "stop socket success:" + z);
            return z;
        } catch (Throwable th2222) {
            d.e("TcpNetwork", "stopSocket() mSocket.close() " + th2222);
            z = false;
            pa.h("TcpNetwork", "stop socket success:" + z);
            return z;
        }
        z = true;
        pa.h("TcpNetwork", "stop socket success:" + z);
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean hr() {
        boolean z = true;
        synchronized (this.IN) {
            if (this.IO != null) {
                if (this.IO != null) {
                    if (!this.IO.isClosed()) {
                    }
                }
                z = false;
            } else {
                return true;
            }
        }
    }

    private NetworkInfo hw() {
        try {
            return TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            mj.bB(-10017);
            d.f("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            return null;
        }
    }

    private int y(byte[] bArr) {
        if (!fJ && this.IJ != (byte) 0) {
            throw new AssertionError();
        } else if (!fJ && this.IP == null) {
            throw new AssertionError("mSocketWriter is null");
        } else {
            try {
                synchronized (this.IO) {
                    if (hs()) {
                        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
                        dataOutputStream.writeInt(bArr.length);
                        dataOutputStream.write(bArr);
                        byte[] toByteArray = byteArrayOutputStream.toByteArray();
                        d.e("TcpNetwork", "sendDataInAsync() realSendData.lenght " + toByteArray.length);
                        this.IP.write(toByteArray);
                        this.IP.flush();
                        return 0;
                    }
                    return -180000;
                }
            } catch (SocketException e) {
                mj.bB(-10013);
                d.c("TcpNetwork", "sendDataInAsync() has a Throwable when sendDataInAsync() e " + e.toString());
                return -120000;
            } catch (Throwable th) {
                mj.bB(-10014);
                d.c("TcpNetwork", "sendDataInAsync() has a Throwable when sendDataInAsync() t " + th.toString());
                return -150000;
            }
        }
    }

    private int z(byte[] bArr) {
        if (fJ || (byte) 1 == this.IJ) {
            try {
                this.IP.writeInt(bArr.length);
                this.IP.write(bArr);
                this.IP.flush();
                return 0;
            } catch (Throwable th) {
                mj.bB(-10015);
                d.c("TcpNetwork", "sendDataInSync() has a Throwable when sendDataInsync() " + th.toString());
                return -150000;
            }
        }
        throw new AssertionError();
    }

    public int a(Context context, pp ppVar) {
        return a(context, ppVar, false);
    }

    protected int a(pp ppVar) {
        if (!d(false, true)) {
            return -210000;
        }
        if (this.mContext == null) {
            d.d("TmsTcpManager", "context == null，无法start TcpNetwork");
        }
        return a(this.mContext, ppVar, true);
    }

    public Socket a(InetAddress inetAddress, int i) throws IOException {
        d.d("MMConnectionManager", "acquireSocketWithTimeOut, addr: " + inetAddress + ", port: " + i);
        Socket socket = new Socket();
        socket.setSoLinger(false, 0);
        socket.connect(new InetSocketAddress(inetAddress, i), 60000);
        return socket;
    }

    public void a(a aVar) {
        this.IT = aVar;
    }

    public boolean fH() {
        return d(false, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean hs() {
        boolean z = false;
        synchronized (this.IN) {
            if (this.IO == null) {
                return false;
            } else if (!hr() && this.IO.isConnected()) {
                z = true;
            }
        }
    }

    public String ht() {
        return this.IO != null ? this.IO.getLocalAddress().toString() : "null";
    }

    public int hu() {
        return this.IO != null ? this.IO.getLocalPort() : 0;
    }

    public boolean hv() {
        NetworkInfo hw = hw();
        return hw != null ? hw.isConnected() : false;
    }

    public boolean isStarted() {
        return !this.IL;
    }

    public int x(byte[] bArr) {
        if (hr()) {
            return -190000;
        }
        if (!hs()) {
            return -180000;
        }
        int i = -900000;
        switch (this.IJ) {
            case (byte) 0:
                i = y(bArr);
                break;
            case (byte) 1:
                i = z(bArr);
                break;
        }
        return i;
    }
}
