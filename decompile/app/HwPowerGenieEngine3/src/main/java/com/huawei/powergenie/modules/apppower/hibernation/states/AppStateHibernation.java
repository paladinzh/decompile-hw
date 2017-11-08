package com.huawei.powergenie.modules.apppower.hibernation.states;

import android.os.Handler;
import android.os.Message;
import com.huawei.powergenie.modules.apppower.hibernation.ASHLog;
import com.huawei.powergenie.modules.apppower.hibernation.actions.AppActionHibernation;

public final class AppStateHibernation extends AppState {
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    AppStateHibernation.this.goRunning("mm_monitor");
                    return;
                default:
                    return;
            }
        }
    };

    public AppStateHibernation(AppStateRecord record) {
        super(record, "hibernation", new AppActionHibernation(record));
    }

    public void startState() {
        super.startState();
        if ("com.tencent.mm".equals(this.mAppRecord.getPkgName())) {
            this.mHandler.removeMessages(1000);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1000), 300000);
        }
    }

    protected void handleProcessStart() {
        goRunning("proc_start");
    }

    public void handleWallpaperChanged(boolean enable) {
        ASHLog.i(this.mAppRecord.getPkgName() + " is live wallpaer, H to R!");
        goRunning("wallpaper_changed");
    }

    public void handleNotification(boolean newNotice, boolean cancelAll, String opPkg) {
        if (opPkg == null || opPkg.equals(this.mAppRecord.getPkgName()) || !"com.huawei.android.pushagent".equals(opPkg)) {
            ASHLog.i(this.mAppRecord.getPkgName() + " notification, H to R!");
            goRunning("add_notification");
            return;
        }
        ASHLog.i("Notification Center send " + this.mAppRecord.getPkgName() + " notification, do nothing");
    }

    public void handleBinderCall(int calledpid) {
        ASHLog.i("Binder call " + calledpid + ", H to R!");
        goRunning("binder_call");
    }

    public void handleWidgetEnabled() {
        ASHLog.i(this.mAppRecord.getPkgName() + " enable widget, H to R!");
        goRunning("widget_enabled");
    }

    public void handleUserWalking() {
        if (this.mAppRecord.restoreActiveGps()) {
            ASHLog.i(this.mAppRecord.getPkgName() + " start walking, H to R!");
            goRunning("start_walking");
        }
    }

    public void handleNFCPayChg() {
        ASHLog.i(this.mAppRecord.getPkgName() + " is default pay app, H to R!");
        goRunning("nfc_app");
    }

    public void handleCallBusy() {
        ASHLog.i(this.mAppRecord.getPkgName() + " call busy, H to R!");
        goRunning("call_busy");
    }

    public void handleUnfreezeDependPids() {
        goRunning("depend_pids");
    }

    public void processVisibleApp(boolean visible) {
        if (visible) {
            ASHLog.i(this.mAppRecord.getPkgName() + " visible, H to R!");
            goRunning("visible");
        }
    }

    public boolean requestRunning(String reason) {
        ASHLog.i(this.mAppRecord.getPkgName() + " request for " + reason + ", H to R!");
        goRunning(reason);
        return true;
    }

    public boolean handleCalledByOtherApp(String exceptionType) {
        ASHLog.i(this.mAppRecord.getPkgName() + " called by other app, H to R!");
        goRunning("be_called");
        return true;
    }

    public void handleFastUnfreezeApp() {
        if (this.mAppRecord.isNeedFastUnfreeze()) {
            this.mAppRecord.updateFastUnfreezeState(false);
            goRunning("fast_unfreeze");
        }
    }

    public void handleScreenOn() {
        if (this.mAppRecord.isCurrentInputMethod()) {
            goRunning("scron_input");
        } else if (this.mAppRecord.isScrOffRejectMsgApp()) {
            ASHLog.i("in extreme mode can't unfreeze " + this.mAppRecord.getPkgName() + " skips screen on.");
        } else if (this.mAppRecord.isImTypeApp() && this.mAppRecord.isConnected()) {
            goRunning("scron_im");
        } else {
            if (this.mAppRecord.hasNotification() && !this.mAppRecord.isKeyguardSecure()) {
                if (this.mAppRecord.isIgnoreGpsApp() && this.mAppRecord.hasActiveGps()) {
                    ASHLog.i("igonre gps app:" + this.mAppRecord.getPkgName() + " skips notification when screen on.");
                } else {
                    goRunning("scron_notfication");
                    return;
                }
            }
            if (this.mAppRecord.hasActiveAppWidget() && !this.mAppRecord.isKeyguardPresent()) {
                goRunning("scron_appwidget");
            } else if (this.mAppRecord.restoreActiveGps()) {
                goRunning("scron_gps");
            } else if (this.mAppRecord.hasActiveSensor()) {
                goRunning("scron_sensor");
            }
        }
    }

    public void handleSreenUnlock() {
        if (this.mAppRecord.hasActiveAppWidget()) {
            goRunning("unlock_appwidget");
        } else if (this.mAppRecord.hasNotification()) {
            goRunning("unlock_notfication");
        } else if (this.mAppRecord.isScrOffRejectMsgApp() && this.mAppRecord.isConnected()) {
            goRunning("unlock_im_extreme");
        }
    }

    public void handleBCOverflow() {
        ASHLog.i(this.mAppRecord.getPkgName() + " broadcast overflow, H to R!");
        goRunning("bc_overflow");
    }

    public void handleBCNotify(String action) {
        ASHLog.i(this.mAppRecord.getPkgName() + " broadcast notify, H to R!");
        goRunning("bc_notify_" + action);
    }

    public void handleBroadcastANR() {
        ASHLog.i(this.mAppRecord.getPkgName() + " broadcast will anr, H to R!");
        goRunning("bc_anr");
    }

    public void handleNetPacket() {
        if (notUnfreeze()) {
            ASHLog.i("Don't care net packet in extreme mode scr off.");
            return;
        }
        ASHLog.i(this.mAppRecord.getPkgName() + " net packet, H to R!");
        goRunning("net_packet");
    }

    public void handleConnectivityChange() {
        if (notUnfreeze()) {
            ASHLog.i("Don't care connect state in extreme mode scr off.");
            return;
        }
        if (this.mAppRecord.isConnected()) {
            ASHLog.i(this.mAppRecord.getPkgName() + " is im app and connected ok, H to R!");
            goRunning("net_connected");
        }
    }

    public void handleAudioStart() {
        ASHLog.i(this.mAppRecord.getPkgName() + " gain audio focus, H to R!");
        goRunning("audio_start");
    }

    public void handleAppsAlarm() {
        super.handleAppsAlarm();
        if (notUnfreeze()) {
            ASHLog.i("Don't care alarm in extreme mode scr off.");
            return;
        }
        if (getDuration() > 10000) {
            ASHLog.i(this.mAppRecord.getPkgName() + " alarm trigger, H to R!");
            goRunning("alarm");
        } else {
            ASHLog.i(this.mAppRecord.getPkgName() + " stay in H duration:" + getDuration() + "ms, not process alarm.");
        }
    }

    public void checkMsgTimeout() {
        super.checkMsgTimeout();
        if (notUnfreeze()) {
            ASHLog.i("Don't care msg time out in extreme mode scr off.");
            return;
        }
        boolean isImType = this.mAppRecord.isImTypeApp();
        if ((isImType || this.mAppRecord.isProtectAppByUser()) && this.mAppRecord.getDuration() >= 300000) {
            String appType;
            String reason;
            if (isImType) {
                appType = " im";
                reason = "alarm_im";
            } else {
                appType = " clean_protect";
                reason = "alarm_clean_protect";
            }
            if (!this.mAppRecord.isConnected() || !this.mAppRecord.isListenerNetPackets()) {
                ASHLog.i(this.mAppRecord.getPkgName() + appType + " app keeps in H for no net!");
            } else if (this.mAppRecord.getBastetProxyState()) {
                ASHLog.i(this.mAppRecord.getPkgName() + appType + " app, bastet has proxy heartbeat, stay in H!");
            } else {
                ASHLog.i(this.mAppRecord.getPkgName() + appType + " app alarm trigger, H to R!");
                goRunning(reason);
            }
        }
    }

    private boolean notUnfreeze() {
        if (this.mAppRecord.isScreenOff() && this.mAppRecord.isScrOffRejectMsgApp()) {
            return true;
        }
        return false;
    }

    protected void clearDelayMsg() {
        this.mHandler.removeMessages(1000);
    }
}
