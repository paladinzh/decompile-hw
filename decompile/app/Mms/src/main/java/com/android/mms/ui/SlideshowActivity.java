package com.android.mms.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import com.huawei.mms.ui.HwBaseActivity;

public class SlideshowActivity extends HwBaseActivity {
    private SlideshowFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFormat(-3);
        this.mFragment = new SlideshowFragment();
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(16908290, this.mFragment);
        transaction.commit();
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        this.mFragment.onKeyDown(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }
}
