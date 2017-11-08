package com.huawei.gallery.photoshare.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class PhotoShareNotificationDeleteIntentReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if ("com.huawei.gallery.action.DOWNLOADNOTIFICATION_DELETE".equals(action)) {
                PhotoShareUtils.enableDownloadStatusBarNotification(false);
            } else if ("com.huawei.gallery.action.UPLOADNOTIFICATION_DELETE".equals(action)) {
                PhotoShareUtils.enableUploadStatusBarNotification(false);
            }
        }
    }
}
