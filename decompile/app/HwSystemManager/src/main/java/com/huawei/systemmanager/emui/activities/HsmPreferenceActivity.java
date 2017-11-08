package com.huawei.systemmanager.emui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import com.huawei.systemmanager.useragreement.UserAgreementActivity;

public class HsmPreferenceActivity extends PreferenceActivity {
    protected int onGetCustomThemeStyle() {
        return 0;
    }

    protected boolean shouldUpdateActionBarStyle() {
        return false;
    }

    protected void onCreate(Bundle savedInstanceState) {
        boolean isLand = true;
        HsmActivityHelper.setTranslucentStatus(this, true);
        HsmActivityHelper.setHsmThemeStyle(this, onGetCustomThemeStyle());
        super.onCreate(savedInstanceState);
        HsmActivityHelper.setRequestedOrientation(this);
        if (shouldUpdateActionBarStyle()) {
            HsmActivityHelper.updateActionBarStyle(this);
        }
        if (savedInstanceState == null) {
            HsmActivityHelper.checkAndShowAgreement(this);
        }
        if (shouldHideStatusBar()) {
            if (getResources().getConfiguration().orientation != 2) {
                isLand = false;
            }
            HsmActivityHelper.setStatusBarHide(this, isLand);
        }
        HsmActivityHelper.initActionBar(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (UserAgreementActivity.REQUEST_CODE_USERAGREEMRNT == requestCode && resultCode != -1) {
            finish();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (shouldHideStatusBar()) {
            HsmActivityHelper.setStatusBarHide(this, newConfig.orientation == 2);
        }
    }

    protected boolean shouldHideStatusBar() {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
