package com.huawei.keyguard.support;

import android.content.Context;
import android.view.View;
import com.android.huawei.music.HwCustHwMusic;
import com.android.keyguard.HwCustEmergencyButton;
import com.android.keyguard.HwCustKeyguardAbsKeyInputView;
import com.huawei.cust.HwCustUtils;
import fyusion.vislib.BuildConfig;

public class CustFeature {
    private static HwCustKeyguardAbsKeyInputView mCustKeyguardAbsKeyInputView = ((HwCustKeyguardAbsKeyInputView) HwCustUtils.createObj(HwCustKeyguardAbsKeyInputView.class, new Object[0]));
    private static String mEmergencyNumber;
    private static HwCustEmergencyButton mHwCustEmergencyButton = ((HwCustEmergencyButton) HwCustUtils.createObj(HwCustEmergencyButton.class, new Object[0]));
    private static HwCustHwMusic mHwCustHwMusic = ((HwCustHwMusic) HwCustUtils.createObj(HwCustHwMusic.class, new Object[0]));

    public static void showPINChangeSuccessToast(Context context) {
        if (mCustKeyguardAbsKeyInputView != null) {
            mCustKeyguardAbsKeyInputView.showPINChangeSuccessToast(context);
        }
    }

    public static boolean isMusicAppSupported(Context context, String packageName) {
        return mHwCustHwMusic != null ? mHwCustHwMusic.isPackageInWhiteMusicList(context, packageName) : false;
    }

    public static boolean isDirectDialEmerCall(View v, Context context) {
        mEmergencyNumber = null;
        if (mHwCustEmergencyButton != null && mHwCustEmergencyButton.isDirectDialEmerCall(context)) {
            mEmergencyNumber = mHwCustEmergencyButton.checkAndGetEmergencyNumber(v);
            if (!(mEmergencyNumber == null || BuildConfig.FLAVOR.equals(mEmergencyNumber))) {
                return true;
            }
        }
        return false;
    }

    public static void directDialEmerCall() {
        mHwCustEmergencyButton.dialEmergencyCallDirectly(mEmergencyNumber);
    }
}
