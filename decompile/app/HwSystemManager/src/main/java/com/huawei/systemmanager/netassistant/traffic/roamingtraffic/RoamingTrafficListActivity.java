package com.huawei.systemmanager.netassistant.traffic.roamingtraffic;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.netassistant.traffic.datasaver.DataSaverConstants;
import com.huawei.systemmanager.util.HSMConst;

public class RoamingTrafficListActivity extends HsmActivity {
    private Fragment mContainerFragment;

    protected void onCreate(Bundle savedInstanceState) {
        HSMConst.managerBundle(savedInstanceState);
        super.onCreate(savedInstanceState);
        initFragments();
    }

    private void initFragments() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        this.mContainerFragment = new RoamingTrafficListContainerFragment();
        if (getIntent() != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("app_type", getIntent().getIntExtra("app_type", -1));
            bundle.putInt(DataSaverConstants.KEY_DATA_SAVER_WHITED_LIST_UID, getIntent().getIntExtra(DataSaverConstants.KEY_DATA_SAVER_WHITED_LIST_UID, -1));
            bundle.putString("action", getIntent().getAction());
            this.mContainerFragment.setArguments(bundle);
        }
        ft.replace(16908290, this.mContainerFragment);
        ft.commit();
    }
}
