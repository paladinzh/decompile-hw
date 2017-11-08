package com.android.settings.applications;

import android.os.Bundle;
import android.util.Log;
import com.android.settings.AppHeader;
import com.android.settings.SettingsPreferenceFragment;

public abstract class AppInfoWithHeader extends AppInfoBase {
    private boolean mCreated;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mCreated) {
            Log.w(TAG, "onActivityCreated: ignoring duplicate call");
            return;
        }
        this.mCreated = true;
        if (this.mPackageInfo != null) {
            AppHeader.createAppHeader((SettingsPreferenceFragment) this, this.mPackageInfo.applicationInfo.loadIcon(this.mPm), this.mPackageInfo.applicationInfo.loadLabel(this.mPm), this.mPackageName, this.mPackageInfo.applicationInfo.uid, 0);
        }
    }
}
