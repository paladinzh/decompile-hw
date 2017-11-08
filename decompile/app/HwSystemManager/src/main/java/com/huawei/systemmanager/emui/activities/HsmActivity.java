package com.huawei.systemmanager.emui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.useragreement.UserAgreementActivity;

public class HsmActivity extends Activity {
    private static final String KEY_SHOW_AGREEMENT = "show_agreement";
    private boolean mShowAgreement;

    protected int onGetCustomThemeStyle() {
        return 0;
    }

    protected boolean shouldUpdateActionBarStyle() {
        return false;
    }

    protected void onCreate(Bundle savedInstanceState) {
        boolean isLand = true;
        super.onCreate(savedInstanceState);
        if (checkMultiUser()) {
            if (isSupportLandOriention()) {
                HsmActivityHelper.setRequestedOrientation(this);
            }
            if (useHsmActivityHelper()) {
                HsmActivityHelper.setTranslucentStatus(this, true);
                HsmActivityHelper.setHsmThemeStyle(this, onGetCustomThemeStyle());
                if (shouldUpdateActionBarStyle()) {
                    HsmActivityHelper.updateActionBarStyle(this);
                }
            }
            if (savedInstanceState == null) {
                this.mShowAgreement = HsmActivityHelper.checkAndShowAgreement(this);
            } else {
                this.mShowAgreement = savedInstanceState.getBoolean(KEY_SHOW_AGREEMENT, false);
            }
            if (shouldHideStatusBar()) {
                if (getResources().getConfiguration().orientation != 2) {
                    isLand = false;
                }
                HsmActivityHelper.setStatusBarHide(this, isLand);
            }
            HsmActivityHelper.initActionBar(this);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_SHOW_AGREEMENT, this.mShowAgreement);
        super.onSaveInstanceState(outState);
    }

    protected boolean useHsmActivityHelper() {
        return true;
    }

    protected boolean shouldHideStatusBar() {
        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (UserAgreementActivity.REQUEST_CODE_USERAGREEMRNT == requestCode) {
            if (resultCode != -1) {
                finish();
            }
            boolean agree = resultCode == -1;
            this.mShowAgreement = false;
            backfromAgreement(agree);
        }
    }

    public boolean isShowAgreement() {
        return this.mShowAgreement;
    }

    protected void backfromAgreement(boolean agree) {
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (shouldHideStatusBar()) {
            HsmActivityHelper.setStatusBarHide(this, newConfig.orientation == 2);
        }
    }

    public boolean isSupportLandOriention() {
        return true;
    }

    private boolean checkMultiUser() {
        if (isSupprotMultiUser() || Utility.isOwnerUser()) {
            return true;
        }
        finish();
        return false;
    }

    public boolean isSupprotMultiUser() {
        return true;
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
