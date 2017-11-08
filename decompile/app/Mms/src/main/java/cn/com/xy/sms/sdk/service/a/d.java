package cn.com.xy.sms.sdk.service.a;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.com.xy.sms.util.ParseManager;

/* compiled from: Unknown */
final class d extends BroadcastReceiver {
    d() {
    }

    public final void onReceive(Context context, Intent intent) {
        if (ParseManager.UPDATE_ICCID_INFO_CACHE_ACTION.equals(intent.getAction())) {
            b.a(intent);
        }
    }
}
