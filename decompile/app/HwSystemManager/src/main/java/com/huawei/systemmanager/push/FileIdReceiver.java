package com.huawei.systemmanager.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import com.huawei.systemmanager.rainbow.client.connect.request.DownloadConfigRequest;
import com.huawei.systemmanager.rainbow.client.connect.request.GetFileVersionRequest;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class FileIdReceiver extends HsmBroadcastReceiver {
    private static final int MAX_RETRY_COUNT = 3;
    private static final long SLEEP_TIME = 300000;
    private static String TAG = "FileIdReceiver";
    private PushResponse mPushResponse = null;
    private String mServerVersion = null;
    private String mSignature = null;
    private Intent mTargetIntent = new Intent();
    private final BroadcastReceiver mToTargetBroadcastDone = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            new LocalSharedPrefrenceHelper(context).putString(FileIdReceiver.this.mPushResponse.fileId, FileIdReceiver.this.mServerVersion);
            HwLog.i(FileIdReceiver.TAG, "mToTargetBroadcastDone fileid : " + FileIdReceiver.this.mPushResponse.fileId + ", new version : " + FileIdReceiver.this.mServerVersion);
        }
    };

    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            HwLog.w(TAG, "intent or action is null, ignore it.");
        } else {
            sendToBackground(context, intent);
        }
    }

    public void doInBackground(Context context, Intent intent) {
        super.doInBackground(context, intent);
        intent.setExtrasClassLoader(PushResponse.class.getClassLoader());
        this.mPushResponse = (PushResponse) intent.getParcelableExtra("PushResponse");
        if (this.mPushResponse == null || this.mPushResponse.fileName == null) {
            HwLog.w(TAG, "PushResponse is invalid!");
            return;
        }
        this.mTargetIntent.setAction(this.mPushResponse.action);
        this.mTargetIntent.putExtra("uri", this.mPushResponse.getUri());
        this.mTargetIntent.putExtra(PushResponse.PUSH_TYPE_FIELD, this.mPushResponse.pushType);
        GetFileVersionRequest request = new GetFileVersionRequest(this.mPushResponse.fileId, new LocalSharedPrefrenceHelper(context).getString(this.mPushResponse.fileId, "0"));
        request.processRequest(context);
        if (request.canDownload()) {
            this.mServerVersion = request.getServerVer();
            this.mSignature = request.getSignature();
            boolean success = download(context, request);
            int retryCount = 1;
            while (!success && retryCount < 3) {
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException e) {
                    HwLog.w(TAG, "doInBackground sleep InterruptedException", e);
                }
                HwLog.i(TAG, "doInBackground download failed and retry after " + (((long) retryCount) * 300000));
                success = download(context, request);
                retryCount++;
            }
            HwLog.i(TAG, "doInBackground download success finally?" + success);
            if (success) {
                sendBroadCastToTarget(context);
            }
        }
    }

    private boolean download(Context context, GetFileVersionRequest request) {
        DownloadConfigRequest downloadRequest = new DownloadConfigRequest(this.mPushResponse, request.getDownloadUrl(), this.mServerVersion, this.mSignature);
        downloadRequest.processRequest(context);
        return downloadRequest.isDownloadSuccess();
    }

    private void sendBroadCastToTarget(Context context) {
        HwLog.i(TAG, "sendBroadCastToTarget action: " + this.mTargetIntent.getAction() + " uri " + this.mTargetIntent.getExtra("uri"));
        context.sendOrderedBroadcastAsUser(this.mTargetIntent, UserHandle.ALL, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", this.mToTargetBroadcastDone, CustomTaskHandler.getInstance(context), 0, null, null);
    }
}
