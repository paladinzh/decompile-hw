package com.huawei.rcs.utils.map.impl;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class RcsGaodeLocationActivity extends Activity {
    private RcsGaodeLocationFragment mFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        this.mFragment = new RcsGaodeLocationFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(16908290, this.mFragment, "MMS_UI_MAP");
        transaction.commit();
    }
}
