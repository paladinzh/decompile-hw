package com.huawei.systemmanager.applock.password;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.huawei.systemmanager.applock.datacenter.AppLockService;
import com.huawei.systemmanager.applock.password.callback.ActivityPostCallback;
import com.huawei.systemmanager.applock.password.callback.BackPressedCallback;
import com.huawei.systemmanager.applock.utils.ActivityIntentUtils;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;
import com.huawei.systemmanager.applock.utils.sp.StartActivityScenarioUtils;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;

public class AuthEnterAppLockActivity extends FingerAuthBaseActivity implements ActivityPostCallback {
    private static final String SETPASSWORD_FRAGMENT_TAG = "SetPasswordFragment";
    private static final String TAG = "AuthEnterAppLockActivity";
    private int mAuthType = -1;

    protected void onCreate(Bundle savedInstanceState) {
        if (Utility.isOwnerUser()) {
            setType();
            super.onCreate(savedInstanceState);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            initialize();
            startAppService();
            return;
        }
        finish();
    }

    private void startAppService() {
        Intent appLockIntent = new Intent(this, AppLockService.class);
        appLockIntent.addFlags(1);
        startService(appLockIntent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (this.mAuthType == 0) {
            Fragment frg = getFragmentManager().findFragmentByTag(SETPASSWORD_FRAGMENT_TAG);
            if (frg != null && ((BackPressedCallback) frg).onBackButtonPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    protected void onPause() {
        if (this.mAuthType == 0) {
            finish();
        }
        super.onPause();
    }

    public void onPostFinish() {
        if (this.mAuthType == 1) {
            HwLog.d(TAG, "onPostSuccess, going to start LockedListActivity");
            startActivity(ActivityIntentUtils.getApplicationListActivityIntent(getApplicationContext()));
            finish();
        }
    }

    public boolean isGrantFingerPrintAuthPermission() {
        return this.mAuthType == 1;
    }

    private void initialize() {
        if (1 == this.mAuthType) {
            inflatePinAuth(FingerAuthBaseActivity.FINGERPRINT_FRAGMENT_TAG, new FingerprintAuthFragment());
            StartActivityScenarioUtils.setAuthScenario(getApplicationContext(), 1);
            return;
        }
        inflatePinAuth(SETPASSWORD_FRAGMENT_TAG, new SetPasswordInitFragment());
    }

    private void inflatePinAuth(String tag, Fragment fragment) {
        getFragmentManager().beginTransaction().replace(16908290, fragment, tag).commit();
    }

    private void setType() {
        if (AppLockPwdUtils.isPasswordSet(GlobalContext.getContext())) {
            this.mAuthType = 1;
        } else {
            this.mAuthType = 0;
        }
    }
}
