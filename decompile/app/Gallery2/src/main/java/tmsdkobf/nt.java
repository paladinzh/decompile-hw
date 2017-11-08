package tmsdkobf;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import java.util.HashMap;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.creator.ManagerCreatorC;

/* compiled from: Unknown */
final class nt extends BaseManagerC {
    private static int[] Dg = new int[]{0, 1, 2, 4, 9, 15};
    private HashMap<ComponentName, ServiceInfo> Dd = new HashMap();
    private byte[] De = new byte[0];
    private np Df;
    private ActivityManager mActivityManager;
    private Context mContext;
    private PackageManager mPackageManager;
    private qa pi;

    nt() {
    }

    public synchronized np fv() {
        if (this.Df == null) {
            this.Df = new nq(this.mContext);
        }
        return this.Df;
    }

    public int getSingletonType() {
        return 1;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.pi = (qa) ManagerCreatorC.getManager(qa.class);
    }
}
