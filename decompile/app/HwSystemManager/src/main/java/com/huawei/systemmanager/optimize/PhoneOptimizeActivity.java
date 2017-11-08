package com.huawei.systemmanager.optimize;

import android.app.Fragment;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.optimize.ui.ProcessManagerFragment;

public class PhoneOptimizeActivity extends SingleFragmentActivity {
    protected Fragment buildFragment() {
        return new ProcessManagerFragment();
    }
}
