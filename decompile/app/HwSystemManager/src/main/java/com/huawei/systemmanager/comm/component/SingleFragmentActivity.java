package com.huawei.systemmanager.comm.component;

import android.app.Fragment;
import android.os.Bundle;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public abstract class SingleFragmentActivity extends HsmActivity {
    private static final String KEY_FRAGMENT_TAG = "content_frag";

    protected abstract Fragment buildFragment();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(16908290, buildFragment(), KEY_FRAGMENT_TAG).commit();
        }
    }

    public void onBackPressed() {
        boolean handled = false;
        Fragment frg = getFragmentManager().findFragmentByTag(KEY_FRAGMENT_TAG);
        if (frg != null && (frg instanceof IBackPressListener)) {
            handled = ((IBackPressListener) frg).onBackPressed();
        }
        if (!handled) {
            super.onBackPressed();
        }
    }

    protected void backfromAgreement(boolean agree) {
        Fragment frg = getFragmentManager().findFragmentByTag(KEY_FRAGMENT_TAG);
        if (frg != null && (frg instanceof IBackFromAgreementListener)) {
            ((IBackFromAgreementListener) frg).onBackFromAgreement(agree);
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Fragment frg = getContainedFragment();
        if (frg != null && (frg instanceof IWindowFocusChangedListener)) {
            ((IWindowFocusChangedListener) frg).onWindowFocusChanged(hasFocus);
        }
    }

    public Fragment getContainedFragment() {
        return getFragmentManager().findFragmentByTag(KEY_FRAGMENT_TAG);
    }
}
