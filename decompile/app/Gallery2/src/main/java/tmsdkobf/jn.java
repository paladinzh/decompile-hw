package tmsdkobf;

import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdkobf.ps.a;

/* compiled from: Unknown */
public class jn {
    private static volatile boolean tZ = false;
    private static a ua = null;

    public static void reportChannelInfo() {
        if (!tZ) {
            tZ = true;
            final nc ncVar = new nc("tms");
            if (!ncVar.getBoolean("reportlc", false)) {
                jq.ct().a(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (((qt) ManagerCreatorC.getManager(qt.class)).ib() == 0) {
                            if (jn.ua != null) {
                                ps.t(TMSDKContext.getApplicaionContext()).c(jn.ua);
                            }
                            ncVar.a("reportlc", true, true);
                        } else if (jn.ua == null) {
                            jn.ua = new a(this) {
                                final /* synthetic */ AnonymousClass1 uc;

                                {
                                    this.uc = r1;
                                }

                                public void cn() {
                                }

                                public void co() {
                                    jn.reportChannelInfo();
                                }
                            };
                            ps.t(TMSDKContext.getApplicaionContext()).b(jn.ua);
                        }
                        jn.tZ = false;
                    }
                }, "reportChannelInfoThread");
            }
        }
    }
}
