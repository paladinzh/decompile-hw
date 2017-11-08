package com.huawei.harassmentinterception.update;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.engine.HwEngineCaller;
import com.huawei.harassmentinterception.engine.HwEngineCallerManager;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;

public class UpdateService extends Service {
    public static final String ACTION_BEGIN_CHECK = "com.huawei.harassmentinterception.update.begin_check";
    public static final String ACTION_BEGIN_UPDATE = "com.huawei.harassmentinterception.update.begin_update";
    public static final String ACTION_CHECK_ERROR = "com.huawei.harassmentinterception.update.check_error";
    public static final String ACTION_FINISHED_CHECK = "com.huawei.harassmentinterception.update.finished_check";
    public static final String ACTION_FINISHED_UPDATE = "com.huawei.harassmentinterception.update.finished_update";
    public static final String ACTION_NET_ERROR = "com.huawei.harassmentinterception.update.net_error";
    public static final String ACTION_OVERDUE_ERROR = "com.huawei.harassmentinterception.update.overdue_error";
    public static final String ACTION_UPDATE_CANCEL = "com.huawei.harassmentinterception.update.cancel";
    public static final String ACTION_UPDATE_ERROR = "com.huawei.harassmentinterception.update.error";
    public static final String ACTION_UPDATE_NOT_SUPPORT = "com.huawei.harassmentinterception.update.not_support";
    public static final String ACTION_UPDATE_PROGRESS = "com.huawei.harassmentinterception.update.progress";
    private static final int AUTO_UPDATE = 1;
    public static final String KEY_ACTION_EXTRA = "ExtraKey";
    private static final int MANUAL_UPDATE = 0;
    private static final int MGS_CHECK_UPDATE = 1;
    public static final String TAG = "UpdateService";
    private ConnectivityManager mConnectivityManager;
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private int mStartId = -1;

    private class HwUpdateListener implements IHwUpdateListener {
        int mTaskStartId;
        int mUpdateFlag;

        public HwUpdateListener(int nStartId, int updateFlag) {
            this.mTaskStartId = nStartId;
            this.mUpdateFlag = updateFlag;
        }

        public void onUpdateStart(int nCode) {
            HwLog.i(UpdateService.TAG, "onUpdateStart: startId = " + this.mTaskStartId + ", update flag = " + this.mUpdateFlag + ", nCode = " + nCode);
            UpdateService.this.updateUI(this.mUpdateFlag, UpdateService.ACTION_BEGIN_UPDATE);
        }

        public void onUpdateProgress(int nProgress) {
            HwLog.d(UpdateService.TAG, "onUpdateProgress: nProgress = " + nProgress + ", startId = " + this.mTaskStartId + ", update flag = " + this.mUpdateFlag);
            UpdateService.this.updateUI(this.mUpdateFlag, UpdateService.ACTION_UPDATE_PROGRESS, nProgress);
        }

        public void onUpdateFinished(int nCode) {
            HwLog.i(UpdateService.TAG, "onUpdateFinished: startId = " + this.mTaskStartId + ", update flag = " + this.mUpdateFlag + ", nCode = " + nCode);
            if (1 == nCode || 3 == nCode || 5 == nCode) {
                UpdateService.this.saveUpdateTime();
                UpdateService.this.updateUI(this.mUpdateFlag, UpdateService.ACTION_FINISHED_UPDATE, nCode);
            } else {
                UpdateService.this.updateUI(this.mUpdateFlag, UpdateService.ACTION_NET_ERROR);
            }
            if (5 != nCode) {
                UpdateService.this.stopSelf();
            }
        }

        public void onUpdateError(int nCode) {
            HwLog.i(UpdateService.TAG, "onUpdateError: startId = " + this.mTaskStartId + ", update flag = " + this.mUpdateFlag + ", nCode = " + nCode);
            UpdateService.this.updateUI(this.mUpdateFlag, UpdateService.ACTION_UPDATE_ERROR);
            UpdateService.this.stopSelf();
        }

        public void onUpdateCancel(int nCode) {
            HwLog.i(UpdateService.TAG, "onUpdateCancel: startId = " + this.mTaskStartId + ", update flag = " + this.mUpdateFlag + ", nCode = " + nCode);
            UpdateService.this.updateUI(this.mUpdateFlag, UpdateService.ACTION_UPDATE_CANCEL);
            UpdateService.this.stopSelf();
        }

        public void onBackgroundUpdateFinished(int nCode) {
            HwLog.i(UpdateService.TAG, "onBackgroundUpdateFinished: startId = " + this.mTaskStartId + ", update flag = " + this.mUpdateFlag + ", nCode = " + nCode);
            UpdateService.this.saveUpdateTime();
            UpdateService.this.stopSelf();
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int nStartId = msg.arg1;
            int nUpdateFlag = msg.arg2;
            if (UpdateService.this.checkNetwork(nUpdateFlag)) {
                if (!UpdateService.this.executeUpdate(nStartId, nUpdateFlag)) {
                    HwLog.i(UpdateService.TAG, "stopSelf " + nStartId);
                    UpdateService.this.stopSelf(nStartId);
                }
                return;
            }
            UpdateService.this.updateUI(nUpdateFlag, UpdateService.ACTION_NET_ERROR);
            HwLog.w(UpdateService.TAG, "Network is not ready.");
            UpdateService.this.stopSelf(nStartId);
        }
    }

