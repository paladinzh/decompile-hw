package com.huawei.systemmanager.power.batterychart;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class BatterHistoryActivity extends HsmActivity {
    public static final String TAG = "BatterHistoryActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_fragment_activity);
        Fragment fragment = BatteryHistoryFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.contents, fragment);
        fragmentTransaction.commit();
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.power_management_moredetail);
        setTitle(R.string.power_management_moredetail);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
