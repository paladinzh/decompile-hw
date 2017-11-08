package com.android.settings.deviceinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.storage.StorageVolume;
import android.util.Log;
import com.android.settings.Utils;

public class VolumeChangeReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("VolumeChangeReceiver", "action =" + action);
        if (Utils.isSwitchPrimaryVolumeSupported()) {
            if (("android.intent.action.MEDIA_BAD_REMOVAL".equals(action) || "android.intent.action.MEDIA_REMOVED".equals(action)) && !Utils.isVolumeUsb(context, (StorageVolume) intent.getExtra("android.os.storage.extra.STORAGE_VOLUME")) && DefaultStorageLocation.isSdcard() && !DefaultStorageLocation.isRebooting()) {
                Log.d("VolumeChangeReceiver", "begin to show DialogActivityForProgress");
                Intent resetIntent = new Intent(context, DefaultStorageResettingActivity.class);
                resetIntent.setFlags(268435456);
                context.startActivity(resetIntent);
            }
        }
    }
}
