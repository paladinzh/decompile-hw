package com.huawei.systemmanager.applock.password;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.systemmanager.applock.password.callback.ActivityPostCallback;
import com.huawei.systemmanager.applock.password.callback.BackPressedCallback;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HwLog;

public class SetPasswordAndBindActivity extends HsmActivity implements ActivityPostCallback {
    private static final String FRAG_TAG = "SetPasswordFragment";
    public static final String TAG = "SetPasswordAndBindActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        inflatePinAuth();
    }

    public void onBackPressed() {
        Fragment frg = getFragmentManager().findFragmentByTag(FRAG_TAG);
        if (frg == null || !((BackPressedCallback) frg).onBackButtonPressed()) {
            super.onBackPressed();
        }
    }

    public void onPostFinish() {
        setResultSuccess();
        finish();
    }

    private void inflatePinAuth() {
        getFragmentManager().beginTransaction().replace(16908290, new SetPasswordInitAndBindFragment(), FRAG_TAG).commit();
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
