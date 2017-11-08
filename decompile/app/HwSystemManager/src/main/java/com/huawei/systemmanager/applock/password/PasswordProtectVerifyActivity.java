package com.huawei.systemmanager.applock.password;

import android.content.Intent;
import android.os.Bundle;
import com.huawei.systemmanager.applock.utils.ActivityIntentUtils;
import com.huawei.systemmanager.applock.utils.sp.StartActivityScenarioUtils;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class PasswordProtectVerifyActivity extends HsmActivity {
    private int mResumeCount = 0;
    private int mScenario;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflatePinAuth();
        this.mScenario = StartActivityScenarioUtils.getAuthScenario(getApplicationContext(), -1);
        if (-1 == this.mScenario) {
            finish();
        }
    }

    protected void onResume() {
        super.onResume();
        this.mResumeCount++;
        if (1 < this.mResumeCount) {
            restartAuthActivity();
            finish();
        }
    }

    private void inflatePinAuth() {
        getFragmentManager().beginTransaction().replace(16908290, new PasswordProtectVerifyFragment()).commit();
    }

    private void restartAuthActivity() {
        Intent startIntent = ActivityIntentUtils.getStartAuthActivity(getApplicationContext(), this.mScenario);
        if (startIntent != null) {
            startActivity(startIntent);
        }
    }
}
