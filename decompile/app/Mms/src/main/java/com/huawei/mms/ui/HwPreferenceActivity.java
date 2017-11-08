package com.huawei.mms.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.android.mms.MmsConfig;
import com.huawei.cspcommon.MLog;

public class HwPreferenceActivity extends PreferenceActivity {
    private static final String TAG = "Mms_View";

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MmsConfig.checkSimpleUi();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MmsConfig.checkSimpleUi();
        if (!MmsConfig.isInSimpleUI() || MmsConfig.getMmsBoolConfig("enableEasyModeAutoRotate")) {
            setRequestedOrientation(-1);
            return;
        }
        MLog.d(TAG, "Easy Mode - only portrait support");
        setRequestedOrientation(1);
    }
}
