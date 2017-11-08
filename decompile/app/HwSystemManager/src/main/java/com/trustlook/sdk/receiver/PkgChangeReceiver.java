package com.trustlook.sdk.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.trustlook.sdk.service.ServicePkgChange;

public class PkgChangeReceiver extends WakefulBroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent intent2 = new Intent(intent);
        intent2.setClass(context, ServicePkgChange.class);
        context.startService(intent2);
    }
}
