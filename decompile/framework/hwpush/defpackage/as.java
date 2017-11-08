package defpackage;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.model.channel.ChannelMgr;

/* renamed from: as */
public class as extends o {
    private static String LOG_TAG = "PushLog2841";
    private static boolean bK = false;

    public as(Context context) {
    }

    private static void a(Context context, boolean z, String str) {
        Intent intent = new Intent();
        aw.d(LOG_TAG, "sendStateBroadcast the current push state is: " + z);
        intent.setAction("com.huawei.intent.action.PUSH_STATE").putExtra("push_state", z).setFlags(32);
        if (intent != null) {
            intent.setPackage(str);
        }
        context.sendBroadcast(intent);
    }

    private static void k(boolean z) {
        bK = z;
    }

    public void onReceive(Context context, Intent intent) {
        aw.d(LOG_TAG, "enter ChannelRecorder:onReceive(intent:" + intent + " context:" + context);
        String action = intent.getAction();
        boolean hasConnection = ChannelMgr.aX().hasConnection();
        aw.d(LOG_TAG, "PushState get action :" + action);
        if ("com.huawei.android.push.intent.GET_PUSH_STATE".equals(action)) {
            action = intent.getStringExtra("pkg_name");
            aw.d(LOG_TAG, "responseClinetGetPushState: get the client packageName: " + action);
            try {
                aw.d(LOG_TAG, "current program pkgName is: " + context.getPackageName());
                aw.d(LOG_TAG, "the current push curIsConnect:" + hasConnection);
                as.a(context, hasConnection, action);
            } catch (Exception e) {
                aw.d(LOG_TAG, "e:" + e.toString());
            }
        }
        if (bK != hasConnection) {
            as.k(hasConnection);
        }
    }
}
