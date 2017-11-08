package com.android.settings.fingerprint.enrollment;

import android.app.StatusBarManager;
import android.content.Intent;
import android.os.Bundle;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsExtUtils;
import com.android.settings.navigation.NaviUtils;

public class FingerprintCalibrationActivity extends SettingsActivity {
    private StatusBarManager mStatusBarManager;

    public Intent getIntent() {
        Intent intent = getBaseIntent();
        if (intent.getStringExtra(":settings:show_fragment") != null) {
            return intent;
        }
        Intent newIntent = new Intent(intent);
        newIntent.putExtra(":settings:show_fragment", FingerprintCalibrationFragment.class.getName());
        newIntent.putExtra(":settings:show_fragment_title_resid", 2131628950);
        return newIntent;
    }

    private Intent getBaseIntent() {
        return super.getIntent();
    }

    protected boolean isValidFragment(String fragmentName) {
        if (FingerprintCalibrationFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }

    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        init();
    }

    protected void onResume() {
        adjustNavibar();
        super.onResume();
    }

    protected void onPause() {
        restoreNavibar();
        super.onPause();
    }

    private void init() {
        setNoDrawer(true);
    }

    private void adjustNavibar() {
        if (NaviUtils.isFrontFingerNaviEnabled() || SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            if (this.mStatusBarManager == null) {
                this.mStatusBarManager = (StatusBarManager) getSystemService("statusbar");
            }
            this.mStatusBarManager.disable(16777216 | 2097152);
        }
    }

    private void restoreNavibar() {
        if (NaviUtils.isFrontFingerNaviEnabled() || SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            this.mStatusBarManager.disable(0);
        }
    }
}
