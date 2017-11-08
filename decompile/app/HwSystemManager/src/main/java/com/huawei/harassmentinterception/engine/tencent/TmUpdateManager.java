package com.huawei.harassmentinterception.engine.tencent;

import com.huawei.harassmentinterception.update.IHwUpdateListener;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.IUpdateListener;
import tmsdk.common.module.update.UpdateInfo;
import tmsdk.common.module.update.UpdateManager;

public class TmUpdateManager implements IUpdateListener, ICheckListener {
    private static final String TAG = "TmUpdateManager";
    private boolean isUpdating = false;
    private List<UpdateInfo> mBackgroundUpdateInfo = new ArrayList();
    private int mEvent = 0;
    private List<UpdateInfo> mForgroundUpdateInfo = new ArrayList();
    private IHwUpdateListener mHwUpdateListener;
    private UpdateManager mUpdateManager;

    class BackgroundUpdateListener implements IUpdateListener {
        BackgroundUpdateListener() {
        }

        public void onUpdateStarted() {
        }

        public void onUpdateFinished() {
            TmUpdateManager.this.isUpdating = false;
            HwLog.i(TmUpdateManager.TAG, "BackgroundUpdated onUpdateFinished ");
            TmUpdateManager.this.mHwUpdateListener.onBackgroundUpdateFinished(5);
        }

        public void onUpdateEvent(UpdateInfo arg0, int arg1) {
            TmUpdateManager.this.isUpdating = false;
            HwLog.i(TmUpdateManager.TAG, "BackgroundUpdated onUpdateEvent");
            TmUpdateManager.this.mHwUpdateListener.onBackgroundUpdateFinished(5);
        }

        public void onUpdateCanceled() {
            TmUpdateManager.this.isUpdating = false;
            HwLog.i(TmUpdateManager.TAG, "BackgroundUpdated onUpdateCanceled ");
            TmUpdateManager.this.mHwUpdateListener.onBackgroundUpdateFinished(5);
        }

        public void onProgressChanged(UpdateInfo arg0, int arg1) {
        }
    }

    public TmUpdateManager(IHwUpdateListener hwUpdateListener) {
        this.mHwUpdateListener = hwUpdateListener;
        initManager();
    }

    private void initManager() {
        if (TMSEngineFeature.isSupportTMS()) {
            this.mUpdateManager = (UpdateManager) ManagerCreatorC.getManager(UpdateManager.class);
        }
    }

    public int doUpdate() {
        HwLog.i(TAG, "current thread id = " + Thread.currentThread().getId());
        Utility.initSDK(GlobalContext.getContext());
        initManager();
        if (this.mUpdateManager == null) {
            HwLog.w(TAG, "doUpdate: Invalid update manager");
            return 4;
        } else if (this.isUpdating) {
            HwLog.w(TAG, "doUpdate: already updating");
            this.mHwUpdateListener.onUpdateFinished(5);
            return 1;
        } else {
            this.mBackgroundUpdateInfo.clear();
            this.mForgroundUpdateInfo.clear();
            try {
                this.mEvent = 0;
                this.mUpdateManager.check(-9079256848778919936L, this);
                return 2;
            } catch (Exception e) {
                HwLog.e(TAG, "doUpdate: Exception", e);
                return 4;
            }
        }
    }

    public int cancelUpdate() {
        HwLog.i(TAG, "cancelUpdate");
        this.isUpdating = false;
        this.mForgroundUpdateInfo.clear();
        this.mBackgroundUpdateInfo.clear();
        if (this.mUpdateManager == null) {
            HwLog.w(TAG, "cancelUpdate: Invalid update manager");
            return 4;
        }
        try {
            this.mUpdateManager.cancel();
            return 1;
        } catch (Exception e) {
            HwLog.e(TAG, "cancelUpdate: Exception", e);
            return 4;
        }
    }

    public void onCheckStarted() {
        this.isUpdating = true;
        HwLog.i(TAG, "onCheckStarted");
    }

