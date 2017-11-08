package com.android.settings.fingerprint;

import android.content.Context;
import android.view.View;

public class HwCustFingerprintManageFragment {
    public FingerprintManageFragment mFingerprintManageFragment;

    public HwCustFingerprintManageFragment(FingerprintManageFragment fingerprintManageFragment) {
        this.mFingerprintManageFragment = fingerprintManageFragment;
    }

    public boolean fingerPrintShotcut() {
        return false;
    }

    public void initDiyButton(Context context, View fragmentView) {
    }
}
