package com.huawei.systemmanager.preventmode;

import android.app.Activity;
import android.os.Bundle;
import com.huawei.systemmanager.util.HwLog;

public class PreventModeActivity extends Activity {
    public static final String TAG = "PreventModeActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HwLog.w(TAG, "no longer supported, finish.");
        finish();
    }
}