    public void onCheckCanceled() {
        HwLog.i(TAG, "onCheckCanceled");
        this.isUpdating = false;
        this.mHwUpdateListener.onUpdateCancel(0);
    }

    public void onCheckEvent(int event) {
        HwLog.i(TAG, "onCheckEvent: event = " + event);
        this.isUpdating = false;
        this.mEvent = event;
        this.mHwUpdateListener.onUpdateError(0);
    }

    public void onCheckFinished(CheckResult checkResult) {
        if (this.mEvent < 0) {
            HwLog.i(TAG, "onCheckFinished");
            this.isUpdating = false;
            this.mHwUpdateListener.onUpdateFinished(4);
        } else if (checkResult == null || Utility.isNullOrEmptyList(checkResult.mUpdateInfoList)) {
            HwLog.i(TAG, "onCheckFinished");
            this.isUpdating = false;
            this.mHwUpdateListener.onUpdateFinished(3);
        } else if (this.mUpdateManager == null) {
            HwLog.w(TAG, "onCheckFinished: Invalid update manager");
            this.isUpdating = false;
            this.mHwUpdateListener.onUpdateFinished(4);
        } else {
            try {
                List<UpdateInfo> updateInfos = checkResult.mUpdateInfoList;
                HwLog.i(TAG, "checkResult.mUpdateInfoList size= " + updateInfos.size());
                for (int i = 0; i < updateInfos.size(); i++) {
                    HwLog.i(TAG, "updateInfo[" + i + "] " + ((UpdateInfo) updateInfos.get(i)).fileName);
                    if (144115188075855872L != ((UpdateInfo) updateInfos.get(i)).flag) {
                        this.mBackgroundUpdateInfo.add((UpdateInfo) updateInfos.get(i));
                    } else {
                        this.mForgroundUpdateInfo.add((UpdateInfo) updateInfos.get(i));
                    }
                }
                doUpdateTask();
            } catch (Exception e) {
                HwLog.e(TAG, "onCheckFinished: update exception", e);
                this.mHwUpdateListener.onUpdateFinished(4);
            }
        }
    }

    private void doUpdateTask() {
        if (this.mForgroundUpdateInfo.size() > 0) {
            HwLog.i(TAG, "doUpdateTask, do forgroundUpdateInfo");
            this.mUpdateManager.update(this.mForgroundUpdateInfo, this);
        } else if (this.mBackgroundUpdateInfo.size() == 0) {
            HwLog.i(TAG, "UpdateInfo is 0,skip.");
            this.isUpdating = false;
            this.mHwUpdateListener.onUpdateFinished(3);
        } else {
            HwLog.i(TAG, "doUpdateTask, just do backgroundUpdateInfo");
            this.mHwUpdateListener.onUpdateFinished(5);
            this.mUpdateManager.update(this.mBackgroundUpdateInfo, new BackgroundUpdateListener());
        }
    }

    public void onProgressChanged(UpdateInfo updateInfo, int progress) {
        this.mHwUpdateListener.onUpdateProgress(progress);
    }

    public void onUpdateCanceled() {
        this.isUpdating = false;
        HwLog.i(TAG, "onUpdateCanceled");
        this.mHwUpdateListener.onUpdateCancel(0);
    }

    public void onUpdateEvent(UpdateInfo arg0, int arg1) {
        this.isUpdating = false;
        this.mHwUpdateListener.onUpdateError(0);
    }

    public void onUpdateFinished() {
        if (this.mBackgroundUpdateInfo.size() == 0) {
            HwLog.i(TAG, "do forground finished");
            this.isUpdating = false;
            this.mHwUpdateListener.onUpdateFinished(1);
            return;
        }
        HwLog.i(TAG, "do forground finished, then do background ");
        this.mHwUpdateListener.onUpdateFinished(5);
        this.mUpdateManager.update(this.mBackgroundUpdateInfo, new BackgroundUpdateListener());
    }

    public void onUpdateStarted() {
    }
}
