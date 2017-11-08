package com.huawei.systemmanager.antivirus.engine.avast.update;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import com.avast.android.sdk.engine.UpdateResultStructure;
import com.avast.android.sdk.engine.UpdateResultStructure.UpdateResult;
import com.avast.android.sdk.update.VpsUpdateService;
import com.huawei.systemmanager.util.HwLog;

public class UpdateService extends VpsUpdateService {
    private static final String TAG = "AvastUpdateService";

    public UpdateService() {
        super(TAG);
    }

    protected boolean isUpdateAllowed(NetworkInfo networkInfo) {
        return true;
    }

    protected void onUpdateStarted() {
        HwLog.i(TAG, "onUpdateStarted");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UpdateUtils.ACTION_VIRUS_UPDATE_START);
        broadcastIntent.setPackage(getPackageName());
        sendBroadcast(broadcastIntent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
    }

    protected void publishResult(UpdateResultStructure updateResultStructure) {
        HwLog.i(TAG, "publishResult");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UpdateUtils.ACTION_VIRUS_UPDATE_FINISH);
        broadcastIntent.setPackage(getPackageName());
        Bundle bundle = new Bundle();
        HwLog.i(TAG, "update result:" + updateResultStructure.result);
        bundle.putBoolean(UpdateUtils.KEY_UPDATE_RESULT, UpdateResult.RESULT_UPDATED.equals(updateResultStructure.result));
        broadcastIntent.putExtras(bundle);
        sendBroadcast(broadcastIntent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
    }

    protected void publishDownloadProgress(long progress, long total) {
        HwLog.d(TAG, "progress:" + progress + ",total:" + total);
    }
}
