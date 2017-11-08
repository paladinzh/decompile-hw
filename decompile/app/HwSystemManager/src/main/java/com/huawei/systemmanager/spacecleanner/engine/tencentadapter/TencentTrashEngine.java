package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.CombineTask;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine.IUpdateListener;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.util.HwLog;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.UpdateInfo;
import tmsdk.common.module.update.UpdateManager;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.deepclean.DeepcleanManager;
import tmsdk.fg.module.deepclean.RubbishEntity;
import tmsdk.fg.module.deepclean.ScanProcessListener;
import tmsdk.fg.module.deepclean.UpdateRubbishDataCallback;

public class TencentTrashEngine implements ITrashEngine, MessageHandler {
    private static final int MSG_CHECK_UPDATE_FINISH = 202;
    private static final int MSG_UPDATE_FINISH = 201;
    private static final int MSG_UPDATE_RUBBISH_LIB = 200;
    private static final String TAG = "TencentTrashEngine";
    private static final int UPDATE_TIMEOUT = 15000;
    private final Context mContext;
    private DeepcleanManager mDeepcleanManager;
    private InnerCheckListener mInnerCheckListener;
    private InnerUpdateListener mInnerUpdateListener;
    private InnerTaskProcessListener mListner;
    private boolean mRubbishUpdateFlag;
    private UpdateRubbishDataCallback mUpdateCallback;
    private GenericHandler mUpdateHandler;
    private IUpdateListener mUpdateListener;
    private UpdateManager mUpdateManager;
    private boolean mWechatUpdateFlag;

    private class InnerCheckListener implements ICheckListener {
        private InnerCheckListener() {
        }

        public void onCheckStarted() {
            HwLog.i(TencentTrashEngine.TAG, "InnerCheckListener , onCheckStarted");
        }

        public void onCheckEvent(int i) {
        }

        public void onCheckCanceled() {
            HwLog.i(TencentTrashEngine.TAG, "InnerCheckListener , onCheckCanceled");
        }

        public void onCheckFinished(CheckResult checkResult) {
            HwLog.i(TencentTrashEngine.TAG, "InnerCheckListener , onCheckFinished");
            if (checkResult != null && checkResult.mUpdateInfoList != null) {
                HwLog.i(TencentTrashEngine.TAG, "InnerCheckListener , update");
                if (TencentTrashEngine.this.mUpdateHandler != null) {
                    TencentTrashEngine.this.mUpdateManager.update(checkResult.mUpdateInfoList, TencentTrashEngine.this.mInnerUpdateListener);
                }
            }
        }
    }

    private static class InnerTaskProcessListener extends TencentSimpleListener {
        private ScanProcessListener mCleannerListener;
        private ScanProcessListener mScannerListener;

        private InnerTaskProcessListener() {
            this.mScannerListener = TencentSimpleListener.sEmptyScanListener;
            this.mCleannerListener = TencentSimpleListener.sEmptyScanListener;
        }

        public void setScanListener(ScanProcessListener l) {
            if (l != null) {
                this.mScannerListener = l;
            }
        }

        public void setCleanLisnter(ScanProcessListener l) {
            if (l != null) {
                this.mCleannerListener = l;
            }
        }

        public void onScanStarted() {
            this.mScannerListener.onScanStarted();
        }

        public void onScanProcessChange(int nowPercent, String scanPath) {
            this.mScannerListener.onScanProcessChange(nowPercent, scanPath);
        }

        public void onScanCanceled() {
            this.mScannerListener.onScanCanceled();
        }

        public void onScanFinished() {
            this.mScannerListener.onScanFinished();
        }

        public void onScanError(int error) {
            this.mScannerListener.onScanError(error);
        }

        public void onCleanStart() {
            this.mCleannerListener.onCleanStart();
        }

        public void onCleanProcessChange(long currenCleanSize, int nowPercent) {
            this.mCleannerListener.onCleanProcessChange(currenCleanSize, nowPercent);
        }

        public void onCleanFinish() {
            this.mCleannerListener.onCleanFinish();
        }

        public void onCleanError(int error) {
            this.mCleannerListener.onCleanError(error);
        }

        public void onRubbishFound(RubbishEntity arg0) {
            this.mScannerListener.onRubbishFound(arg0);
        }
    }

    private class InnerUpdateCallback implements UpdateRubbishDataCallback {
        private InnerUpdateCallback() {
        }

        public void updateFinished() {
            HwLog.i(TencentTrashEngine.TAG, "receive UpdateRubbishDataCallback callback updateFinished");
            TencentTrashEngine.this.mRubbishUpdateFlag = true;
            TencentTrashEngine.this.mUpdateHandler.sendEmptyMessage(202);
        }
    }

    private class InnerUpdateListener implements tmsdk.common.module.update.IUpdateListener {
        private InnerUpdateListener() {
        }

        public void onUpdateStarted() {
            HwLog.i(TencentTrashEngine.TAG, "InnerUpdateListener , onUpdateStarted");
        }

        public void onProgressChanged(UpdateInfo updateInfo, int i) {
            HwLog.i(TencentTrashEngine.TAG, "InnerUpdateListener , onProgressChanged");
        }

