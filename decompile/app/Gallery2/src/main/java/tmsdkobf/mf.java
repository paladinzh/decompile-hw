package tmsdkobf;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.UpdateManager;

/* compiled from: Unknown */
public class mf {
    public static void eL() {
        if (fw.w().L().booleanValue()) {
            final UpdateManager updateManager = (UpdateManager) ManagerCreatorC.getManager(UpdateManager.class);
            ((lq) fe.ad(4)).a(new Runnable() {
                public void run() {
                    updateManager.check(290984034304L, new ICheckListener(this) {
                        final /* synthetic */ AnonymousClass1 AG;

                        {
                            this.AG = r1;
                        }

                        public void onCheckCanceled() {
                        }

                        public void onCheckEvent(int i) {
                        }

                        public void onCheckFinished(CheckResult checkResult) {
                            if (checkResult != null) {
                                updateManager.update(checkResult.mUpdateInfoList, null);
                            }
                        }

                        public void onCheckStarted() {
                        }
                    });
                }
            }, "checkUpdate");
            fw.w().k(Boolean.valueOf(false));
        }
    }
}
