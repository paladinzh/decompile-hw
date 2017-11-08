package com.huawei.systemmanager.sdk.tmsdk;

import android.content.Context;
import android.content.Intent;
import tmsdk.common.TMSBootReceiver;

public class TMSdkSecureReceiverWrapper extends TMSBootReceiver {
    public void doOnRecv(Context context, Intent intent) {
        super.doOnRecv(context, intent);
    }
}
