package com.huawei.systemmanager.mainscreen;

import android.app.Fragment;
import android.os.Bundle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;

public class SettingActivity extends SingleFragmentActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.main_screen_page_settings);
    }

    protected Fragment buildFragment() {
        return new SettingsFragment();
    }
}
