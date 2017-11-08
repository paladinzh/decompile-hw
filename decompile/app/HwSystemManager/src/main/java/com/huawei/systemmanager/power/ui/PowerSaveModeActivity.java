package com.huawei.systemmanager.power.ui;

import android.os.Bundle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class PowerSaveModeActivity extends HsmActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.power_main_frame);
        initUI();
        setTitle(R.string.save_mode);
    }

    private void initUI() {
        getFragmentManager().beginTransaction().replace(R.id.power_frame, new PowerSaveModeFragment()).commit();
    }
}
