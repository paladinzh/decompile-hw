package com.huawei.permissionmanager.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class PermissionSettingActivity extends HsmActivity {
    private Fragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragments();
    }

    private void initFragments() {
        this.mFragment = getFragmentManager().findFragmentByTag(PermissionSettingFragment.class.getSimpleName());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (this.mFragment == null) {
            this.mFragment = new PermissionSettingFragment();
            ft.replace(16908290, this.mFragment, PermissionSettingFragment.class.getSimpleName());
        } else {
            ft.attach(this.mFragment);
        }
        ft.commit();
    }

    protected void updatePermissionAppListForSpinner(int permissionType, int uid, String pkgName, int permissionOperation) {
        ((PermissionSettingFragment) this.mFragment).updatePermissionAppListForSpinner(permissionType, uid, pkgName, permissionOperation);
    }

    void onHeaderChanged() {
        ((PermissionSettingFragment) this.mFragment).onHeaderChanged();
    }

    protected void updateUI() {
        ((PermissionSettingFragment) this.mFragment).updateUI();
    }
}