        public void onUpdateEvent(UpdateInfo updateInfo, int i) {
        }

        public void onUpdateCanceled() {
        }

        public void onUpdateFinished() {
            HwLog.i(TencentTrashEngine.TAG, "InnerUpdateListener , onUpdateFinished");
            TencentTrashEngine.this.mWechatUpdateFlag = true;
            if (TencentTrashEngine.this.mUpdateHandler != null) {
                TencentTrashEngine.this.mUpdateHandler.sendEmptyMessage(202);
            }
        }
    }

    public TencentTrashEngine(Context context) {
        this.mContext = context;
    }

    public boolean init() {
        this.mDeepcleanManager = (DeepcleanManager) ManagerCreatorF.getManager(DeepcleanManager.class);
        this.mListner = new InnerTaskProcessListener();
        if (this.mDeepcleanManager == null || !this.mDeepcleanManager.init(this.mListner)) {
            HwLog.e(TAG, "TencentTrashEngine init DeepcleanManager faild, mDeepcleanManager = " + this.mDeepcleanManager);
            return false;
        }
        this.mUpdateCallback = new InnerUpdateCallback();
        this.mUpdateHandler = new GenericHandler(this, Looper.getMainLooper());
        this.mInnerUpdateListener = new InnerUpdateListener();
        this.mInnerCheckListener = new InnerCheckListener();
        this.mUpdateManager = (UpdateManager) ManagerCreatorC.getManager(UpdateManager.class);
        return true;
    }

    public Task getScanner(ScanParams p) {
        int type = p.getType();
        switch (type) {
            case 0:
                return creatScanAllTask();
            case 1:
                return new TmsAppScanTask(this.mContext, this.mDeepcleanManager);
            case 3:
                return null;
            case 4:
                HwLog.i(TAG, "Don't use Tencent scanner");
                return null;
            default:
                HwLog.i(TAG, "getScanner params type error!! type:" + type);
                return null;
        }
    }

    public Task createScanTask() {
        TencentScanTask task = new TencentScanTask(this.mContext, this.mDeepcleanManager);
        this.mListner.setScanListener(task.getTencentListener());
        return task;
    }

    public Task creatScanAllTask() {
        Task tencentScanTask = createScanTask();
        Task tencentWechatTask = new TencentWeChatTask(this.mContext);
        Task similarPhotoTask = new SimilarPhotoTask(this.mContext);
        return new CombineTask(this.mContext, "TencentDeepScanner", tencentScanTask, tencentWechatTask, similarPhotoTask);
    }

    public void destory() {
        if (this.mDeepcleanManager != null) {
            this.mDeepcleanManager.onDestory();
        }
    }

    public void update(IUpdateListener listener) {
        this.mUpdateListener = listener;
        if (this.mDeepcleanManager != null) {
            this.mRubbishUpdateFlag = false;
            this.mWechatUpdateFlag = false;
            HwLog.d(TAG, "tencent space clean update start");
            if (this.mUpdateListener != null) {
                this.mUpdateListener.onUpdateStarted();
                this.mUpdateHandler.sendEmptyMessageDelayed(200, 15000);
            }
            this.mDeepcleanManager.updateRubbishData(this.mUpdateCallback);
            new Thread(new Runnable() {
                public void run() {
                    HwLog.i(TencentTrashEngine.TAG, "mUpdateManager start check");
                    TencentTrashEngine.this.mUpdateManager.check(1152921504606846976L, TencentTrashEngine.this.mInnerCheckListener);
                }
            }).start();
            return;
        }
        HwLog.e(TAG, "update called! but mDeepcleanManager is null!!");
    }

    public void onHandleMessage(Message msg) {
        if (msg != null) {
            switch (msg.what) {
                case 200:
                    if (this.mUpdateListener != null) {
                        if (!this.mDeepcleanManager.isUseCloudList()) {
                            HwLog.i(TAG, "MSG_UPDATE_RUBBISH_LIB, faild,isUseCloudList false");
                            this.mUpdateListener.onError(301);
                            break;
                        }
                        HwLog.i(TAG, "MSG_UPDATE_RUBBISH_LIB ,isUseCloudList true");
                        this.mUpdateListener.onUpdateFinished();
                        break;
                    }
                    HwLog.e(TAG, "MSG_UPDATE_RUBBISH_LIB update listener is null, cancel callback");
                    break;
                case 201:
                    HwLog.i(TAG, "MSG_UPDATE_FINISH tencent space clean update finish");
                    this.mUpdateHandler.removeCallbacksAndMessages(null);
                    if (this.mUpdateListener != null) {
                        this.mUpdateListener.onUpdateFinished();
                        break;
                    }
                    break;
                case 202:
                    HwLog.i(TAG, "MSG_CHECK_UPDATE_FINISH");
                    checkUpdateFinish();
                    break;
            }
        }
    }

    private void checkUpdateFinish() {
        HwLog.i(TAG, "checkUpdateFinish start");
        if (this.mWechatUpdateFlag && this.mRubbishUpdateFlag && this.mUpdateHandler != null) {
            HwLog.i(TAG, "checkUpdateFinish checked success");
            this.mUpdateHandler.sendEmptyMessage(201);
        }
    }
}
