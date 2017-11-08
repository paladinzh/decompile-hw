package com.huawei.systemmanager.antivirus.engine.avast.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.UpdateCheckResultStructure;
import com.avast.android.sdk.engine.UpdateCheckResultStructure.UpdateCheck;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class UpdateManager {
    private static final String TAG = "UpdateManager";
    private IUpdateListener mListener;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null) {
                String action = intent.getAction();
                if (!TextUtils.isEmpty(action)) {
                    HwLog.i(UpdateManager.TAG, "action:" + action);
                    if (UpdateUtils.ACTION_VIRUS_UPDATE_FINISH.equals(action)) {
                        UpdateManager.this.handlerUpdateFinish(intent.getBooleanExtra(UpdateUtils.KEY_UPDATE_RESULT, true));
                    } else if (UpdateUtils.ACTION_VIRUS_UPDATE_START.equals(action)) {
                        UpdateManager.this.handlerUpdateStart();
                    }
                }
            }
        }
    };

    public UpdateManager() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UpdateUtils.ACTION_VIRUS_UPDATE_START);
        filter.addAction(UpdateUtils.ACTION_VIRUS_UPDATE_FINISH);
        GlobalContext.getContext().registerReceiver(this.mReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        this.mListener = null;
    }

    public void destory() {
        GlobalContext.getContext().unregisterReceiver(this.mReceiver);
        this.mReceiver = null;
        this.mListener = null;
    }

    private void handlerUpdateFinish(boolean isUpdateSucess) {
        if (this.mListener != null) {
            if (isUpdateSucess) {
                this.mListener.onUpdateFinished();
            } else {
                this.mListener.onUpdateEvent(null, -1);
            }
        }
    }

    private void handlerUpdateStart() {
        if (this.mListener != null) {
            this.mListener.onUpdateStarted();
        }
    }

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    public void checkUpdateAvailable(final Context context, final ICheckListener checkListener) {
        if (checkListener != null) {
            checkListener.onCheckStarted();
            new Thread("thread_avast_check_lib") {
                public void run() {
                    UpdateCheckResultStructure result = EngineInterface.isUpdateAvailable(context);
                    HwLog.i(UpdateManager.TAG, "UpdateCheckResultStructure is:" + result.checkResult);
                    if (result.checkResult == UpdateCheck.RESULT_UP_TO_DATE) {
                        checkListener.onCheckFinished(false);
                    } else if (result.checkResult != UpdateCheck.RESULT_UPDATE_AVAILABLE) {
                        checkListener.onCheckEvent(-1);
                        checkListener.onCheckFinished(false);
                    } else {
                        checkListener.onCheckFinished(true);
                    }
                }
            }.start();
        }
    }

    public void update(IUpdateListener updateListener) {
        if (updateListener != null) {
            this.mListener = updateListener;
            innerUpdate();
        }
    }

    private void innerUpdate() {
        GlobalContext.getContext().startService(new Intent(GlobalContext.getContext(), UpdateService.class));
    }
}
