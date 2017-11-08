package com.huawei.powergenie.modules.apppower.hibernation.states;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import com.huawei.powergenie.modules.apppower.hibernation.ASHLog;
import com.huawei.powergenie.modules.apppower.hibernation.actions.AppActionDoze;

public final class AppStateDoze extends AppState {
    private long mEventStart;
    private boolean mGoHWhenScreenOff = false;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    if (AppStateDoze.this.checkHConditions()) {
                        AppStateDoze.this.goHibernation();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };

    public AppStateDoze(AppStateRecord record) {
        super(record, "doze", new AppActionDoze(record));
    }

    protected void clearDelayMsg() {
        this.mHandler.removeMessages(100);
        this.mHandler.removeMessages(101);
    }

    private void delayToHibernation() {
        if (!this.mHandler.hasMessages(100)) {
            long delay = getDozeDelay();
            ASHLog.d(this.mAppRecord.getPkgName() + " delayToHibernation: " + delay);
            this.mEventStart = SystemClock.elapsedRealtime();
            this.mHandler.sendEmptyMessageDelayed(100, delay);
        }
    }

    public void handleScreenOff() {
        super.handleScreenOff();
        if (this.mAppRecord.isCurrentInputMethod()) {
            ASHLog.i("screen off inputmethod, delay to hibernation");
            delayToHibernation();
        }
    }

    public void handleWidgetEnabled() {
        goRunning("widget_enabled");
    }

    public void handleTopView(boolean disappear) {
        if (disappear) {
            ASHLog.i("top view disappear, delay to hibernation");
            delayToHibernation();
        }
    }

    public void handleWallpaperChanged(boolean enable) {
        if (!enable) {
            ASHLog.i("Application " + this.mAppRecord.getPkgName() + ", wallpaper changed, delay to hibernation");
            delayToHibernation();
        }
    }

    public void handleNotification(boolean newNotice, boolean cancelAll, String opPkg) {
        if (!(newNotice || this.mAppRecord.hasNotification())) {
            ASHLog.i("Application " + this.mAppRecord.getPkgName() + ", notification removed, delay to hibernation");
            delayToHibernation();
        }
    }

    public void checkMsgTimeout() {
        super.checkMsgTimeout();
        if (this.mAppRecord.isScreenOff() && !this.mHandler.hasMessages(101)) {
            long overtime = (SystemClock.elapsedRealtime() - this.mEventStart) - getDozeDelay();
            if (overtime >= 0 && this.mHandler.hasMessages(100)) {
                ASHLog.i(this.mAppRecord.getPkgName() + " goes to hibernation overtime: " + overtime);
                clearDelayMsg();
                if (this.mAppRecord.isImTypeApp() && this.mAppRecord.isConnected()) {
                    this.mHandler.sendEmptyMessageDelayed(100, 3000);
                    this.mHandler.sendEmptyMessageDelayed(101, 3001);
                    return;
                }
                this.mHandler.sendEmptyMessageDelayed(100, 0);
            }
        }
    }

    public void startState() {
        super.startState();
        this.mGoHWhenScreenOff = isGoHWhenScreenOff();
    }

    private long getDozeDelay() {
        if (this.mIPolicy.getPowerMode() == 1 || this.mIPolicy.getPowerMode() == 4 || this.mAppRecord.isIAwareProtectNotCleanApp()) {
            return 15000;
        }
        return 30000;
    }

    public void handleConnectivityChange() {
        if (!this.mAppRecord.isConnected()) {
            ASHLog.i("Application " + this.mAppRecord.getPkgName() + ", Disconnected, delay IM app to hibernation");
            delayToHibernation();
        }
    }

    private boolean checkHConditions() {
        if (this.mGoHWhenScreenOff && this.mAppRecord.isScreenOff()) {
            this.mAppRecord.updateFastUnfreezeState(true);
            return true;
        } else if (doNonFixedConditionCheck()) {
            return doFixedConditionCheck();
        } else {
            delayToHibernation();
            return false;
        }
    }

    private boolean isGoHWhenScreenOff() {
        if (this.mAppRecord.isCurrentInputMethod()) {
            ASHLog.i("current input method: " + this.mAppRecord.getPkgName() + " can enter Hibernation state when screen off!");
            return true;
        } else if (this.mAppRecord.isCurrentLiveWallpaper()) {
            ASHLog.i("current live wallpaer: " + this.mAppRecord.getPkgName() + " can enter Hibernation state when screen off!");
            return true;
        } else if (this.mAppRecord.isCurrentLauncher()) {
            ASHLog.i("current launcher: " + this.mAppRecord.getPkgName() + " can enter Hibernation state when screen off!");
            return true;
        } else if (!this.mAppRecord.isDefaultLauncher()) {
            return false;
        } else {
            ASHLog.i("default launcher: " + this.mAppRecord.getPkgName() + " can enter Hibernation state when screen off!");
            return true;
        }
    }

