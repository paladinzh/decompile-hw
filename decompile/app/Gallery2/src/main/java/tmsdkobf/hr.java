package tmsdkobf;

import java.lang.ref.WeakReference;

/* compiled from: Unknown */
public class hr {
    private static Object lock = new Object();
    private static hr qT;

    public static hr by() {
        if (qT == null) {
            synchronized (lock) {
                if (qT == null) {
                    qT = new hr();
                }
            }
        }
        return qT;
    }

    public WeakReference<ll> b(int i, int i2, int i3, long j, long j2, int i4, fs fsVar, byte[] bArr, fs fsVar2, int i5, lg lgVar, lh lhVar, long j3, long j4) {
        return hv.bG().b(i, i2, i3, j, j2, i4, fsVar, bArr, fsVar2, i5, lgVar, lhVar, j3, j4);
    }
}
