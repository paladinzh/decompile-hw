package com.huawei.systemmanager.applock.password;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HwLog;

public abstract class FingerAuthBaseActivity extends HsmActivity {
    public static final String FINGERPRINT_FRAGMENT_TAG = "FP_FRAG_TAG";
    private static final String TAG = "FingerAuthBaseActivity";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!(context == null || intent == null)) {
                try {
                    if (!TextUtils.isEmpty(intent.getAction())) {
                        if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                            HwLog.v(FingerAuthBaseActivity.TAG, "onReceive ACTION_USER_PRESENT");
                            FingerAuthBaseActivity.this.notifyFragResumeFingerprint();
                        }
                    }
                } catch (Exception ex) {
                    HwLog.e(FingerAuthBaseActivity.TAG, "onReceive catch exception: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    };

    public abstract boolean isGrantFingerPrintAuthPermission();

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        if (isGrantFingerPrintAuthPermission()) {
            regReceiver();
        }
    }

    protected void onDestroy() {
        if (isGrantFingerPrintAuthPermission()) {
            unregReceiver();
        }
        super.onDestroy();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        HwLog.v(TAG, "onWindowFocusChanged " + hasFocus + ", isResumed: " + isResumed());
        if (isGrantFingerPrintAuthPermission() && hasFocus && isResumed()) {
            notifyFragResumeFingerprint();
        }
    }

    private void regReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_PRESENT");
        registerReceiver(this.mReceiver, filter);
    }

    private void unregReceiver() {
        HwLog.d(TAG, "unregReceiver");
        unregisterReceiver(this.mReceiver);
    }

    private void notifyFragResumeFingerprint() {
        FingerprintAuthFragment fragment = (FingerprintAuthFragment) getFragmentManager().findFragmentByTag(FINGERPRINT_FRAGMENT_TAG);
        if (fragment == null) {
            HwLog.w(TAG, "onWindowFocusChanged can't find FingerprintAuthFragment");
        } else {
            fragment.resumeFingerprintImmediately();
        }
    }
}
