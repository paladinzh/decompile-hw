package com.huawei.systemmanager.power.model;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import com.huawei.android.net.ConnectivityManagerEx;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.util.HwLog;

public class PowerModeControl {
    public static final String CHANGE_MODE_ACTION = "huawei.intent.action.POWER_MODE_CHANGED_ACTION";
    private static final String CHANGE_MODE_STATE = "state";
    public static final String DB_BATTERY_PERCENT_SWITCH = "battery_percent_switch";
    public static final String DB_PERCENT_SWITCH_STATUS_ENTER_SAVEMODE = "percent_status_savemode";
    public static final String DB_PERCENT_SWITCH_STATUS_ENTER_SUPERSAVEMODE = "percent_status_supersave";
    private static final int DELAY_TIME = 500;
    public static final int GENIE_SAVE_MODE = 1;
    public static final int GENIE_SMART_MODE = 2;
    private static final int MSG_CHANGE_MODE = 1;
    private static final String TAG = PowerModeControl.class.getSimpleName();
    private static PowerModeControl mPowerModeControl;
    private final Context mContext;
    private Handler mHandler;
    private final HandlerThread mHandlerThread = new HandlerThread(TAG);

    private class BarHandler extends Handler {
        public BarHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HwLog.i(PowerModeControl.TAG, "handleMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                    PowerModeControl.this.handlePowerModeSwitch(((Integer) msg.obj).intValue());
                    return;
                default:
                    return;
            }
        }
    }

    private PowerModeControl(Context context) {
        this.mContext = context.getApplicationContext();
        this.mHandlerThread.start();
    }

    public static synchronized PowerModeControl getInstance(Context context) {
        PowerModeControl powerModeControl;
        synchronized (PowerModeControl.class) {
            if (mPowerModeControl == null) {
                mPowerModeControl = new PowerModeControl(context);
            }
            powerModeControl = mPowerModeControl;
        }
        return powerModeControl;
    }

    public void changePowerMode(int powerModeNum) {
        sendMessage(1, Integer.valueOf(powerModeNum));
    }

    private void sendMessage(int what, Object obj) {
        synchronized (this) {
            if (this.mHandler == null) {
                Looper looper = this.mHandlerThread.getLooper();
                if (looper == null) {
                    HwLog.e(TAG, "hanlderthread looper is null!!!!!");
                    return;
                }
                this.mHandler = new BarHandler(looper);
            }
            Message msg = this.mHandler.obtainMessage(what, obj);
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessageDelayed(msg, 500);
        }
    }

    public void recordBatteryPercentStatusForSuperMode() {
        boolean mIsBatteryPercent;
        if (System.getInt(this.mContext.getContentResolver(), DB_BATTERY_PERCENT_SWITCH, 0) != 0) {
            mIsBatteryPercent = true;
        } else {
            mIsBatteryPercent = false;
        }
        if (mIsBatteryPercent) {
            Global.putInt(this.mContext.getContentResolver(), DB_PERCENT_SWITCH_STATUS_ENTER_SUPERSAVEMODE, 1);
        } else {
            Global.putInt(this.mContext.getContentResolver(), DB_PERCENT_SWITCH_STATUS_ENTER_SUPERSAVEMODE, 0);
        }
    }

    public void recordBatteryPercentStatusForSaveMode() {
        boolean mIsBatteryPercent;
        if (System.getInt(this.mContext.getContentResolver(), DB_BATTERY_PERCENT_SWITCH, 0) != 0) {
            mIsBatteryPercent = true;
        } else {
            mIsBatteryPercent = false;
        }
        if (mIsBatteryPercent) {
            Global.putInt(this.mContext.getContentResolver(), DB_PERCENT_SWITCH_STATUS_ENTER_SAVEMODE, 1);
        } else {
            Global.putInt(this.mContext.getContentResolver(), DB_PERCENT_SWITCH_STATUS_ENTER_SAVEMODE, 0);
        }
    }

    public boolean getPercentStatusEnterSaveMode() {
        if (Global.getInt(this.mContext.getContentResolver(), DB_PERCENT_SWITCH_STATUS_ENTER_SAVEMODE, 1) == 0) {
            return true;
        }
        return false;
    }

    private void handlePowerModeSwitch(int powerModeNum) {
        if (powerModeNum == 1) {
            if (getPercentStatusEnterSaveMode()) {
                System.putInt(this.mContext.getContentResolver(), DB_BATTERY_PERCENT_SWITCH, 0);
            }
            setConnect(this.mContext, "normal_level");
            wirtePowerMode(1, 2);
            HwLog.i(TAG, "handlePowerModeSwitch to SmartMode, settings db SmartModeStatus= " + powerModeNum + " ,broadcast genieValue= " + 2);
        } else if (powerModeNum == 4) {
            recordBatteryPercentStatusForSaveMode();
            System.putInt(this.mContext.getContentResolver(), DB_BATTERY_PERCENT_SWITCH, 1);
            setConnect(this.mContext, "normal_level");
            wirtePowerMode(4, 1);
            HwLog.i(TAG, "handlePowerModeSwitch to SaveMode, settings db SmartModeStatus= " + powerModeNum + " ,broadcast genieValue= " + 1);
        }
    }

    public void wirtePowerMode(int mSaveMode, int genieValue) {
        if (readSaveMode() == mSaveMode) {
            HwLog.i(TAG, "the current powerMode is same with change mode, do nothing.");
            return;
        }
        System.putIntForUser(this.mContext.getContentResolver(), ApplicationConstant.SMART_MODE_STATUS, mSaveMode, 0);
        Intent intent = new Intent(CHANGE_MODE_ACTION);
        intent.putExtra("state", genieValue);
        this.mContext.sendBroadcast(intent);
    }

    public int readSaveMode() {
        return System.getIntForUser(this.mContext.getContentResolver(), ApplicationConstant.SMART_MODE_STATUS, 1, 0);
    }

    private void setConnect(Context context, String level) {
        ConnectivityManagerEx.getDefault().setSmartKeyguardLevel(level);
    }
}
