package com.android.keyguard;

import android.content.Context;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.keyguard.KeyguardCfg;

public class KeyguardSecurityModel {
    private static final /* synthetic */ int[] -com-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues = null;
    private static KeyguardSecurityModel sSecurityMode = null;
    private final Context mContext;
    private final boolean mIsPukScreenAvailable = this.mContext.getResources().getBoolean(17956939);
    private LockPatternUtils mLockPatternUtils;

    public enum SecurityMode {
        Invalid,
        None,
        Pattern,
        Password,
        PIN,
        SimPin,
        SimPuk
    }

    private static /* synthetic */ int[] -getcom-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues() {
        if (-com-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues != null) {
            return -com-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues;
        }
        int[] iArr = new int[SecurityMode.values().length];
        try {
            iArr[SecurityMode.Invalid.ordinal()] = 2;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SecurityMode.None.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SecurityMode.PIN.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SecurityMode.Password.ordinal()] = 5;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SecurityMode.Pattern.ordinal()] = 1;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SecurityMode.SimPin.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SecurityMode.SimPuk.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        -com-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues = iArr;
        return iArr;
    }

    KeyguardSecurityModel(Context context) {
        this.mContext = context;
        this.mLockPatternUtils = new LockPatternUtils(context);
    }

    void setLockPatternUtils(LockPatternUtils utils) {
        this.mLockPatternUtils = utils;
    }

    public SecurityMode getSecurityMode() {
        KeyguardUpdateMonitor monitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        if (SubscriptionManager.isValidSubscriptionId(monitor.getNextSubIdForState(State.PIN_REQUIRED))) {
            return SecurityMode.SimPin;
        }
        if (this.mIsPukScreenAvailable && SubscriptionManager.isValidSubscriptionId(monitor.getNextSubIdForState(State.PUK_REQUIRED))) {
            return SecurityMode.SimPuk;
        }
        int security = this.mLockPatternUtils.getActivePasswordQuality(KeyguardUpdateMonitor.getCurrentUser());
        switch (security) {
            case 0:
                return SecurityMode.None;
            case 65536:
                return SecurityMode.Pattern;
            case 131072:
            case 196608:
                return SecurityMode.PIN;
            case 262144:
            case 327680:
            case 393216:
            case 524288:
                return SecurityMode.Password;
            default:
                throw new IllegalStateException("Unknown security quality:" + security);
        }
    }

    public SecurityMode getBackupSecurityMode(SecurityMode mode) {
        switch (-getcom-android-keyguard-KeyguardSecurityModel$SecurityModeSwitchesValues()[mode.ordinal()]) {
            case 1:
                return KeyguardCfg.isBackupPinEnabled() ? SecurityMode.PIN : SecurityMode.Pattern;
            default:
                return mode;
        }
    }

    public static KeyguardSecurityModel getInst(Context context) {
        KeyguardSecurityModel keyguardSecurityModel;
        synchronized (KeyguardSecurityModel.class) {
            if (sSecurityMode == null) {
                sSecurityMode = new KeyguardSecurityModel(context);
            }
            keyguardSecurityModel = sSecurityMode;
        }
        return keyguardSecurityModel;
    }

    public static boolean isPINorPasswordorPatternMode(SecurityMode mode) {
        if (mode == SecurityMode.PIN || mode == SecurityMode.Password || mode == SecurityMode.Pattern) {
            return true;
        }
        return false;
    }
}
