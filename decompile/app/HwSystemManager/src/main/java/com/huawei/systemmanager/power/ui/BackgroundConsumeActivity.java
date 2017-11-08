package com.huawei.systemmanager.power.ui;

import android.app.Fragment;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;

public class BackgroundConsumeActivity extends SingleFragmentActivity {
    protected Fragment buildFragment() {
        return BackgroundConsumeFragment.newInstance(true);
    }
}
