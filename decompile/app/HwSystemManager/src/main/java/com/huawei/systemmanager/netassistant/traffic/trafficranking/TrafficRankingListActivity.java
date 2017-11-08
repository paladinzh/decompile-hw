package com.huawei.systemmanager.netassistant.traffic.trafficranking;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.netassistant.utils.NatConst;
import com.huawei.systemmanager.util.HSMConst;

public class TrafficRankingListActivity extends HsmActivity {
    private static final String DATA_TAG = "TrafficRankingListFragment_Data";
    private static final String WLAN_TAG = "TrafficRankingListFragment_Wlan";
    private FragmentManager mFManager;
    private FragmentTransaction mFTransaction;
    private int mLastPosition = -1;
    int mSimId = -1;

    protected void onCreate(Bundle savedInstanceState) {
        HSMConst.managerBundle(savedInstanceState);
        super.onCreate(savedInstanceState);
        this.mFManager = getFragmentManager();
        if (getIntent() == null || getIntent().getIntExtra(NatConst.KEY_SUBID, -1) < 0) {
            setTitle(R.string.netassistant_traffic_ranking_wlan);
            showFragment(new TrafficRankingListContainerFragment());
            return;
        }
        this.mSimId = getIntent().getIntExtra(NatConst.KEY_SUBID, -1);
        ArrayAdapter<String> aa = new ArrayAdapter(this, R.layout.multi_spinner_dropdown_item, 16908308, new String[]{getString(R.string.netassistant_traffic_ranking_data), getString(R.string.netassistant_traffic_ranking_wlan)});
        ActionBar bar = getActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setNavigationMode(1);
        bar.setListNavigationCallbacks(aa, new OnNavigationListener() {
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                if (TrafficRankingListActivity.this.mLastPosition != itemPosition) {
                    TrafficRankingListActivity.this.initFragment_SIM(itemPosition);
                    TrafficRankingListActivity.this.mLastPosition = itemPosition;
                }
                return false;
            }
        });
    }

    private void initFragment_SIM(int position) {
        Fragment fragment = new TrafficRankingListContainerFragment();
        if (position == 0) {
            Bundle bundle = new Bundle();
            bundle.putInt(NatConst.KEY_SUBID, this.mSimId);
            fragment.setArguments(bundle);
        }
        showFragment(fragment);
    }

    private void showFragment(Fragment fragment) {
        this.mFTransaction = this.mFManager.beginTransaction();
        this.mFTransaction.replace(16908290, fragment);
        this.mFTransaction.commitAllowingStateLoss();
    }
}
