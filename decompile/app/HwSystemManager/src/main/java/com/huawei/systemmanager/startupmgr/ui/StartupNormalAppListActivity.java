package com.huawei.systemmanager.startupmgr.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class StartupNormalAppListActivity extends HsmActivity {
    private Fragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragments();
    }

    private void initFragments() {
        this.mFragment = getFragmentManager().findFragmentByTag(StartupNormalAppListFragment.class.getSimpleName());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (this.mFragment == null) {
            this.mFragment = new StartupNormalAppListFragment();
            ft.replace(16908290, this.mFragment, StartupNormalAppListFragment.class.getSimpleName());
        } else {
            ft.attach(this.mFragment);
        }
        ft.commit();
    }
}
