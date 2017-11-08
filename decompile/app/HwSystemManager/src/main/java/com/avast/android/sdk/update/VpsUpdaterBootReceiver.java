package com.avast.android.sdk.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.obfuscated.bg;

/* compiled from: Unknown */
public class VpsUpdaterBootReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        bg.a(context, EngineInterface.getEngineConfig());
    }
}
