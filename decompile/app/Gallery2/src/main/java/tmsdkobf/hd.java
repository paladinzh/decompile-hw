package tmsdkobf;

import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdkobf.hc.a;

/* compiled from: Unknown */
public class hd extends gz {
    hb pT = new hb();
    hc pU = new hc();
    gp pV = new gp();
    boolean qc = false;
    String[] qq;
    ha qt = new ha();
    boolean qu = false;

    public hd(final int i) {
        super(i);
        this.pU.a(new a(this) {
            final /* synthetic */ hd qv;

            {
                this.qv = r1;
            }

            public void a(int i, List<String> list, boolean z, long j, String str, String str2, String str3) {
                this.qv.a(i, list, z, j, str, str2, str3);
            }
        });
        this.qt.a(this.pU);
        this.qt.a(this.pT);
        this.qt.a(new ha.a(this) {
            final /* synthetic */ hd qv;

            public void a(int i, int i2, String str) {
                if (this.qv.pP != null) {
                    this.qv.pP.a(i, i2, str);
                }
            }

            public void bn() {
                if (this.qv.pP != null) {
                    this.qv.pP.aC(i);
                }
            }
        });
        this.qt.a(this.pV);
    }

    public void a(String[] strArr) {
        this.qq = strArr;
        this.pT.a(this.qq);
    }

    void bi() {
    }

    void bj() {
        this.qu = true;
    }

    void bk() {
        if (this.qu) {
            this.pT.a(TMSDKContext.getApplicaionContext());
            this.pV.init();
            this.qt.r(this.qc);
            this.qt.bk();
        }
    }

    void bl() {
        this.qt.bm();
    }

    public void onDestroy() {
        if (this.qu) {
            this.qt.bm();
        }
    }

    public void t(boolean z) {
        this.qc = z;
        be();
    }
}
