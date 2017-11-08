package com.huawei.notificationmanager.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.huawei.notificationmanager.util.Const;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HSMConst;

public class NotificationSettingsActivity extends HsmActivity {
    private Fragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        HSMConst.managerBundle(savedInstanceState);
        super.onCreate(savedInstanceState);
        initFragments();
    }

    private void initFragments() {
        this.mFragment = getFragmentManager().findFragmentByTag(NotificationSettingsFragment.class.getSimpleName());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (this.mFragment == null) {
            this.mFragment = new NotificationSettingsFragment();
            if (getIntent() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("packageName", getIntent().getStringExtra("packageName"));
                bundle.putString(Const.KEY_FROM_PACKAGE, getIntent().getStringExtra(Const.KEY_FROM_PACKAGE));
                this.mFragment.setArguments(bundle);
            }
            ft.replace(16908290, this.mFragment, NotificationSettingsFragment.class.getSimpleName());
        } else {
            ft.attach(this.mFragment);
        }
        ft.commit();
    }
}
