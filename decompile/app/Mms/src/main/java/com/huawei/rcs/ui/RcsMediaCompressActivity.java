package com.huawei.rcs.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import com.android.mms.ui.ControllerImpl;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.location.places.Place;

public class RcsMediaCompressActivity extends Activity {
    private RcsMediaCompressFragment mFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(16973934);
        requestWindowFeature(1);
        getWindow().setFlags(Place.TYPE_SUBLOCALITY_LEVEL_2, Place.TYPE_SUBLOCALITY_LEVEL_2);
        getWindow().addFlags(134217728);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mFragment == null) {
            this.mFragment = new RcsMediaCompressFragment();
        }
        if (this.mFragment != null) {
            this.mFragment.setController(new ControllerImpl(this, this.mFragment));
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(16908290, this.mFragment, "Mms_UI_MCF");
            transaction.commit();
        }
    }

    public void doClick(View v) {
        if (this.mFragment != null) {
            this.mFragment.doClick(v);
        }
    }

    public void onBackPressed() {
        if (this.mFragment != null) {
            this.mFragment.onBackPressed();
        }
        super.onBackPressed();
    }
}
