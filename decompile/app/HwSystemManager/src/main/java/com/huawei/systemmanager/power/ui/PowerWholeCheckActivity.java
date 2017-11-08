package com.huawei.systemmanager.power.ui;

import android.app.Fragment;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.util.HwLog;

public class PowerWholeCheckActivity extends SingleFragmentActivity {
    private static String TAG = "PowerWholeCheckActivity";
    private PowerWholeCheckFragment mPowerWholeCheckFragment;

    protected Fragment buildFragment() {
        this.mPowerWholeCheckFragment = new PowerWholeCheckFragment();
        return this.mPowerWholeCheckFragment;
    }

    protected boolean useHsmActivityHelper() {
        return false;
    }

    public void onBackPressed() {
        if (this.mPowerWholeCheckFragment != null) {
            HwLog.i(TAG, "PowerWholeCheckActivity on back pressed! ");
            this.mPowerWholeCheckFragment.onBackPressed();
            return;
        }
        if (isResumed()) {
            HwLog.i(TAG, "is resumed! ");
            super.onBackPressed();
        }
    }
}