    private boolean doNonFixedConditionCheck() {
        if (!this.mAppRecord.isVisible() || this.mAppRecord.ignoreFrontActiveGps() || this.mAppRecord.isIgnoreFrontApp()) {
            if (!this.mAppRecord.isScreenOff() && this.mAppRecord.hasNotification()) {
                if (this.mAppRecord.isIgnoreGpsApp() && this.mAppRecord.hasActiveGps()) {
                    ASHLog.i("igonre gps app:" + this.mAppRecord.getPkgName() + " skips notification condition at doze.");
                } else {
                    ASHLog.i("Application " + this.mAppRecord.getPkgName() + " has notification, delay to H!");
                    return false;
                }
            }
            if (this.mAppRecord.hasTopView()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " has top view, delay to H!");
                if (this.mAppRecord.isScreenOff()) {
                    iStats(4, "scnoff_topview");
                }
                return false;
            } else if (this.mAppRecord.isDependedByOtherApp()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is depended by other app, delay to H!");
                iStats(4, "depended");
                return false;
            } else if (this.mAppRecord.isDependedByFrontApp() && !this.mAppRecord.ignoreFrontActiveGps() && !this.mAppRecord.isIgnoreFrontApp()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is depended by front apps, delay to H!");
                iStats(4, "depended_front");
                return false;
            } else if (this.mAppRecord.isDependsAudioActiveApp()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " depends a active audio app, delay to H!");
                iStats(4, "depend_audio");
                return false;
            } else if (!this.mAppRecord.isScreenOff() && this.mAppRecord.hasActiveAppWidget()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " has active app widget, delay to H!");
                return false;
            } else if (this.mAppRecord.hasActiveSensor() && !this.mAppRecord.ignoreActiveGps()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " has active sensor, delay to H!");
                this.mAppRecord.startMotionDetection();
                iStats(4, "active_sensor");
                return false;
            } else if (this.mAppRecord.hasVaildSensor() && !this.mAppRecord.ignoreActiveGps()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " has vaild sensor, delay to H!");
                this.mAppRecord.startMotionDetection();
                iStats(4, "vaild_sensor");
                return false;
            } else if (this.mAppRecord.hasActiveGps() && !this.mAppRecord.ignoreActiveGps()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " has active gps,  delay to H!");
                this.mAppRecord.startMotionDetection();
                iStats(4, "active_gps");
                return false;
            } else if (!this.mAppRecord.isScreenOff() && this.mAppRecord.isConnected() && this.mAppRecord.isImTypeApp()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is im app and connected ok, delay to H!");
                return false;
            } else if (this.mAppRecord.hasDataTransmitting()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " has data transmitting, delay to H!");
                iStats(4, "data_transmit");
                return false;
            } else if (this.mAppRecord.getAppType() == 12 && this.mAppRecord.isCalling()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is music app and calling, delay to H!");
                return false;
            } else if (this.mAppRecord.hasBluetoothConnected()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " has ble connected, delay to H!");
                iStats(4, "ble_app");
                return false;
            } else if (this.mAppRecord.isPkgsSharePid()) {
                ASHLog.i(this.mAppRecord.getPkgName() + " shares pid with other app, delay to H!");
                iStats(4, "shares_pid");
                return false;
            } else if (!this.mAppRecord.hasActiveAudio()) {
                return true;
            } else {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " has active audio, delay to H!");
                return false;
            }
        }
        ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is visible, delay to H!");
        return false;
    }

    private boolean doFixedConditionCheck() {
        if (!this.mAppRecord.isScreenOff() && this.mAppRecord.isCurrentInputMethod()) {
            ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is current input method, can't enter Hibernation state!");
            return false;
        } else if (this.mAppRecord.isCurrentInputMethod() && this.mAppRecord.isMonkeyRunning()) {
            ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is current input method and monkey testing, can't enter Hibernation state!");
            return false;
        } else if (this.mAppRecord.isCurrentLiveWallpaper()) {
            ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is current live wallpaer, can't enter Hibernation state!");
            return false;
        } else if (this.mAppRecord.isCurrentLauncher()) {
            ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is current launcher, can't enter Hibernation state!");
            return false;
        } else if (this.mAppRecord.isDefaultLauncher()) {
            ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is default launcher, can't enter Hibernation state!");
            return false;
        } else {
            if (this.mAppRecord.isAlarmClockApp()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is alarm clock application, can enter Hibernation state!");
            }
            if (!this.mAppRecord.hasIconOnLauncher() && !this.mIAppManager.isStandbyDBExist()) {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is no icon on launcher, can't enter Hibernation state!");
                return false;
            } else if (this.mAppRecord.getAppType() == 17 && this.mAppRecord.isConnected()) {
                ASHLog.i(this.mAppRecord.getPkgName() + " is voip app, can't enter Hibernation state!");
                return false;
            } else if (!this.mAppRecord.isNFCOn() || !this.mAppRecord.isNFCPayApp()) {
                return true;
            } else {
                ASHLog.i("Application " + this.mAppRecord.getPkgName() + " is NFC pay app, can't enter Hibernation state!");
                return false;
            }
        }
    }

    public void processVisibleApp(boolean visible) {
        ASHLog.i("doze package: " + this.mAppRecord.getPkgName() + ", visible: " + visible);
        clearDelayMsg();
        if (visible) {
            goRunning("visible");
        } else {
            delayToHibernation();
        }
    }

    public boolean handleCalledByOtherApp(String exceptionType) {
        if (exceptionType.endsWith("startservice") && this.mHandler.hasMessages(100)) {
            clearDelayMsg();
            delayToHibernation();
        }
        return false;
    }

    public void handleBinderCall(int calledpid) {
        if (this.mHandler.hasMessages(100)) {
            clearDelayMsg();
            ASHLog.i("Binder call " + calledpid + ", delay to H");
            delayToHibernation();
        }
    }

    protected void handleProcessExit() {
        ASHLog.i(this.mAppRecord.getPkgName() + " stay in D when one process exits.");
    }

    public boolean requestHibernate(String reason) {
        ASHLog.i("request: " + this.mAppRecord.getPkgName() + " D to Hibernate!");
        clearDelayMsg();
        goHibernation(reason);
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
