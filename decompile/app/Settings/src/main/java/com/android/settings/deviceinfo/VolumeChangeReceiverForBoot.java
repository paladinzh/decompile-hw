package com.android.settings.deviceinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.settings.Utils;

public class VolumeChangeReceiverForBoot extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("VolumeChangeReceiverForBoot", "action =" + action);
        if (Utils.isSwitchPrimaryVolumeSupported() && "android.intent.action.BOOT_COMPLETED".equals(action) && DefaultStorageLocation.isSdcard() && !Utils.hasWriteableExternalSdcard(context)) {
            Intent resetIntent = new Intent(context, DefaultStorageResettingActivity.class);
            resetIntent.setFlags(268435456);
            context.startActivity(resetIntent);
        }
    }
}
