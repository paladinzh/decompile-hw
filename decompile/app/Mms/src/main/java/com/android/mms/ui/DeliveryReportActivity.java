package com.android.mms.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import com.huawei.mms.ui.HwBaseActivity;

public class DeliveryReportActivity extends HwBaseActivity {
    private DeliveryReportFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFragment = new DeliveryReportFragment();
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(16908290, this.mFragment);
        transaction.commit();
    }
}
