package tmsdkobf;

import android.os.Handler;
import android.os.Looper;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;

/* compiled from: Unknown */
public class hz extends pf implements lm {
    public hz(long j) {
        super(j);
    }

    public hz(oq oqVar, final boolean z) {
        super(bI());
        ((pd) ManagerCreatorC.getManager(pd.class)).a(oqVar);
        jq.a((pf) this);
        final boolean equals = TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_IS_TEST).equals("true");
        super.init(true);
        lk.wC = true;
        kr.do().a(ku.dq());
        kr.do().a(kz.dx());
        new Thread(new Runnable(this) {
            final /* synthetic */ hz rA;

            public void run() {
                boolean v = new fv().v();
                pd pdVar = (pd) ManagerCreatorC.getManager(pd.class);
                pdVar.J(equals);
                if (v) {
                    pdVar.onImsiChanged();
                    ku.dq().onImsiChanged();
                }
                pg.gB().a(pdVar.gm());
                if (z) {
                    pg.gB().start();
                }
            }
        }).start();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable(this) {
            final /* synthetic */ hz rA;

            {
                this.rA = r1;
            }

            public void run() {
                kr.do().bb(ku.dq().dl());
            }
        }, 30000);
    }

    private static long bI() {
        return jk.getIdent(0, 4294967296L);
    }

    public static void bJ() {
        if (jq.cu() == null) {
            hz hzVar = new hz(new fz(), fw.w().K().booleanValue());
        }
    }
}
