package com.huawei.systemmanager.applock.password;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.huawei.systemmanager.applock.datacenter.AppLockService;
import com.huawei.systemmanager.applock.password.callback.BackPressedCallback;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class SetPasswordActivity extends HsmActivity {
    private static final String FRAG_TAG = "SetPasswordFragment";
    public static final String TAG = "SetPasswordActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utility.isOwnerUser()) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            inflatePinAuth();
            startAppService();
            return;
        }
        finish();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startAppService() {
        Intent appLockIntent = new Intent(this, AppLockService.class);
        appLockIntent.addFlags(1);
        startService(appLockIntent);
    }

    public void onBackPressed() {
        Fragment frg = getFragmentManager().findFragmentByTag(FRAG_TAG);
        if (frg == null || !((BackPressedCallback) frg).onBackButtonPressed()) {
            super.onBackPressed();
        }
    }

    protected void onPause() {
        finish();
        super.onPause();
    }

    private void inflatePinAuth() {
        getFragmentManager().beginTransaction().replace(16908290, new SetPasswordInitFragment(), FRAG_TAG).commit();
    }
}
