package com.huawei.rcs.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import com.android.mms.ui.ControllerImpl;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsGroupChatDeliveryReportFragment;
import com.huawei.mms.ui.HwListActivity;

public class RcsGroupChatDeliveryReportActivity extends HwListActivity {
    private RcsGroupChatDeliveryReportFragment mFragment;

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.v("RcsGroupChatDeliveryReportActivity", "onCreate");
        requestWindowFeature(5);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mFragment == null) {
            this.mFragment = new RcsGroupChatDeliveryReportFragment();
        }
        if (this.mFragment != null) {
            this.mFragment.setController(new ControllerImpl(this, this.mFragment));
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(16908290, this.mFragment, "Mms_UI_GCDRF");
            transaction.commit();
        }
    }
}
