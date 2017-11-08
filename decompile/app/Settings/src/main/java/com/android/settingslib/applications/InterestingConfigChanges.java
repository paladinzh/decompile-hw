package com.android.settingslib.applications;

import android.content.res.Configuration;
import android.content.res.Resources;

public class InterestingConfigChanges {
    private final Configuration mLastConfiguration = new Configuration();
    private int mLastDensity;

    public boolean applyNewConfig(Resources res) {
        boolean densityChanged;
        int configChanges = this.mLastConfiguration.updateFrom(res.getConfiguration());
        if (this.mLastDensity != res.getDisplayMetrics().densityDpi) {
            densityChanged = true;
        } else {
            densityChanged = false;
        }
        if (!densityChanged && (configChanges & 772) == 0) {
            return false;
        }
        this.mLastDensity = res.getDisplayMetrics().densityDpi;
        return true;
    }
}
