package com.huawei.keyguard.data;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor.BatteryStatus;
import com.android.keyguard.R$integer;
import com.android.keyguard.R$string;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import fyusion.vislib.BuildConfig;
import java.text.NumberFormat;

public class BatteryStateInfo {
    private static final BatteryStateInfo sBatteryInfo = new BatteryStateInfo();
    private static int sFastThreshold = -1;
    private static int sSlowThreshold = -1;
    private BatteryStatus mBatteryStatus = null;
    private int mChargeLevel = 100;
    private int mChargingSpeed = -1;
    private boolean mIsCharged = false;
    private int mIsInCharging = -1;
    private boolean mIsLow = false;
    private boolean mPlugIn = false;
    private int mQuickCharge = -1;
    private int mStatus = 1;
    private int mSuperCharge = -1;

    public static BatteryStateInfo getInst() {
        return sBatteryInfo;
    }

    public String getChargePercent(Context context) {
        return context.getString(R$string.charge_percent, new Object[]{Integer.valueOf(this.mChargeLevel)});
    }

    public int getChargeLevel() {
        return this.mChargeLevel;
    }

    public int getQuickCharge() {
        return this.mQuickCharge;
    }

    public int getSuperCharge() {
        return this.mSuperCharge;
    }

    public static void setQuickCharge(String quickCharge) {
        if (TextUtils.isEmpty(quickCharge)) {
            HwLog.w("KgBattery", "quickCharge is null");
            return;
        }
        if (Integer.parseInt(quickCharge) == 1) {
            sBatteryInfo.mQuickCharge = 1;
        } else if (Integer.parseInt(quickCharge) == 2) {
            sBatteryInfo.mSuperCharge = 2;
        } else {
            sBatteryInfo.mQuickCharge = -1;
            sBatteryInfo.mSuperCharge = -1;
        }
    }

    public boolean showBatteryInfo() {
        return this.mPlugIn || this.mIsLow || this.mStatus == 2;
    }

    public String getBatteryInfo2(Context context) {
        if (this.mIsCharged) {
            return context.getString(R$string.keyguard_charged);
        }
        if (this.mPlugIn || this.mStatus == 2) {
            return getChargeInfo(context, HwUnlockUtils.getChargingType());
        }
        if (this.mIsLow) {
            return context.getString(R$string.emui40_lockscreen_low_battery, new Object[]{NumberFormat.getPercentInstance().format(((double) this.mChargeLevel) / 100.0d)});
        }
        Log.w("KgBattery", "BatteryInfo need be hidden 2");
        return BuildConfig.FLAVOR;
    }

    public String getBatteryInfo(Context context) {
        if (this.mIsCharged) {
            return context.getString(R$string.lockscreen_charged);
        }
        if (this.mPlugIn || this.mStatus == 2) {
            return getChargeInfo(context, HwUnlockUtils.getChargingType());
        }
        if (this.mIsLow) {
            return context.getString(R$string.emui40_lockscreen_low_battery, new Object[]{NumberFormat.getPercentInstance().format(((double) this.mChargeLevel) / 100.0d)});
        }
        Log.w("KgBattery", "BatteryInfo need be hidden");
        return BuildConfig.FLAVOR;
    }

    private String getChargeInfo(Context context, String type) {
        Log.w("KgBattery", "getCharge info with type: " + type);
        if ("text".equalsIgnoreCase(type)) {
            NumberFormat pnf = NumberFormat.getPercentInstance();
            return context.getString(chargeLevelResID(), new Object[]{pnf.format(((double) this.mChargeLevel) / 100.0d)});
        } else if ("number".equalsIgnoreCase(type)) {
            return String.valueOf(this.mChargeLevel);
        } else {
            return context.getString(R$string.charge_percent, new Object[]{Integer.valueOf(this.mChargeLevel)});
        }
    }

    public int chargeLevelResID() {
        if (isSuperCharge()) {
            return R$string.keyguard_plugged_in2_superquick;
        }
        if (isQuickCharge()) {
            return R$string.emui41_lockscreen_plugged_in2_quick;
        }
        return R$string.lockscreen_plugged_in2;
    }

    public boolean isExhaustBatteryLevel() {
        return getChargeLevel() < 5;
    }

    public boolean isQuickCharge() {
        return getQuickCharge() == 1;
    }

    public boolean isSuperCharge() {
        return getSuperCharge() == 2;
    }

    public boolean isCharge() {
        return (this.mPlugIn && this.mIsCharged) || this.mStatus == 2;
    }

    public void updateBatteryInfo(Context context, BatteryStatus status) {
        if (status == null || (this.mBatteryStatus != null && status == this.mBatteryStatus)) {
            Log.v("KgBattery", "Skip BatteryInfo change.");
            return;
        }
        this.mBatteryStatus = status;
        this.mStatus = status.status;
        this.mPlugIn = status.isPluggedIn();
        this.mChargeLevel = status.level;
        this.mIsCharged = status.isCharged();
        this.mIsLow = status.isBatteryLow();
        this.mChargingSpeed = getChargeSpeed(context, status);
        Log.i("KgBattery", "BatteryInfo updated:  " + this.mBatteryStatus.status + (this.mIsLow ? 'L' : 'l') + (this.mPlugIn ? 'P' : 'p') + (this.mIsCharged ? 'C' : 'c') + this.mChargingSpeed);
        syncToUI();
    }

    public static int getChargeSpeed(Context context, BatteryStatus status) {
        if (sSlowThreshold == -1 || sFastThreshold == -1) {
            Resources res = context.getResources();
            sSlowThreshold = res.getInteger(R$integer.config_chargingSlowlyThreshold);
            sFastThreshold = res.getInteger(R$integer.config_chargingFastThreshold);
        }
        return status.getChargingSpeed(sSlowThreshold, sFastThreshold);
    }

    private void syncToUI() {
        int i;
        boolean notice;
        int oldState = this.mIsInCharging;
        if (isCharge()) {
            i = 1;
        } else {
            i = 0;
        }
        this.mIsInCharging = i;
        if (oldState == -1 || this.mIsInCharging == oldState) {
            notice = false;
        } else {
            notice = true;
        }
        AppHandler.sendMessage(100, Boolean.valueOf(notice));
    }

    public int getChargingMode() {
        if (isQuickCharge()) {
            return 1;
        }
        return isSuperCharge() ? 2 : 0;
    }
}
