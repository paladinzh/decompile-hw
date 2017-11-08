package tmsdkobf;

import android.content.Context;
import tmsdk.common.utils.d;
import tmsdkobf.po.b;
import tmsdkobf.pv.a;

/* compiled from: Unknown */
public class pw {
    public static String TAG = "TmsTcpNetwork";
    private final int Em = 3;
    private on Et;
    private pj HE = null;
    private int HX = pk.HX;
    private pv IX = new pv();
    b IY = null;
    private volatile boolean IZ = false;
    private po Ja = null;
    private Context context = null;

    public pw(Context context) {
        this.context = context;
    }

    private void gS() {
        if (hy() && this.Ja != null) {
            this.Ja.stop();
        }
    }

    public void a(on onVar) {
        this.Et = onVar;
    }

    public void a(pj pjVar) {
        this.HE = pjVar;
    }

    public void a(px pxVar) {
        this.IX.a((a) pxVar);
    }

    public void a(boolean z, b bVar) {
        this.IZ = z;
        this.IY = bVar;
        if (this.IZ) {
            this.Ja = new po(this.context, bVar);
            this.Ja.cn(this.HX);
            this.Ja.start();
            return;
        }
        if (this.Ja != null) {
            this.Ja.stop();
        }
        this.Ja = null;
    }

    public void close() {
        this.IX.fH();
        gS();
    }

    public void cn(int i) {
        if (!(this.HX == i || this.Ja == null)) {
            this.Ja.cn(i);
        }
    }

    public int gX() {
        if (mu.fb()) {
            d.f(TAG, "reconnect HttpConnection.couldNotConnect()");
            return -230000;
        }
        gS();
        this.HE.gL();
        long currentTimeMillis = System.currentTimeMillis();
        int a = this.IX.a(this.HE);
        this.Et.j(System.currentTimeMillis() - currentTimeMillis);
        a(this.IZ, this.IY);
        return a;
    }

    public boolean hs() {
        return this.IX.hs();
    }

    public boolean hv() {
        return this.IX.hv();
    }

    public int hx() {
        d.d(TAG, "connect");
        if (mu.fb()) {
            d.f(TAG, "connect HttpConnection.couldNotConnect()");
            return -230000;
        }
        long currentTimeMillis = System.currentTimeMillis();
        int a = this.IX.a(this.context, this.HE);
        this.Et.j(System.currentTimeMillis() - currentTimeMillis);
        return a;
    }

    public boolean hy() {
        return this.IZ;
    }

    public void hz() {
        if (hy()) {
            try {
                this.Ja.reset();
            } catch (Throwable th) {
                d.c(TAG, th);
            }
        }
    }

    public int x(byte[] bArr) {
        int i = -900000;
        for (int i2 = 0; i2 < 3; i2++) {
            i = this.IX.x(bArr);
            if (i == 0) {
                break;
            }
            if (2 != i2) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    d.c(TAG, "sendData() InterruptedException e: " + e.toString());
                }
            }
        }
        return i;
    }
}
