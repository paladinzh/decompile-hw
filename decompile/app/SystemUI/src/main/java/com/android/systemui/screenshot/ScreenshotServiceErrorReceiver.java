package com.android.systemui.screenshot;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.systemui.R;

public class ScreenshotServiceErrorReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        GlobalScreenshot.notifyScreenshotError(context, (NotificationManager) context.getSystemService("notification"), R.string.screenshot_failed_to_save_unknown_text);
    }
}
