package tmsdkobf;

import android.os.IBinder;

/* compiled from: Unknown */
final class pz {
    private static int Jc = 41;
    private static int Jd = 43;
    private IBinder mBinder;

    public pz() {
        if (ng.cK("android.app.admin.IDevicePolicyManager$Stub")) {
            Jc = ng.h("TRANSACTION_packageHasActiveAdmins", 41);
            Jd = ng.h("TRANSACTION_removeActiveAdmin", 43);
            this.mBinder = nh.getService("device_policy");
        }
    }
}
