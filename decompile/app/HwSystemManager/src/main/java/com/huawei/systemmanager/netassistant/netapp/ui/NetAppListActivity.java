package com.huawei.systemmanager.netassistant.netapp.ui;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.netassistant.traffic.roamingtraffic.RoamingTrafficListContainerFragment;
import com.huawei.systemmanager.util.HSMConst;

public class NetAppListActivity extends HsmActivity {
    private FragmentManager mFManager;
    private FragmentTransaction mFTransaction;
    private int mLastPosition = -1;

    protected void onCreate(Bundle savedInstanceState) {
        HSMConst.managerBundle(savedInstanceState);
        super.onCreate(savedInstanceState);
        this.mFManager = getFragmentManager();
        initTitle();
    }

    private void initTitle() {
        ArrayAdapter<String> aa = new ArrayAdapter(this, R.layout.multi_spinner_dropdown_item, 16908308, new String[]{getString(R.string.net_assistant_main_net_app_title), getString(R.string.advanced_net_control)});
        ActionBar bar = getActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setNavigationMode(1);
        bar.setListNavigationCallbacks(aa, new OnNavigationListener() {
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                if (NetAppListActivity.this.mLastPosition != itemPosition) {
                    NetAppListActivity.this.showFragment(itemPosition);
                    NetAppListActivity.this.mLastPosition = itemPosition;
                }
                return false;
            }
        });
    }

    private void showFragment(int position) {
        Fragment fragment;
        this.mFTransaction = this.mFManager.beginTransaction();
        if (position == 0) {
            setTitle(R.string.net_assistant_main_net_app_title);
            fragment = new NetAppListContainerFragment();
        } else {
            setTitle(R.string.advanced_net_control);
            fragment = new RoamingTrafficListContainerFragment();
        }
        this.mFTransaction.replace(16908290, fragment);
        this.mFTransaction.commitAllowingStateLoss();
    }
}
