package com.android.contacts.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import com.android.contacts.calllog.CallLogFragment;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import com.huawei.contact.util.HwUtil;

public class CallLogActivity extends Activity {
    private CallLogFragment mFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        if (ActivityManager.isUserAMonkey()) {
            finish();
        }
        setContentView(R.layout.call_log_activity);
        setDefaultKeyMode(1);
        this.mFragment = (CallLogFragment) getFragmentManager().findFragmentById(R.id.call_log_fragment);
    }

    @VisibleForTesting
    CallLogFragment getFragment() {
        return this.mFragment;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 5:
                if (SystemClock.uptimeMillis() - event.getDownTime() >= ((long) ViewConfiguration.getLongPressTimeout())) {
                    Intent intent = new Intent("android.intent.action.VOICE_COMMAND");
                    intent.setFlags(268435456);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        HwLog.e("CallLogActivity", "Activity Not Found Exception");
                    }
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 5:
                if (HwUtil.isIdle("com.android.contacts")) {
                    this.mFragment.callSelectedEntry();
                    return true;
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
