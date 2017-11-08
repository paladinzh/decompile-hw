package com.huawei.systemmanager.applock.password;

import android.content.Intent;
import android.os.Bundle;
import com.huawei.systemmanager.applock.password.callback.ActivityPostCallback;
import com.huawei.systemmanager.applock.utils.sp.FingerprintBindUtils;
import com.huawei.systemmanager.applock.utils.sp.StartActivityScenarioUtils;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HwLog;

public class AuthBindFingerprintActivity extends HsmActivity implements ActivityPostCallback {
    private static final String BIND_SWITCH_KEY = "fingerprintAuthSwitchType";
    private static final int CLOSE_BIND = 0;
    private static final int OPEN_BIND = 1;
    private static final String TAG = "AuthBindFingerprintActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        HwLog.d(TAG, "onCreate set default result");
        checkInputAndRun();
        StartActivityScenarioUtils.setAuthScenario(getApplicationContext(), 4);
    }

    public void onPostFinish() {
        setResultSuccess();
        finish();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkInputAndRun() {
        Intent intent = getIntent();
        if (intent == null) {
            HwLog.d(TAG, "checkInputAndRun intent is null");
            finish();
            return;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            HwLog.d(TAG, "checkInputAndRun extras is null");
            finish();
            return;
        }
        try {
            switch (extras.getInt(BIND_SWITCH_KEY)) {
                case 0:
                    runCloseBind();
                    break;
                case 1:
                    runOpenBind();
                    break;
                default:
                    HwLog.e(TAG, "checkInputAndRun unsupport bindSwitch");
                    break;
            }
        } catch (Exception ex) {
            HwLog.e(TAG, "checkInputAndRun catch exception, terminate soon.");
            ex.printStackTrace();
            finish();
        }
    }

    private void runCloseBind() {
        FingerprintBindUtils.setFingerprintBindStatus(getApplicationContext(), false);
        setResultSuccess();
        finish();
    }

    private void runOpenBind() {
        inflatePinAuth();
    }

    private void inflatePinAuth() {
        getFragmentManager().beginTransaction().replace(16908290, new BindFingerprintAuthFragment()).commit();
    }

    private void setResultSuccess() {
        HwLog.d(TAG, "setResultSuccess");
        setResult(-1, getResultIntent(true));
    }

    private Intent getResultIntent(boolean success) {
        HwLog.d(TAG, "getResultIntent:" + success);
        Intent intent = new Intent();
        intent.putExtra("isSuccess", success);
        return intent;
    }
}
