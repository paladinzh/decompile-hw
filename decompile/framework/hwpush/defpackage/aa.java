package defpackage;

import android.content.Context;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;

/* renamed from: aa */
public class aa implements IPushChannel {
    private static byte[] aA;
    private static byte[] aB;
    private Socket aC;
    private boolean isInitialized = false;
    private Context mContext;

    public aa(Context context) {
        this.mContext = context;
    }

    private static void a(InputStream inputStream, byte[] bArr) {
        int i = 0;
        while (i < bArr.length) {
            int read = inputStream.read(bArr, i, bArr.length - i);
            if (-1 == read) {
                throw new IOException("read -1 reached");
            }
            i += read;
        }
    }

    private boolean b(Socket socket) {
        if (socket == null) {
            aw.w("PushLog2841", "socket is null");
            return false;
        } else if (socket.isConnected()) {
            return true;
        } else {
            aw.w("PushLog2841", "when init Channel, socket is not ready");
            return false;
        }
    }

    public static byte[] b(byte[] bArr) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(48);
        byte[] b = bj.b(bArr, aB);
        if (b == null) {
            aw.d("PushLog2841", "aes encrypt pushMsgData error");
            return null;
        }
        byteArrayOutputStream.write(au.c((b.length + 1) + 2));
        byteArrayOutputStream.write(b);
        return byteArrayOutputStream.toByteArray();
    }

    private static void c(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            aw.d("PushLog2841", "key is null");
            return;
        }
        aA = new byte[bArr.length];
        System.arraycopy(bArr, 0, aA, 0, bArr.length);
    }

    private static void d(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            aw.d("PushLog2841", "key is null");
            return;
        }
        aB = new byte[bArr.length];
        System.arraycopy(bArr, 0, aB, 0, bArr.length);
    }

    private byte[] d(InputStream inputStream) {
        aa.a(inputStream, new byte[2]);
        byte[] bArr = new byte[1];
        aa.a(inputStream, bArr);
        byte b = bArr[0];
        aw.d("PushLog2841", "result is " + b);
        if (b == (byte) 0) {
            bArr = new byte[32];
            aa.a(inputStream, bArr);
            return bArr;
        }
        aw.w("PushLog2841", "secure key exchange error");
        return null;
    }

    private static byte[] e(InputStream inputStream) {
        byte[] bArr = new byte[2];
        aa.a(inputStream, bArr);
        bArr = new byte[(au.g(bArr) - 3)];
        aa.a(inputStream, bArr);
        return bArr;
    }

    private byte[] k(Context context) {
        byte ak = (byte) ae.l(context).ak();
        String al = ae.l(context).al();
        byte[] bArr = new byte[16];
        new SecureRandom().nextBytes(bArr);
        aw.d("PushLog2841", "ready to send SecureChannelReqMessage, save clientKey for decode serverKey");
        aa.c(bArr);
        byte[] a = bj.a(bArr, al);
        if (a == null) {
            aw.w("PushLog2841", "rsa encrypr clientKey error");
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(20);
        byteArrayOutputStream.write(au.c(((a.length + 1) + 1) + 2));
        byteArrayOutputStream.write(ak);
        byteArrayOutputStream.write(a);
        return byteArrayOutputStream.toByteArray();
    }

    public synchronized boolean a(Socket socket) {
        boolean z = false;
        synchronized (this) {
            if (b(socket)) {
                this.aC = socket;
                try {
                    byte[] k = k(this.mContext);
                    OutputStream outputStream = this.aC.getOutputStream();
                    if (outputStream == null) {
                        aw.w("PushLog2841", "outputStream is null");
                    } else if (k == null) {
                        aw.w("PushLog2841", "data is null");
                    } else {
                        outputStream.write(k);
                        outputStream.flush();
                        InputStream inputStream = this.aC.getInputStream();
                        if (b(socket)) {
                            int read = inputStream.read();
                            if (-1 == read) {
                                aw.i("PushLog2841", " read -1 when init secure channel, socket maybe closed");
                            } else if (21 == read) {
                                k = d(inputStream);
                                if (k != null) {
                                    aa.d(bj.c(k, aA));
                                    this.isInitialized = true;
                                    aw.i("PushLog2841", "CustSecureChannel isInitialized success!");
                                    z = true;
                                } else {
                                    aw.i("PushLog2841", "get server key error");
                                }
                            } else {
                                aw.i("PushLog2841", "cmdId is not CMD_SECUREKEYEXCHANGE_RSP");
                            }
                        }
                    }
                } catch (Throwable e) {
                    aw.d("PushLog2841", "call send cause:" + e.toString(), e);
                }
                close();
            } else {
                close();
            }
        }
        return z;
    }

    public synchronized boolean a(byte[] bArr) {
        boolean z = false;
        synchronized (this) {
            if (this.aC == null) {
                aw.w("PushLog2841", "socket is null");
            } else if (this.isInitialized) {
                try {
                    byte[] b = aa.b(bArr);
                    OutputStream outputStream = this.aC.getOutputStream();
                    if (outputStream == null) {
                        aw.w("PushLog2841", "outputStream is null");
                    } else if (b == null) {
                        aw.w("PushLog2841", "data is null");
                    } else {
                        outputStream.write(b);
                        outputStream.flush();
                        z = true;
                    }
                } catch (Throwable e) {
                    aw.d("PushLog2841", "call send cause:" + e.toString(), e);
                    close();
                }
            } else {
                aw.w("PushLog2841", "secure socket is not initialized, can not write any data");
                close();
            }
        }
        return z;
    }

    public ChannelType br() {
        return ChannelType.ChannelType_Secure;
    }

    public synchronized void close() {
        aw.d("PushLog2841", "enter pushChannel:close()");
        this.isInitialized = false;
        try {
            if (this.aC == null || this.aC.isClosed()) {
                aw.w("PushLog2841", "socket has been closed");
            } else {
                this.aC.close();
            }
            this.aC = null;
        } catch (Throwable e) {
            aw.d("PushLog2841", "close socket error: " + e.toString(), e);
            this.aC = null;
        } catch (Throwable th) {
            this.aC = null;
        }
    }

    public InputStream getInputStream() {
        try {
            if (this.aC != null) {
                return new ab(this, this.aC.getInputStream());
            }
            aw.w("PushLog2841", "socket is null");
            return null;
        } catch (Throwable e) {
            aw.d("PushLog2841", "call socket.getInputStream cause:" + e.toString(), e);
        }
    }

    public Socket getSocket() {
        return this.aC;
    }

    public boolean hasConnection() {
        if (this.aC != null) {
            return this.aC.isConnected();
        }
        aw.w("PushLog2841", "socket is null");
        return false;
    }
}
