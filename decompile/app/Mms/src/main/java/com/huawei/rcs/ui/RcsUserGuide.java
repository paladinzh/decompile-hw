package com.huawei.rcs.ui;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import com.android.mms.ui.ControllerImpl;
import com.android.rcs.ui.RcsUserGuideFragment;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.rcs.util.RcsFeatureEnabler;

public class RcsUserGuide extends HwBaseActivity {
    private RcsUserGuideFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MLog.v("RcsUserGuide", "onCreate");
        if (RcsFeatureEnabler.getInstance().isRcsPropertiesConfigOn() && this.mFragment == null) {
            this.mFragment = new RcsUserGuideFragment();
        }
        if (this.mFragment != null) {
            this.mFragment.setController(new ControllerImpl(this, this.mFragment));
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(16908290, this.mFragment, "Mms_UI_GCDRF");
            transaction.commit();
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (this.mFragment != null) {
            this.mFragment.onNewIntent(intent);
        }
    }

    public void onBackPressed() {
        if (this.mFragment != null) {
            this.mFragment.onBackPressed();
        }
        super.onBackPressed();
    }
}
