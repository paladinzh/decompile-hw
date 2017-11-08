package com.huawei.systemmanager.applock.password;

import android.os.Bundle;
import com.huawei.systemmanager.applock.taskstack.ActivityStackUtils;
import com.huawei.systemmanager.applock.view.abs.AppLockRelockBaseActivity;

public class PasswordProtectResetActivity extends AppLockRelockBaseActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflatePinAuth();
        ActivityStackUtils.addActivity(this);
    }

    protected void concreteOnResume() {
    }

    protected String baseClassName() {
        return PasswordProtectResetActivity.class.getName();
    }

    protected void onDestroy() {
        ActivityStackUtils.removeFromStack(this);
        super.onDestroy();
    }

    private void inflatePinAuth() {
        getFragmentManager().beginTransaction().replace(16908290, new PasswordProtectResetFragment()).commit();
    }
}
