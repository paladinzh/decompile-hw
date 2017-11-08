package com.huawei.systemmanager.applock.password;

import android.os.Bundle;
import android.view.MenuItem;
import com.huawei.systemmanager.applock.password.callback.ActivityPostCallback;
import com.huawei.systemmanager.applock.taskstack.ActivityStackUtils;
import com.huawei.systemmanager.applock.utils.sp.StartActivityScenarioUtils;

public class AuthEnterRelockSelfActivity extends FingerAuthBaseActivity implements ActivityPostCallback {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        inflatePinAuth();
        StartActivityScenarioUtils.setAuthScenario(getApplicationContext(), 2);
        ActivityStackUtils.addActivity(this);
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
        ActivityStackUtils.finishAllActivity();
        ActivityStackUtils.release();
        super.onBackPressed();
    }

    protected void onPause() {
        super.onPause();
    }

    public void onPostFinish() {
        finish();
    }

    protected void onDestroy() {
        ActivityStackUtils.removeFromStack(this);
        super.onDestroy();
    }

    public boolean isGrantFingerPrintAuthPermission() {
        return true;
    }

    private void inflatePinAuth() {
        getFragmentManager().beginTransaction().replace(16908290, new FingerprintAuthFragment(), FingerAuthBaseActivity.FINGERPRINT_FRAGMENT_TAG).commit();
    }
}
