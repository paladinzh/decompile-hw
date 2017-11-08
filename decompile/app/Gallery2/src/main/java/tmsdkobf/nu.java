package tmsdkobf;

import android.content.Context;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import tmsdk.common.module.aresengine.SmsEntity;
import tmsdk.common.module.optimus.BsFakeType;
import tmsdk.common.module.optimus.Optimus;
import tmsdk.common.module.optimus.SMSCheckerResult;
import tmsdk.common.module.optimus.impl.bean.BsCloudResult;
import tmsdk.common.module.optimus.impl.bean.BsInput;
import tmsdk.common.module.optimus.impl.bean.BsResult;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;

/* compiled from: Unknown */
public class nu implements tmsdkobf.ny.a {
    private static nu Dl = null;
    private String Dm;
    public Optimus Dn = new Optimus();
    private ny Do;
    private nx Dp = new nx();
    private Context mContext;

    /* compiled from: Unknown */
    public abstract class a {
        final /* synthetic */ nu Dq;
        public BsCloudResult Ds;
        protected CountDownLatch Dt;

        public a(nu nuVar, CountDownLatch countDownLatch) {
            this.Dq = nuVar;
            this.Dt = countDownLatch;
        }

        public abstract void a(BsCloudResult bsCloudResult);
    }

    /* compiled from: Unknown */
    class b {
        final /* synthetic */ nu Dq;
        BsInput Du;
        int Dv = 0;

        b(nu nuVar) {
            this.Dq = nuVar;
        }
    }

    private nu(Context context) {
        this.mContext = context.getApplicationContext();
        this.Dm = ms.a(context, "fake_bs.dat", null);
    }

    private BsCloudResult a(b bVar) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        a anonymousClass1 = new a(this, countDownLatch) {
            final /* synthetic */ nu Dq;

            public void a(BsCloudResult bsCloudResult) {
                d.e("QQPimSecure", "[Optimus]:checkFakeBsWithCloudSync has result =" + bsCloudResult);
                this.Ds = bsCloudResult;
                this.Dt.countDown();
            }
        };
        d.e("QQPimSecure", "[Optimus]:checkFakeBsWithCloudSync start");
        a(bVar.Du, anonymousClass1);
        try {
            countDownLatch.await(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        d.e("QQPimSecure", "[Optimus]:checkFakeBsWithCloudSync timeout or notifyed");
        return anonymousClass1.Ds;
    }

    private void a(BsInput bsInput, final a aVar) {
        fs obVar = new ob();
        obVar.DU = bsInput.sms;
        obVar.DT = bsInput.sender;
        obVar.DS = oa.t(this.Dn.getBsInfos(bsInput));
        jq.cu().a(812, obVar, new og(), 0, new lg(this) {
            final /* synthetic */ nu Dq;

            public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
                this.Dq.Dp.cL("云端检测结果:retCode=" + i3 + ",dataRetCode=" + i4 + ",resp=" + (fsVar != null ? fsVar.toString() : "null"));
                try {
                    og ogVar = (og) fsVar;
                    if (ogVar != null) {
                        if (ogVar.Ed != null) {
                            BsCloudResult a = oa.a(ogVar.Ed);
                            this.Dq.Dn.setBlackWhiteItems(oa.u(ogVar.Ee), oa.u(ogVar.Ef));
                            aVar.a(a);
                            return;
                        }
                    }
                    aVar.a(null);
                } catch (Throwable th) {
                    aVar.a(null);
                }
            }
        }, 3000);
    }

    public static nu q(Context context) {
        if (Dl == null) {
            synchronized (nu.class) {
                if (Dl == null) {
                    Dl = new nu(context);
                }
            }
        }
        return Dl;
    }

    public void a(BsInput bsInput) {
        BsResult bsResult = new BsResult();
        this.Dn.check(bsInput, bsResult);
        if (BsFakeType.FAKE == bsResult.fakeType) {
            nz.fD().s(System.currentTimeMillis());
            this.Dp.a("", "", "", bsInput.neighbors == null ? "" : bsInput.neighbors.toString(), this.Dn.getUploadInfo(), true, false);
        }
    }

    public SMSCheckerResult b(SmsEntity smsEntity, boolean z) {
        SMSCheckerResult sMSCheckerResult = new SMSCheckerResult();
        if (smsEntity == null) {
            return sMSCheckerResult;
        }
        BsInput bsInput = this.Do == null ? new BsInput() : this.Do.fC();
        bsInput.sender = smsEntity.phonenum;
        bsInput.sms = smsEntity.body;
        BsResult bsResult = new BsResult();
        this.Dn.check(bsInput, bsResult);
        this.Dp.cL("|本地检测结果=" + bsResult.toString());
        sMSCheckerResult.mType = bsResult.fakeType;
        if (z && f.iu() && !f.iv()) {
            b bVar = new b(this);
            bVar.Du = bsInput;
            this.Dp.fy();
            BsCloudResult a = a(bVar);
            if (a != null) {
                this.Dn.checkWithCloud(bsInput, a, bsResult);
                sMSCheckerResult.isCloudCheck = true;
            }
        }
        this.Dp.cL("|最终的检测结果=" + bsResult.toString());
        if (BsFakeType.FAKE == bsResult.fakeType) {
            nz.fD().r(System.currentTimeMillis());
            this.Dp.a("", smsEntity.phonenum, smsEntity.body, bsInput.neighbors == null ? "" : bsInput.neighbors.toString(), this.Dn.getUploadInfo(), false, sMSCheckerResult.isCloudCheck);
        }
        sMSCheckerResult.mType = bsResult.fakeType;
        return sMSCheckerResult;
    }

    public boolean start() {
        if (!this.Dn.init(this.Dm, null)) {
            return false;
        }
        this.Dp.init();
        this.Do = new ny(this.Dp);
        this.Do.a((tmsdkobf.ny.a) this);
        this.Do.r(this.mContext);
        return true;
    }

    public void stop() {
        this.Dn.finish();
        if (this.Do != null) {
            this.Do.s(this.mContext);
            this.Do.a(null);
        }
        if (this.Dp != null) {
            this.Dp.destroy();
        }
        synchronized (nu.class) {
            Dl = null;
        }
    }
}
