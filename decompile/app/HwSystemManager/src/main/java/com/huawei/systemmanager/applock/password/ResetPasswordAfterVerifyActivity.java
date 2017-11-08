package com.huawei.systemmanager.applock.password;

import android.content.Intent;
import android.os.Bundle;
import com.huawei.systemmanager.applock.utils.ActivityIntentUtils;
import com.huawei.systemmanager.applock.utils.compatibility.AuthRetryUtil;
import com.huawei.systemmanager.applock.utils.compatibility.AuthRetryUtil.TimeKeeperSuffix;
import com.huawei.systemmanager.applock.utils.sp.StartActivityScenarioUtils;

public class ResetPasswordAfterVerifyActivity extends ResetPasswordActivityBase {
    private int mResumeCount = 0;
    private int mScenario;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    public void onPostFinish() {
        AuthRetryUtil.resetTimeKeeper(getApplicationContext(), TimeKeeperSuffix.SUFFIX_APPLOCK_PASSWORD);
        finish();
    }

    private void restartAuthActivity() {
        Intent startIntent = ActivityIntentUtils.getStartAuthActivity(getApplicationContext(), this.mScenario);
        if (startIntent != null) {
            startActivity(startIntent);
        }
    }
}
