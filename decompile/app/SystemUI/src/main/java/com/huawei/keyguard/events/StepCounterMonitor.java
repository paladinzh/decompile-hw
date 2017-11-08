package com.huawei.keyguard.events;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemProperties;
import com.huawei.keyguard.data.StepCounterInfo;
import com.huawei.keyguard.events.EventCenter.IEventListener;
import com.huawei.keyguard.events.MonitorImpl.MonitorChangeListener;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;

public class StepCounterMonitor extends MonitorImpl implements IEventListener {
    private static boolean mHasHealthPackage = false;
    private boolean bootCompleted = "1".equals(SystemProperties.get("sys.boot_completed", "0"));
    public int stepsNum = 0;

    public StepCounterMonitor(Context context, MonitorChangeListener callback, int monitorId) {
        super(context, callback, monitorId);
        EventCenter.getInst().listen(10, this);
        setHasHealthPackage(HwUnlockUtils.hasPackageInfo(context.getPackageManager(), "com.huawei.health"));
    }

    public static boolean getHasHealthPackage() {
        return mHasHealthPackage;
    }

    private static void setHasHealthPackage(boolean hasHealthPackage) {
        mHasHealthPackage = hasHealthPackage;
    }

    public void register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.com.huawei.bone.ENABLE_PHONE_STEP_COUNTER");
        registerBroadcast(filter, "com.android.keyguard.permission.SEND_STEP_INFO_COUNTER");
        requestStepInfoPositive(this.bootCompleted);
    }

    protected boolean onPreBrocastReceive(Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        StepCounterInfo info = StepCounterInfo.getInst();
        if (bundle == null) {
            HwLog.w("StepCounterMonitor", "onPreBrocastReceive, bundle is null!");
            return false;
        }
        if ("android.com.huawei.bone.NOTIFY_SPORT_DATA".equalsIgnoreCase(action)) {
            info.setStepsCount(bundle.getInt("steps"));
            this.mCallback.onMonitorChanged(this.mMonitorId, info);
        } else if ("android.com.huawei.bone.ENABLE_PHONE_STEP_COUNTER".equalsIgnoreCase(action)) {
            info.setEnableCounterChanged(true);
            info.setEnableCounter(bundle.getBoolean("state"));
            this.mCallback.onMonitorChanged(this.mMonitorId, info);
        }
        return false;
    }

    public boolean onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            HwLog.w("StepCounterMonitor", "onReceive action: Null");
            return false;
        }
        String action = intent.getAction();
        StepCounterInfo info = StepCounterInfo.getInst();
        if ("com.huawei.health.ENABLE_STEP_INFO_SHOW".equals(action)) {
            requestStepInfoPositive(true);
            info.setStepInfoShowEnable(intent.getBooleanExtra("state", true));
            info.setStepsCount(this.stepsNum);
            this.mCallback.onMonitorChanged(this.mMonitorId, info);
            return true;
        } else if ("android.com.huawei.bone.NOTIFY_SPORT_DATA".equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null || this.stepsNum == bundle.getInt("steps")) {
                return false;
            }
            this.stepsNum = bundle.getInt("steps");
            info.setStepsCount(this.stepsNum);
            this.mCallback.onMonitorChanged(this.mMonitorId, info);
            return true;
        } else if (intent.getData() == null) {
            HwLog.w("StepCounterMonitor", "onReceive getData: Null");
            return false;
        } else {
            if (!"com.huawei.health".equalsIgnoreCase(intent.getData().getEncodedSchemeSpecificPart())) {
                return false;
            }
            if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                HwUpdateMonitor.getInstance(context).clearStepCountInfo();
                setHasHealthPackage(false);
                info.clearStepInfo();
                this.mCallback.onMonitorChanged(this.mMonitorId, info);
                return true;
            } else if (!"android.intent.action.PACKAGE_ADDED".equals(action)) {
                return false;
            } else {
                info.setStepInfoShowEnable(true);
                this.mCallback.onMonitorChanged(this.mMonitorId, info);
                return true;
            }
        }
    }

    public void requestStepInfoPositive(boolean isSystemReady) {
        if (this.mContext != null && isSystemReady) {
            this.mContext.sendBroadcast(new Intent("com.android.keyguard.action.REQUEST_STEP_INFO"), "com.android.keyguard.permission.RECEIVE_COVERSCREEN_STATE");
            HwLog.w("StepCounterMonitor", "requestStepInfoPositive sendBroadcast");
        }
    }

    Object onQueryDatabase() {
        return null;
    }
}
