package com.huawei.systemmanager.power.receiver.handle;

import android.content.Context;
import android.content.Intent;

public interface IBroadcastHandler {
    void handleBroadcast(Context context, Intent intent);
}
