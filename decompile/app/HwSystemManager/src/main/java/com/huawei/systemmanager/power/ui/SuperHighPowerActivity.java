package com.huawei.systemmanager.power.ui;

import android.app.ActionBar;
import android.os.Bundle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class SuperHighPowerActivity extends HsmActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_high_power_main_frame);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.super_high_power_record);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        initUI();
    }

    private void initUI() {
        getFragmentManager().beginTransaction().replace(R.id.super_high_power_frame, new SuperHighPowerFragment()).commit();
    }
}
