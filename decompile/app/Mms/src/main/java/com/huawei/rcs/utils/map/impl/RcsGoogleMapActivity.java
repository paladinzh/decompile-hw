package com.huawei.rcs.utils.map.impl;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.android.rcs.RcsCommonConfig;

public class RcsGoogleMapActivity extends Activity {
    private RcsGoogleMapFragment mFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mFragment == null) {
            this.mFragment = new RcsGoogleMapFragment();
        }
        if (this.mFragment != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(16908290, this.mFragment, "MMS_GOOGLE_MAP");
            transaction.commit();
        }
    }
}
