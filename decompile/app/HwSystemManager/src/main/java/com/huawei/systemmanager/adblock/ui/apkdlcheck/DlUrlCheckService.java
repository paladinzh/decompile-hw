package com.huawei.systemmanager.adblock.ui.apkdlcheck;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.adblock.ui.apkdlcheck.DlUrlCheckTask.Callback;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class DlUrlCheckService extends Service implements Callback {
    private static final String DOWNLOAD_ID_STAT = "100";
    public static final String SCREEN_ORIENTATION_ACTION = "com.huawei.systemmanger.action.OrientationChange";
    private static final String TAG = "AdBlock_DlUrlCheckService";
    private int currentOrientation;
    private int mClientCount = 0;
    private List<IDlUrlCheckTask> mRequestList = new ArrayList();

    public void onCreate() {
        super.onCreate();
        this.currentOrientation = getResources().getConfiguration().orientation;
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (this) {
            this.mClientCount++;
            HwLog.i(TAG, "Dialog service start, client count:" + this.mClientCount);
        }
        Bundle bundle = intent != null ? intent.getExtras() : null;
        if (bundle == null) {
            stopSelfIfNeeded();
            return 2;
        }
        addTask(bundle);
        return 2;
    }

    private void stopSelfIfNeeded() {
        synchronized (this) {
            this.mClientCount--;
            HwLog.i(TAG, "dialog service stop, client count:" + this.mClientCount);
            if (this.mClientCount <= 0) {
                stopSelf();
            }
        }
    }

    private void addTask(Bundle bundle) {
        IDlUrlCheckTask task = new DlUrlCheckTask(getApplicationContext(), this, bundle);
        if (DOWNLOAD_ID_STAT.equals(task.getDownloadId())) {
            HwLog.i(TAG, "addRequest DOWNLOAD_ID_STAT");
            task.statAdUrlBlocked();
            abortTask(task);
        } else if (!task.isValid()) {
            HwLog.w(TAG, "addRequest param is invalid");
            abortTask(task);
        } else if (!AdUtils.isCloudEnable(getApplicationContext())) {
            HwLog.i(TAG, "execute it is not allow access network, just return");
            abortTask(task);
        } else if (!AdUtils.isDlCheckEnable(getApplicationContext())) {
            HwLog.i(TAG, "execute hw_download_non_market_apps is open, should not block");
            abortTask(task);
        } else if (this.mRequestList.indexOf(task) >= 0) {
            stopSelfIfNeeded();
        } else {
            this.mRequestList.add(task);
            task.execute();
        }
    }

    private void abortTask(IDlUrlCheckTask task) {
        if (!DOWNLOAD_ID_STAT.equals(task.getDownloadId())) {
            task.setResult(true);
        }
        stopSelfIfNeeded();
    }

    public void onTaskFinish(IDlUrlCheckTask task) {
        this.mRequestList.remove(task);
        stopSelfIfNeeded();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation != this.currentOrientation) {
            this.currentOrientation = newConfig.orientation;
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(SCREEN_ORIENTATION_ACTION));
            HwLog.i(TAG, "onConfigurationChanged,sendBroadcast");
        }
    }
}
