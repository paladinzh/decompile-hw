package tmsdkobf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

/* compiled from: Unknown */
public class jy {
    private static final String[] uM = new String[]{"android.intent.action.PHONE_STATE", "android.intent.action.PHONE_STATE_2", "android.intent.action.PHONE_STATE2", "android.intent.action.PHONE_STATE_EXT"};

    public static int a(Context context, Intent intent) {
        String stringExtra = intent.getStringExtra("state");
        if (stringExtra != null) {
            if (stringExtra.equals("IDLE")) {
                return 0;
            }
            if (stringExtra.equals("RINGING")) {
                return 1;
            }
            if (stringExtra.equals("OFFHOOK")) {
                return 2;
            }
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        return telephonyManager == null ? 0 : telephonyManager.getCallState();
    }

    public static void a(Context context, BroadcastReceiver broadcastReceiver) {
        String[] strArr;
        int i = 0;
        qz qzVar = jq.uh;
        if (qzVar != null) {
            String str = qzVar.if();
            strArr = (str == null || str.equalsIgnoreCase("android.intent.action.PHONE_STATE")) ? uM : new String[]{"android.intent.action.PHONE_STATE", str};
        } else {
            strArr = uM;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(Integer.MAX_VALUE);
        intentFilter.addCategory("android.intent.category.DEFAULT");
        int length = strArr.length;
        while (i < length) {
            intentFilter.addAction(strArr[i]);
            i++;
        }
        context.registerReceiver(broadcastReceiver, intentFilter);
    }
}
