package com.android.systemui.screenshot;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import com.android.systemui.utils.HwLog;

/* compiled from: HwScreenshotNotifications */
class HwDeleteImageInBackgroundTask extends AsyncTask<Uri, Void, Void> {
    private Context mReceiverContext;

    HwDeleteImageInBackgroundTask(Context context) {
        this.mReceiverContext = context;
    }

    protected Void doInBackground(Uri... params) {
        if (params.length != 1) {
            HwLog.e("DeleteImageInBackgroundTask", "HwDeleteImageInBackgroundTask:: params length is invalid");
            return null;
        }
        Uri screenshotUri = params[0];
        try {
            this.mReceiverContext.getContentResolver().delete(screenshotUri, null, null);
        } catch (Exception e) {
            HwLog.e("DeleteImageInBackgroundTask", "HwDeleteImageInBackgroundTask::delete failed,screenshotUri=" + screenshotUri);
        }
        return null;
    }
}
