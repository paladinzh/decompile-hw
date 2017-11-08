package com.huawei.systemmanager.addviewmonitor;

import android.os.Bundle;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class AddViewMonitorActivity extends HsmActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.addview_activity_title);
        initFragments();
    }

    private void initFragments() {
        getFragmentManager().beginTransaction().replace(16908290, new AddViewMgrFragment()).commit();
    }
}
