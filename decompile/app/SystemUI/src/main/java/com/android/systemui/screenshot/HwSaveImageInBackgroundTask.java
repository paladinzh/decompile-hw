package com.android.systemui.screenshot;

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.PerformanceCheck;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/* compiled from: HwGlobalScreenshot */
class HwSaveImageInBackgroundTask extends SaveImageInBackgroundTask {
    private static boolean DEBUG_ACTION = true;
    private Context mCtx;
    private Uri mImageUri = null;
    private int mPostCode = -1;
    private CountDownLatch mWaitLock = new CountDownLatch(1);

    HwSaveImageInBackgroundTask(Context context, SaveImageInBackgroundData data, NotificationManager nManager, int nId) {
        super(context, data, nManager);
        this.mCtx = context;
        HwLog.i("HwGlobalScreenshot.HwSaveImageInBackgroundTask", "construct " + this.mImageUri + ", " + this.mPostCode);
    }

    protected Void doInBackground(Void... params) {
        if (DEBUG_ACTION) {
            HwLog.i("HwGlobalScreenshot.HwSaveImageInBackgroundTask", "doInBackground");
        }
        return super.doInBackground(params);
    }

    protected void onPostExecute(Void params) {
        if (DEBUG_ACTION) {
            HwLog.i("HwGlobalScreenshot.HwSaveImageInBackgroundTask", "onPostExecute");
        }
        try {
            super.onPostExecute(params);
        } finally {
            this.mWaitLock.countDown();
        }
    }

    public void onFileSaved(Uri imageUri) {
        if (DEBUG_ACTION) {
            HwLog.i("HwGlobalScreenshot.HwSaveImageInBackgroundTask", "onFileSaved with postAction: " + imageUri);
        }
        this.mImageUri = imageUri;
    }

    public void onButtonClicked(int postCode) {
        if (DEBUG_ACTION) {
            HwLog.i("HwGlobalScreenshot.HwSaveImageInBackgroundTask", "onButtonClicked in: " + postCode);
        }
        PerformanceCheck.enforceCallingInWorkThread();
        this.mPostCode = postCode;
        try {
            if (!this.mWaitLock.await(5000, TimeUnit.MILLISECONDS)) {
                HwLog.w("HwGlobalScreenshot.HwSaveImageInBackgroundTask", "onButtonClicked wait timeout");
            }
            postActionProcess();
        } catch (InterruptedException e) {
            HwLog.e("HwGlobalScreenshot.HwSaveImageInBackgroundTask", "onButtonClicked exception " + e.getMessage());
        }
    }

    public void postActionProcess() {
        if (this.mImageUri == null || -1 == this.mPostCode) {
            if (DEBUG_ACTION) {
                HwLog.w("HwGlobalScreenshot.HwSaveImageInBackgroundTask", "postActionProcess condition not match " + this.mImageUri + ", code: " + this.mPostCode);
            }
            return;
        }
        HwLog.i("HwGlobalScreenshot.HwSaveImageInBackgroundTask", "postActionProcess " + this.mImageUri + ", code: " + this.mPostCode);
        switch (this.mPostCode) {
            case 0:
                HwScreenshotUtil.shareScreenshot(this.mCtx, this.mImageUri);
                break;
            case 1:
                HwScreenshotUtil.editScreenshot(this.mCtx, this.mImageUri);
                break;
            case 2:
                HwScreenshotUtil.scrollScreenshot(this.mCtx, this.mImageUri);
                break;
            default:
                HwLog.e("HwGlobalScreenshot.HwSaveImageInBackgroundTask", "postActionProcess can't support postCode :" + this.mPostCode);
                break;
        }
    }
}
