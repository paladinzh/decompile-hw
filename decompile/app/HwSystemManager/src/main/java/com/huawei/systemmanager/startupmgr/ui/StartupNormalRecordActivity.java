package com.huawei.systemmanager.startupmgr.ui;

import android.app.Fragment;
import android.content.Intent;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;

public class StartupNormalRecordActivity extends SingleFragmentActivity {
    protected Fragment buildFragment() {
        return new StartupNormalRecordFragment();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Fragment frg = getContainedFragment();
        if (frg != null) {
            ((StartupNormalRecordFragment) frg).resetListPosition();
        }
    }
}
