package tmsdkobf;

import android.content.Context;
import java.util.concurrent.atomic.AtomicReference;
import tmsdkobf.pb.d;

/* compiled from: Unknown */
public class ph implements or {
    static final /* synthetic */ boolean fJ;
    private on Et;
    private boolean HA = false;
    private oj HB;
    private pk HC = pk.gQ();
    private b HD;
    private pj HE;
    private final String TAG = "SharkWharf";

    /* compiled from: Unknown */
    public interface b {
        void a(boolean z, int i, byte[] bArr);
    }

    /* compiled from: Unknown */
    public interface a {
        void a(boolean z, int i, d dVar);
    }

    static {
        boolean z = false;
        if (!ph.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public ph(boolean z, Context context, oq oqVar, boolean z2, b bVar, tmsdkobf.pk.a aVar, tmsdkobf.pb.a aVar2) {
        this.HA = z;
        this.HD = bVar;
        this.Et = oqVar;
        if (this.HA) {
            this.HE = new pj(context, z2, oqVar);
            this.HB = new oj(context, this.HE, oqVar);
            pk.gQ().a(oqVar, oqVar, this.HE, bVar, aVar, this, aVar2);
        }
    }

    public synchronized void a(d dVar, byte[] bArr, a aVar) {
        if (!this.HA) {
            pa.c("ocean", "not in sending process!", null, null);
            throw new RuntimeException("not in sending process!");
        } else if (dVar != null) {
            long j;
            AtomicReference atomicReference;
            int a;
            if (bArr != null) {
                if (bArr.length > 0) {
                    tmsdk.common.utils.d.e("SharkWharf", "sendData() isSharkVip: " + dVar.Ga + " isTcpHello: " + dVar.Gb + " isTcpFirst: " + dVar.Gc + " data.length: " + bArr.length);
                    if (dVar.Ge) {
                        dVar.Gd = true;
                        if (!dVar.gr()) {
                            tmsdk.common.utils.d.e("SharkWharf", "sendData() isTcpVip");
                            this.HC.a(bArr, aVar, dVar);
                        } else if (dVar.gs()) {
                            j = dVar.Gi;
                            tmsdk.common.utils.d.d("SharkWharf", "sendData() tcp通道");
                            this.HC.a(j, dVar.Gb, dVar.Gc, bArr, aVar, dVar);
                        } else {
                            tmsdk.common.utils.d.e("SharkWharf", "sendData() isSharkVip connected");
                            dVar.Gd = true;
                            this.HC.a(bArr, aVar, dVar);
                        }
                    }
                    dVar.Gd = false;
                    aVar.a(false, 0, dVar);
                    atomicReference = new AtomicReference();
                    if (this.HB == null) {
                        a = this.HB.a(bArr, atomicReference);
                        this.Et.as(a);
                        tmsdk.common.utils.d.e("SharkWharf", "onBefore() only http通道 retCode: " + a);
                        this.HD.a(false, a, (byte[]) atomicReference.get());
                        return;
                    }
                    pa.c("SharkWharf", "mHttpNetwork == null. maybe is fore process.", null, null);
                    return;
                }
            }
            tmsdk.common.utils.d.c("SharkWharf", "sendData() data is empty");
            tmsdk.common.utils.d.e("SharkWharf", "sendData() isSharkVip: " + dVar.Ga + " isTcpHello: " + dVar.Gb + " isTcpFirst: " + dVar.Gc);
            if (dVar.Ge) {
                dVar.Gd = false;
                aVar.a(false, 0, dVar);
                atomicReference = new AtomicReference();
                if (this.HB == null) {
                    pa.c("SharkWharf", "mHttpNetwork == null. maybe is fore process.", null, null);
                    return;
                }
                a = this.HB.a(bArr, atomicReference);
                this.Et.as(a);
                tmsdk.common.utils.d.e("SharkWharf", "onBefore() only http通道 retCode: " + a);
                this.HD.a(false, a, (byte[]) atomicReference.get());
                return;
            }
            dVar.Gd = true;
            if (!dVar.gr()) {
                tmsdk.common.utils.d.e("SharkWharf", "sendData() isTcpVip");
                this.HC.a(bArr, aVar, dVar);
            } else if (dVar.gs()) {
                tmsdk.common.utils.d.e("SharkWharf", "sendData() isSharkVip connected");
                dVar.Gd = true;
                this.HC.a(bArr, aVar, dVar);
            } else {
                j = dVar.Gi;
                tmsdk.common.utils.d.d("SharkWharf", "sendData() tcp通道");
                this.HC.a(j, dVar.Gb, dVar.Gc, bArr, aVar, dVar);
            }
        }
    }

    public void a(a aVar, d dVar, int i, byte[] bArr) {
        boolean z = false;
        if (aVar == null || dVar == null) {
            String str = "SharkWharf";
            StringBuilder append = new StringBuilder().append("onSendFailed() beforeSend is null : ").append(aVar == null).append(" sharkSend is null : ");
            if (dVar == null) {
                z = true;
            }
            tmsdk.common.utils.d.c(str, append.append(z).toString());
            return;
        }
        dVar.Gd = false;
        if (dVar.gr()) {
            tmsdk.common.utils.d.e("SharkWharf", "sendData()");
            aVar.a(true, i, dVar);
            this.HD.a(false, -800, null);
            return;
        }
        tmsdk.common.utils.d.d("SharkWharf", "tcp通道发送失败，转http通道");
        aVar.a(true, i, dVar);
        AtomicReference atomicReference = new AtomicReference();
        if (this.HB != null) {
            int a = this.HB.a(bArr, atomicReference);
            tmsdk.common.utils.d.e("SharkWharf", "onSendFailed() http 通道 retCode: " + a);
            this.HD.a(false, a, (byte[]) atomicReference.get());
            return;
        }
        pa.c("SharkWharf", "mHttpNetwork == null. maybe is fore process.", null, null);
    }

    public os gI() {
        return this.HE;
    }

    public pk gl() {
        if (this.HA) {
            return this.HC;
        }
        if (fJ) {
            return this.HC;
        }
        throw new AssertionError("SharkWharf TmsTcpManager is null ");
    }

    public void n(boolean z) {
        if (this.HA && this.HE != null) {
            this.HE.n(z);
        }
    }
}
