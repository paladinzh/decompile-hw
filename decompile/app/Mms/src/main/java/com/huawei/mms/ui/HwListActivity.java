package com.huawei.mms.ui;

import android.app.ListActivity;
import android.content.res.Configuration;
import android.os.Bundle;
import com.android.messaging.util.BugleActivityUtil;
import com.android.mms.MmsConfig;
import com.huawei.cspcommon.MLog;

public abstract class HwListActivity extends ListActivity {
    private final String mComponentName = (getClass().getSimpleName() + " @" + String.valueOf(hashCode()) + "  ");
    protected int mOritation;

    public void onConfigurationChanged(Configuration newConfig) {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        MmsConfig.checkSimpleUi();
        invalidateOptionsMenu();
        MLog.d("Mms_View", this.mComponentName + "onConfigurationChanged. orientation from " + this.mOritation + " to " + newConfig.orientation);
        if (this.mOritation != newConfig.orientation) {
            onRotationChanged(this.mOritation, newConfig.orientation);
            this.mOritation = newConfig.orientation;
        }
    }

    protected void onRotationChanged(int oldOritation, int newOritation) {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onRotationChanged. from " + oldOritation + " to " + newOritation);
    }

    protected void onCreate(Bundle savedInstanceState) {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onCreate");
        super.onCreate(savedInstanceState);
        MmsConfig.checkSimpleUi();
        if (!MmsConfig.isInSimpleUI() || MmsConfig.getMmsBoolConfig("enableEasyModeAutoRotate")) {
            setRequestedOrientation(-1);
        } else {
            MLog.d("Mms_View", "Easy Mode - only portrait support");
            setRequestedOrientation(1);
        }
        this.mOritation = getResources().getConfiguration().orientation;
    }

    protected void onRestart() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onRestart running");
        super.onRestart();
        BugleActivityUtil.onActivityResume(this, this);
    }

    protected void onResume() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onResume running");
        super.onResume();
    }

    protected void onPause() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onPause running");
        super.onPause();
    }

    protected void onStop() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onStop running");
        super.onStop();
    }

    public void onLowMemory() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onLowMemory");
        super.onLowMemory();
    }

    public void onBackPressed() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle interaction-onBackPressed");
        super.onBackPressed();
    }

    protected void onDestroy() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onDestroy");
        super.onDestroy();
    }
}
