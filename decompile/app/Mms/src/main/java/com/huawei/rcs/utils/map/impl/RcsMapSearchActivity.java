package com.huawei.rcs.utils.map.impl;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.android.mms.ui.ControllerImpl;
import com.huawei.mms.ui.HwBaseFragment;

public class RcsMapSearchActivity extends Activity {
    private HwBaseFragment mFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFragment = new RcsGaodeSearchFragment();
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(16908290, this.mFragment, "MMS_UI_MAPSEARCH");
        transaction.commit();
    }
}
