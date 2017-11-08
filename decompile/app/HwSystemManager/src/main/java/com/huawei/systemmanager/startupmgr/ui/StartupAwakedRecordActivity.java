package com.huawei.systemmanager.startupmgr.ui;

import android.app.Fragment;
import android.content.Intent;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;

public class StartupAwakedRecordActivity extends SingleFragmentActivity {
    protected Fragment buildFragment() {
        return new StartupAwakedRecordFragment();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Fragment frg = getContainedFragment();
        if (frg != null) {
            ((StartupAwakedRecordFragment) frg).resetListPosition();
        }
    }
}
