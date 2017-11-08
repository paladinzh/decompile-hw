package com.huawei.systemmanager.applock.view.abs;

import android.os.Bundle;
import com.huawei.systemmanager.applock.utils.sp.RelockActivityUtils;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public abstract class AppLockRelockBaseActivity extends HsmActivity {
    private static boolean mAppLockClearFlag = false;

    protected abstract String baseClassName();

    protected abstract void concreteOnResume();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppLockClearFlag(false);
        RelockActivityUtils.addRelockActivity(getApplicationContext(), baseClassName());
        RelockActivityUtils.setRelockFlag(getApplicationContext(), false);
    }

    protected void onResume() {
        super.onResume();
        if (getAppLockClearFlag()) {
            finish();
        } else {
            concreteOnResume();
        }
    }

    public static boolean getAppLockClearFlag() {
        return mAppLockClearFlag;
    }

    public static void setAppLockClearFlag(boolean clear) {
        mAppLockClearFlag = clear;
    }
}
