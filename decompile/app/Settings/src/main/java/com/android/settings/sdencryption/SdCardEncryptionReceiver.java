package com.android.settings.sdencryption;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserManager;
import android.os.storage.StorageVolume;
import com.android.settings.ItemUseStat;

public class SdCardEncryptionReceiver extends BroadcastReceiver {
    private static String SD_ENCRYPTION_ENABLE = "persist.sys.sdencryption.enable";

    public void onReceive(final Context context, Intent intent) {
        if (SdEncryptionUtils.isFeatureAvailable() && intent != null) {
            String action = intent.getAction();
            SdLog.i("SdCardEncryptionReceiver", "Receive action = " + action);
            if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                String enable = SystemProperties.get(SD_ENCRYPTION_ENABLE, "false");
                if (enable.equals("false")) {
                    SystemProperties.set(SD_ENCRYPTION_ENABLE, "true");
                    SdLog.i("SdCardEncryptionReceiver", "Set SD_ENCRYPTION_ENABLE = " + enable);
                }
            } else if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
                StorageVolume volume = (StorageVolume) intent.getParcelableExtra("android.os.storage.extra.STORAGE_VOLUME");
                if (volume == null || !SdEncryptionUtils.isVolumeExternalSDcard(context, volume)) {
                    SdLog.i("SdCardEncryptionReceiver", "Volume is not SD card.");
                    return;
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        SdCardEncryptionReceiver.this.startCryptionIfNeeded(context);
                    }
                }, 0);
            }
        }
    }

    private void startCryptionIfNeeded(Context context) {
        if (UserManager.get(context).isPrimaryUser()) {
            String sdEncryptionState = SdEncryptionUtils.getCryptState();
            if (sdEncryptionState.equals("encrypting")) {
                ItemUseStat.getInstance().handleClick(context, 8, "automatic_encrytion");
                SdEncryptionUtils.startCryption(true, context);
                SdEncryptionUtils.sendStateBroadcast(context, 2, true);
            } else if (sdEncryptionState.equals("decrypting")) {
                ItemUseStat.getInstance().handleClick(context, 8, "automatic_decrytion");
                SdEncryptionUtils.startCryption(false, context);
                SdEncryptionUtils.sendStateBroadcast(context, 2, false);
            } else {
                SdLog.d("SdCardEncryptionReceiver", "received unknow event, just return");
            }
            return;
        }
        SdLog.i("SdCardEncryptionReceiver", "Not Primary User, return.");
    }
}
