package com.huawei.systemmanager.applock.password;

import android.os.Bundle;
import android.view.MenuItem;
import com.google.common.base.Strings;
import com.huawei.systemmanager.applock.password.callback.ActivityPostCallback;
import com.huawei.systemmanager.applock.utils.ActivityIntentUtils;
import com.huawei.systemmanager.applock.utils.ProviderWrapperUtils;
import com.huawei.systemmanager.applock.utils.sp.LockingPackageUtils;
import com.huawei.systemmanager.applock.utils.sp.StartActivityScenarioUtils;
import com.huawei.systemmanager.util.HwLog;

public class AuthLaunchLockedAppActivity extends FingerAuthBaseActivity implements ActivityPostCallback {
    private static final String KEY_PKGNAME = "pkgName";
    private static final String TAG = "AuthLaunchLockedAppActivity";
    private String mUnLockPkg;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        inflatePinAuth();
        if (savedInstanceState != null) {
            this.mUnLockPkg = savedInstanceState.getString("pkgName");
        } else {
            this.mUnLockPkg = LockingPackageUtils.getLockingPackageName(getApplicationContext(), "");
        }
        StartActivityScenarioUtils.setAuthScenario(getApplicationContext(), 3);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("pkgName", this.mUnLockPkg);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        HwLog.d(TAG, "onBackPressed to startHomeActivity");
        startActivity(ActivityIntentUtils.getStartHomeActivityIntent(getApplicationContext()));
        finish();
    }

    protected void onPause() {
        HwLog.v(TAG, "onPause");
        LockingPackageUtils.setLockingPackageName(getApplicationContext(), "");
        super.onPause();
    }

    protected void onResume() {
        HwLog.v(TAG, "onResume");
        super.onResume();
    }

    public boolean isGrantFingerPrintAuthPermission() {
        return true;
    }

    public void onPostFinish() {
        if (!Strings.isNullOrEmpty(this.mUnLockPkg)) {
            HwLog.d(TAG, "onPostFinish set authSuccess package: " + this.mUnLockPkg);
            new Thread("applock_addAuthSuccessPackage") {
                public void run() {
                    ProviderWrapperUtils.addAuthSuccessPackage(AuthLaunchLockedAppActivity.this.getApplicationContext(), AuthLaunchLockedAppActivity.this.mUnLockPkg);
                }
            }.start();
        }
        finish();
    }

    private void inflatePinAuth() {
        getFragmentManager().beginTransaction().replace(16908290, new FingerprintAuthFragment(), FingerAuthBaseActivity.FINGERPRINT_FRAGMENT_TAG).commit();
    }
}
