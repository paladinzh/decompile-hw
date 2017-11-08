package com.huawei.keyguard;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.ConfigurationEx;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import com.android.keyguard.R$bool;
import com.huawei.keyguard.data.SportInfo;
import com.huawei.keyguard.util.HwLog;
import huawei.android.os.HwGeneralManager;

public class KeyguardCfg {
    private static final boolean mBkPinEnabled = SystemProperties.getBoolean("ro.keyguard.hwbkpin", true);
    private static final boolean mFpPasswordTimeout = SystemProperties.getBoolean("ro.config.fp_timeout_password", false);
    private static final boolean mFrontFpNavigation = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private static final boolean mIsDefaultPortOrientation;
    private static boolean mIsVerifying = false;
    private static boolean sCameraExist = false;
    private static int sIsCredentialProtected = -1;
    private static int sIsCurveScreen = -1;
    private static boolean sIsEnableMagazineUpdate = true;
    private static boolean sIsEnableNetwork = true;
    private static int sIsUseVSuperChargeIcon = -1;

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90 != 0) {
            z = false;
        }
        mIsDefaultPortOrientation = z;
    }

    public static boolean isDoubleLockOn(Context context) {
        return true;
    }

    public static final void init(Context context) {
        if (context != null) {
            sIsEnableMagazineUpdate = context.getResources().getBoolean(R$bool.enable_update_magazine);
        }
        if (!sIsEnableMagazineUpdate || SystemProperties.getBoolean("ro.config.keyguard_unusedata", false)) {
            sIsEnableNetwork = false;
        }
        if (context != null) {
            setMagzieUpdateInSettings(context);
        }
        SportInfo.getInst().initHealthSDK(context);
    }

    public static void setMagazineUpdateDisabled() {
        sIsEnableNetwork = false;
        sIsEnableMagazineUpdate = false;
    }

    public static boolean isNetworkEnabled() {
        return sIsEnableNetwork;
    }

    public static boolean isMagazineUpdateEnabled() {
        return sIsEnableMagazineUpdate;
    }

    public static boolean isExtremePowerSavingMode() {
        return SystemProperties.getBoolean("sys.super_power_save", false);
    }

    public static boolean isSimpleModeOn() {
        boolean z = false;
        Configuration curConfig = new Configuration();
        try {
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            ConfigurationEx mExtraConfig = new com.huawei.android.content.res.ConfigurationEx(curConfig).getExtraConfig();
            if (mExtraConfig != null && 2 == mExtraConfig.simpleuiMode) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            HwLog.e("KeyguardCfg", "isSimpleModeOn exception");
            return false;
        } catch (NoSuchFieldError e2) {
            HwLog.e("KeyguardCfg", "isSimpleModeOn NoSuchFieldError");
            return false;
        }
    }

    public static boolean isBackupPinEnabled() {
        return mBkPinEnabled;
    }

    public static final void setCameraExists(boolean enabled) {
        sCameraExist = enabled;
    }

    public static final void setMagzieUpdateInSettings(Context context) {
        Global.putInt(context.getContentResolver(), "magazine_unlock_enabled", sIsEnableMagazineUpdate ? 1 : 0);
    }

    public static final boolean isCameraExists() {
        return sCameraExist;
    }

    public static boolean isCurveScreen(Context context) {
        if (sIsCurveScreen == -1) {
            sIsCurveScreen = 0;
            try {
                int i;
                if (HwGeneralManager.getInstance().isCurveScreen()) {
                    i = 1;
                } else {
                    i = 0;
                }
                sIsCurveScreen = i;
            } catch (Exception e) {
                HwLog.e("KeyguardCfg", "isCurveScreen fail", e);
            } catch (NoClassDefFoundError e2) {
                HwLog.e("KeyguardCfg", "isCurveScreen HwGeneralManager not support", e2);
            } catch (UnsatisfiedLinkError e3) {
                HwLog.e("KeyguardCfg", "HwGeneralManager isSupportForce UnsatisfiedLinkError", e3);
            } catch (NoSuchMethodError e4) {
                HwLog.e("KeyguardCfg", "HwGeneralManager isSupportForce NoSuchMethodError", e4);
            }
            HwLog.w("KeyguardCfg", "IsCurveScreen : " + sIsCurveScreen);
        }
        if (sIsCurveScreen == 1) {
            return true;
        }
        return false;
    }

    public static boolean isFrontFpNavigationSupport() {
        return mFrontFpNavigation;
    }

    public static boolean isFpPerformanceOpen() {
        return true;
    }

    public static boolean isCredentialProtected(Context context) {
        if (sIsCredentialProtected == -1) {
            int i;
            if (StorageManager.isFileEncryptedNativeOrEmulated()) {
                i = 1;
            } else {
                i = 0;
            }
            sIsCredentialProtected = i;
            HwLog.w("KeyguardCfg", "isCredentialProtected sIsCredentialProtected: " + sIsCredentialProtected);
        }
        if (sIsCredentialProtected == 1) {
            return true;
        }
        return false;
    }

    public static boolean isUseVSuperChargeIcon() {
        if (sIsUseVSuperChargeIcon == -1) {
            int i;
            if (SystemProperties.getBoolean("ro.config.super_charge_icon", false)) {
                i = 1;
            } else {
                i = 0;
            }
            sIsUseVSuperChargeIcon = i;
            HwLog.d("KeyguardCfg", "useVSuperChargeIcon : " + sIsUseVSuperChargeIcon);
        }
        if (sIsUseVSuperChargeIcon == 1) {
            return true;
        }
        return false;
    }

    public static boolean isSupportFpPasswordTimeout() {
        return mFpPasswordTimeout;
    }

    public static boolean isDefaultPortOrientation() {
        return mIsDefaultPortOrientation;
    }

    public static boolean isVerifying() {
        return mIsVerifying;
    }

    public static void setVerifyingStatus(boolean isVerifying) {
        mIsVerifying = isVerifying;
    }
}
