package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.systemmanager.util.HwLog;

public class ListDownloadActivity extends ListTrashSetActivity {
    private static final String TAG = "ListAppCacheSetActivity";

    public int onGetCustomThemeStyle() {
        return 0;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (savedInstanceState != null || intent == null) {
            HwLog.d(TAG, "intent is invalidate or save");
            finish();
        }
    }

    protected Fragment buildDefaultFragment() {
        return new ListDownloadFragment();
    }
}
