package com.huawei.powergenie.modules.apppower.restrict;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import com.huawei.powergenie.api.IAppPowerAction;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IPolicy;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.debugtest.DbgUtils;

public class DeviceIdleCtrl {
    private static Object mDeviceIdleController = null;
    private static boolean mDeviceIdledByPG = false;
    private static boolean mEnableForceDeviceIdle = false;
    private static long mLastCheckIdleTime = 0;
    private static long mPreTotalTxRx = 0;
    private static long mPreTotalTxRxTime = 0;
    private final Context mContext;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    DeviceIdleCtrl.this.setDeviceToIdleIfSatisfiedInternal();
                    DeviceIdleCtrl.this.mWakeLock.release();
                    return;
                default:
                    return;
            }
        }
    };
    private final IAppPowerAction mIAppPowerAction;
    private final ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private final IPolicy mIPolicy;
    private WakeLock mWakeLock = null;

    protected DeviceIdleCtrl(ICoreContext coreContext) {
        this.mICoreContext = coreContext;
        this.mContext = this.mICoreContext.getContext();
        this.mIDeviceState = (IDeviceState) coreContext.getService("device");
        this.mIAppPowerAction = (IAppPowerAction) coreContext.getService("appmamager");
        this.mIPolicy = (IPolicy) coreContext.getService("policy");
        PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
        if (Integer.parseInt(VERSION.SDK) >= 23) {
            this.mWakeLock = pm.newWakeLock(1, "deviceCtrl");
            if (this.mIPolicy.isChinaMarketProduct()) {
                mEnableForceDeviceIdle = true;
                Log.i("DeviceIdleCtrl", "force device idle is enable in china market");
                return;
            }
            mEnableForceDeviceIdle = false;
            Log.i("DeviceIdleCtrl", "force device idle is disabled in oversea market");
            return;
        }
        Log.i("DeviceIdleCtrl", "force device idle is disabled");
    }

    protected void handleAction(PowerAction action) {
        switch (action.getActionId()) {
            case 224:
                triggerEntryDeviceToIdle();
                return;
            case 300:
                if (mEnableForceDeviceIdle) {
                    this.mIAppPowerAction.exitDeviceIdle();
                    return;
                }
                return;
            case 301:
                onScreenOff();
                return;
            default:
                return;
        }
    }

    private void triggerEntryDeviceToIdle() {
        if (!mEnableForceDeviceIdle || !this.mICoreContext.isScreenOff()) {
            return;
        }
        if (this.mIDeviceState.isCharging() && !DbgUtils.DBG_USB) {
            return;
        }
        if (this.mIDeviceState.isCtsRunning()) {
            Log.i("DeviceIdleCtrl", " not to idle for stc!");
        } else if (!this.mIPolicy.isOffPowerMode() && this.mIPolicy.getPowerMode() != 2) {
            setDeviceToIdleIfSatisfied();
        }
    }

    private void setDeviceToIdleIfSatisfied() {
        if (this.mICoreContext != null && mEnableForceDeviceIdle && this.mICoreContext.isScreenOff() && this.mIDeviceState.getScrOffDuration() >= 180000) {
            long now = SystemClock.elapsedRealtime();
            if (now - mLastCheckIdleTime > 120000 && !this.mHandler.hasMessages(100)) {
                mLastCheckIdleTime = now;
                if (!this.mIAppPowerAction.isDeviceIdleMode()) {
                    this.mWakeLock.acquire();
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), 500);
                }
            }
        }
    }

    private void setDeviceToIdleIfSatisfiedInternal() {
        if (this.mICoreContext != null && mEnableForceDeviceIdle && this.mICoreContext.isScreenOff() && this.mIDeviceState.getScrOffDuration() >= 180000) {
            Log.i("DeviceIdleCtrl", "check if now can force device to idle");
            long now = SystemClock.elapsedRealtime();
            long nowTotalTxRx = TrafficStats.getTotalTxBytes() + TrafficStats.getTotalRxBytes();
            if (now - mPreTotalTxRxTime >= 120000) {
                long speed = (nowTotalTxRx - mPreTotalTxRx) / ((now - mPreTotalTxRxTime) / 1000);
                boolean hasDataTransmitting = speed < 4096 ? this.mIDeviceState.is2GNetworkClass() && speed >= 1024 : true;
                boolean calling = this.mIDeviceState.isCalling();
                boolean playingSound = this.mIDeviceState.isPlayingSound();
                boolean hasActiveGps = this.mIDeviceState.hasActiveGps();
                mPreTotalTxRx = nowTotalTxRx;
                mPreTotalTxRxTime = now;
                if (calling || playingSound || hasActiveGps || hasDataTransmitting) {
                    Log.i("DeviceIdleCtrl", "force to idle not exec:calling=" + calling + ",playingSound=" + playingSound + ",ActiveGps=" + hasActiveGps + ",DataTransmitting=" + hasDataTransmitting);
                } else if (this.mIDeviceState.isDisplayOn() || !this.mIDeviceState.isScreenOff()) {
                    Log.i("DeviceIdleCtrl", "display on and not to idle!");
                } else {
                    this.mIAppPowerAction.forceDeviceToIdle();
                }
            }
        }
    }

    private void onScreenOff() {
        if (mEnableForceDeviceIdle) {
            mPreTotalTxRx = TrafficStats.getTotalTxBytes() + TrafficStats.getTotalRxBytes();
            long elapsedRealtime = SystemClock.elapsedRealtime();
            mLastCheckIdleTime = elapsedRealtime;
            mPreTotalTxRxTime = elapsedRealtime;
        }
    }
}
