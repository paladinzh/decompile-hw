package com.huawei.systemmanager.comm;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HwLog;

public abstract class MultiFragmentActivity extends HsmActivity {
    public static final String KEY_DEFAULT_FRAGMENT_TAG = "default_fragment_tag";
    private static final String TAG = "MultiFragmentActivity";
    private String currentFragmentTag = "";

    protected abstract Fragment buildDefaultFragment();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Fragment frg = buildDefaultFragment();
            setCurrentFragmentTag(KEY_DEFAULT_FRAGMENT_TAG);
            getFragmentManager().beginTransaction().add(16908290, frg, KEY_DEFAULT_FRAGMENT_TAG).commit();
        }
    }

    protected String getCurrentFragmentTag() {
        return this.currentFragmentTag;
    }

    protected void setCurrentFragmentTag(String tag) {
        this.currentFragmentTag = tag;
    }

    protected Fragment getContainedFragment() {
        HwLog.i(TAG, "currentFragmentTag:" + this.currentFragmentTag);
        return getFragmentManager().findFragmentByTag(this.currentFragmentTag);
    }

    public void switchContent(Fragment from, Fragment to) {
        if (getContainedFragment() != to) {
            setCurrentFragmentTag(to.getTag());
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            if (to.isAdded()) {
                transaction.hide(from).show(to).commit();
            } else {
                transaction.hide(from).add(16908290, to, to.getTag()).commit();
            }
        }
    }
}