    private boolean executeUpdate(int nStartId, int updateFlag) {
        Utility.initSDK(GlobalContext.getContext());
        HwEngineCaller caller = HwEngineCallerManager.getInstance().getEngineCaller();
        if (caller == null) {
            HwLog.w(TAG, "number mark database should update ,need not EngineSwitchOn");
            caller = new HwEngineCaller(GlobalContext.getContext());
            HwEngineCallerManager.getInstance().setEngineCaller(caller);
            caller.onSwitchIn(0);
            HwLog.d(TAG, "UI process die, so we need to reset HwEngineCallerManager");
        }
        int nUpdateCode = caller.doUpdate(new HwUpdateListener(nStartId, updateFlag));
        HwLog.d(TAG, "executeUpdate: nUpdateCode = " + nUpdateCode);
        switch (nUpdateCode) {
            case 0:
                updateUI(updateFlag, ACTION_NET_ERROR);
                HwLog.w(TAG, "executeUpdate: fail to start update");
                return false;
            case 3:
                saveUpdateTime();
                updateUI(updateFlag, ACTION_FINISHED_CHECK);
                return false;
            case 4:
                updateUI(updateFlag, ACTION_UPDATE_ERROR);
                return false;
            default:
                HwLog.i(TAG, "executeUpdate: id = " + nStartId + ", flag = " + updateFlag + ", code = " + nUpdateCode);
                return true;
        }
    }

    private boolean cancelUpdate() {
        boolean z = true;
        HwEngineCaller caller = HwEngineCallerManager.getInstance().getEngineCaller();
        if (caller == null) {
            HwLog.i(TAG, "cancelUpdate: update is not supported");
            return false;
        }
        int nUpdateCode = caller.cancelUpdate();
        HwLog.i(TAG, "cancelUpdate: result = " + nUpdateCode);
        if (1 != nUpdateCode) {
            z = false;
        }
        return z;
    }

    private void updateUI(int nUpdateFlag, String action) {
        if (1 != nUpdateFlag) {
            sendBroadcastAsUser(new Intent(action), UserHandle.OWNER, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        }
    }

    private void updateUI(int nUpdateFlag, String action, int nExtra) {
        if (1 != nUpdateFlag) {
            Intent intent = new Intent(action);
            intent.putExtra(KEY_ACTION_EXTRA, nExtra);
            sendBroadcastAsUser(intent, UserHandle.OWNER, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        }
    }

    public void onCreate() {
        super.onCreate();
        HwLog.i(TAG, "onCreate");
        HandlerThread thread = new HandlerThread("ServiceStartArguments", 10);
        thread.start();
        this.mServiceLooper = thread.getLooper();
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
        this.mConnectivityManager = (ConnectivityManager) getSystemService("connectivity");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int i = 1;
        HwLog.i(TAG, "onStartCommand, flags = " + flags + ", startId = " + startId);
        if (intent == null) {
            return flags;
        }
        boolean isAuto = intent.getBooleanExtra(ConstValues.KEY_AUTOUPDATE_FLAG, true);
        this.mStartId = startId;
        this.mServiceHandler.removeMessages(1);
        Message msg = this.mServiceHandler.obtainMessage(1);
        msg.arg1 = this.mStartId;
        if (!isAuto) {
            i = 0;
        }
        msg.arg2 = i;
        this.mServiceHandler.sendMessage(msg);
        return 3;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        HwLog.d(TAG, "onDestroy starts , startId = " + this.mStartId);
        this.mServiceHandler.removeMessages(1);
        if (this.mServiceLooper != null) {
            this.mServiceLooper.quit();
        }
        cancelUpdate();
        HwLog.d(TAG, "onDestroy ends,startId = " + this.mStartId);
    }

    private void saveUpdateTime() {
        PreferenceHelper.saveLastAlarmTime(this);
    }

    private boolean checkNetwork(int updateFlag) {
        NetworkInfo networkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            HwLog.i(TAG, "checkNetwork: Fail to get network info");
            return false;
        } else if (!networkInfo.isConnected()) {
            HwLog.i(TAG, "checkNetwork: network is not connected");
            return false;
        } else if (networkInfo.isAvailable()) {
            return checkNetworkInfoForAutoUpdate(networkInfo, updateFlag);
        } else {
            HwLog.i(TAG, "checkNetwork: network is not available");
            return false;
        }
    }

    private boolean checkNetworkInfoForAutoUpdate(NetworkInfo networkInfo, int updateFlag) {
        if (1 != updateFlag) {
            return true;
        }
        int updateStrategy = UpdateHelper.getAutoUpdateStrategy(getApplication());
        HwLog.i(TAG, "checkNetworkInfoForAutoUpdate called, updateStrategy:" + updateStrategy);
        if (updateStrategy != 2) {
            return updateStrategy == 3;
        } else {
            int nType = networkInfo.getType();
            if (9 == nType || 1 == nType || 6 == nType) {
                HwLog.i(TAG, "checkNetworkInfoForAutoUpdate current is on wifi");
                return true;
            }
            HwLog.i(TAG, "checkNetworkInfoForAutoUpdate current is not wifi");
            return false;
        }
    }
}
