package com.huawei.permissionmanager.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionItemBase;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class SingleAppActivity extends HsmActivity {
    private Fragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragments();
    }

    private void initFragments() {
        this.mFragment = getFragmentManager().findFragmentByTag(SingleAppFragment.class.getSimpleName());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (this.mFragment == null) {
            this.mFragment = new SingleAppFragment();
            ft.replace(16908290, this.mFragment, SingleAppFragment.class.getSimpleName());
        } else {
            ft.attach(this.mFragment);
        }
        ft.commit();
    }

    protected void updatePermissionListForSMSGroup() {
        ((SingleAppFragment) this.mFragment).updatePermissionListForSMSGroup();
    }

    protected void updatePermissionListForSpinner(int permissionType, int permissionOperation, PermissionItemBase item) {
        ((SingleAppFragment) this.mFragment).updatePermissionListForSpinner(permissionType, permissionOperation, item);
    }
}
