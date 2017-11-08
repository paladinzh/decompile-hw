package com.android.systemui.tv.pip;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import com.android.systemui.SystemUI;

public class PipUI extends SystemUI {
    private boolean mSupportPip;

    public void start() {
        boolean hasSystemFeature;
        PackageManager pm = this.mContext.getPackageManager();
        if (pm.hasSystemFeature("android.software.picture_in_picture")) {
            hasSystemFeature = pm.hasSystemFeature("android.software.leanback");
        } else {
            hasSystemFeature = false;
        }
        this.mSupportPip = hasSystemFeature;
        if (this.mSupportPip) {
            PipManager.getInstance().initialize(this.mContext);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mSupportPip) {
            PipManager.getInstance().onConfigurationChanged();
        }
    }
}
