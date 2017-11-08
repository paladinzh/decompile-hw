package com.huawei.systemmanager.applock.password;

import android.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
import com.huawei.systemmanager.applock.password.callback.ActivityPostCallback;
import com.huawei.systemmanager.applock.password.callback.BackPressedCallback;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public abstract class ResetPasswordActivityBase extends HsmActivity implements ActivityPostCallback {
    private static final String FRAG_TAG = "ResetPasswordFragment";
    public static final String TAG = "ResetPasswordActivityBase";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        inflatePinAuth();
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
        Fragment frg = getFragmentManager().findFragmentByTag(FRAG_TAG);
        if (frg == null || !((BackPressedCallback) frg).onBackButtonPressed()) {
            subProcessOnBackPressed();
            super.onBackPressed();
        }
    }

    protected void onPause() {
        subProcessOnPause();
        super.onPause();
    }

    private void inflatePinAuth() {
        getFragmentManager().beginTransaction().replace(16908290, new SetPasswordResetFragment(), FRAG_TAG).commit();
    }

    protected void subProcessOnBackPressed() {
    }

    protected void subProcessOnPause() {
    }
}
