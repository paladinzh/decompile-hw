package com.huawei.systemmanager.spacecleanner;

import android.app.Fragment;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.spacecleanner.ui.tips.LowerMemTipFragment;
import com.huawei.systemmanager.spacecleanner.ui.tips.onTipsListener;

public class LowerMemTipActivity extends SingleFragmentActivity implements onTipsListener {
    private static final String TAG = "LowerMemTipActivity";

    protected Fragment buildFragment() {
        return new LowerMemTipFragment();
    }

    public void goToSpaceManager() {
        finish();
    }
}
