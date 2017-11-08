package com.huawei.powergenie.modules.apppower.hibernation.states;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import com.huawei.powergenie.modules.apppower.hibernation.ASHLog;
import com.huawei.powergenie.modules.apppower.hibernation.actions.AppActionRunning;

public final class AppStateRunning extends AppState {
    private long mEventStart;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    if (AppStateRunning.this.checkDConditions()) {
                        AppStateRunning.this.goDoze();
                        return;
                    } else {
                        AppStateRunning.this.delayToDoze();
                        return;
                    }
                default:
                    return;
            }
        }
    };

    public AppStateRunning(AppStateRecord record) {
        super(record, "running", new AppActionRunning(record));
    }

    private void delayToDoze() {
        if (!this.mHandler.hasMessages(100)) {
            this.mEventStart = SystemClock.elapsedRealtime();
            this.mHandler.sendEmptyMessageDelayed(100, getRunningDelay());
        }
    }

    protected void clearDelayMsg() {
        this.mHandler.removeMessages(100);
    }

    private boolean checkDConditions() {
        if (this.mAppRecord.hasActiveAudio()) {
            ASHLog.i(this.mAppRecord.getPkgName() + " has active audio, delay to D!");
            return false;
        } else if (this.mAppRecord.hasDataTransmitting()) {
            ASHLog.i(this.mAppRecord.getPkgName() + " has data transmitting, delay to D!");
            iStats(4, "data_transmit");
            return false;
        } else if (this.mAppRecord.hasActiveGps() && !this.mAppRecord.ignoreActiveGps()) {
            ASHLog.i(this.mAppRecord.getPkgName() + " has active gps, delay to D!");
            this.mAppRecord.startMotionDetection();
            iStats(4, "active_gps");
            return false;
        } else if (this.mAppRecord.isDependedByFrontApp() && !this.mAppRecord.ignoreFrontActiveGps() && !this.mAppRecord.isIgnoreFrontApp()) {
            ASHLog.i(this.mAppRecord.getPkgName() + " is depended by front apps, delay to D!");
            iStats(4, "depended_front");
            return false;
        } else if (this.mAppRecord.getPkgName() == null || !this.mAppRecord.getPkgName().contains("com.google.android.wearable.app") || !this.mIAppManager.isCleanDBExist() || this.mIAppManager.isCleanUnprotectApp(this.mAppRecord.getPkgName())) {
            return true;
        } else {
            ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is protect app, delay to D!");
            return false;
        }
    }

    public void checkMsgTimeout() {
        super.checkMsgTimeout();
        if (this.mAppRecord.isScreenOff() && this.mHandler.hasMessages(100)) {
            long overtime = (SystemClock.elapsedRealtime() - this.mEventStart) - getRunningDelay();
            if (overtime >= 0) {
                ASHLog.i(this.mAppRecord.getPkgName() + " goes to D overtime: " + overtime);
                clearDelayMsg();
                this.mHandler.sendEmptyMessage(100);
            }
        }
    }

    private long getRunningDelay() {
        if (this.mIPolicy.getPowerMode() == 1 || this.mIPolicy.getPowerMode() == 4 || this.mAppRecord.isIAwareProtectNotCleanApp()) {
            return 15000;
        }
        return 30000;
    }

    public void processVisibleApp(boolean visible) {
        ASHLog.i("running package: " + this.mAppRecord.getPkgName() + ", visible: " + visible);
        clearDelayMsg();
        if (!visible) {
            delayToDoze();
        }
    }

    public boolean requestHibernate(String reason) {
        ASHLog.i("request: " + this.mAppRecord.getPkgName() + " R to Hibernate!");
        clearDelayMsg();
        goHibernation(reason);
        return true;
    }

    public boolean handleCalledByOtherApp(String exceptionType) {
        if (!exceptionType.endsWith("startservice") || !this.mHandler.hasMessages(100)) {
            return false;
        }
        clearDelayMsg();
        delayToDoze();
        return true;
    }

    public void handleActiveHighPowerGps() {
        if (!this.mAppRecord.isScreenOff()) {
            return;
        }
        if (this.mAppRecord.ignoreFrontActiveGps() || this.mAppRecord.ignoreNavigationApp()) {
            clearDelayMsg();
            this.mHandler.sendEmptyMessage(100);
        }
    }
}
