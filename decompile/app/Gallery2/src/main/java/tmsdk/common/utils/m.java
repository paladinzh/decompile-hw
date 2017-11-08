package tmsdk.common.utils;

import android.content.Context;
import android.content.Intent;
import tmsdk.common.TMSDKContext;
import tmsdkobf.jw;

/* compiled from: Unknown */
public class m {
    public static void wakeup() {
        if (TMSDKContext.getTmsliteSwitch()) {
            Context applicaionContext = TMSDKContext.getApplicaionContext();
            Intent intent = new Intent();
            intent.setPackage(jw.uF);
            intent.setAction(jw.uF + ".TMS_LITE_SERVICE");
            intent.putExtra("avtive_by_tmssdk_cloud_switch", applicaionContext.getPackageName());
            d.e("TmsliteUtil", "XXX:[" + intent + "]");
            applicaionContext.startService(intent);
            return;
        }
        d.e("TmsliteUtil", "getTmsliteSwitch off");
    }
}
