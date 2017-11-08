package com.huawei.rcs.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.android.mms.ui.ControllerImpl;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.location.places.Place;

public class RcsVideoPreviewActivity extends Activity {
    private RcsVideoPreviewFragment mFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(Place.TYPE_SUBLOCALITY_LEVEL_2, Place.TYPE_SUBLOCALITY_LEVEL_2);
        requestWindowFeature(1);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mFragment == null) {
            this.mFragment = new RcsVideoPreviewFragment();
        }
        if (this.mFragment != null) {
            this.mFragment.setController(new ControllerImpl(this, this.mFragment));
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(16908290, this.mFragment, "Mms_UI_MCF");
            transaction.commit();
        }
    }
}
