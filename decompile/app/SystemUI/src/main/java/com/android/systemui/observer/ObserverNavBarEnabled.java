package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.System;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;

public class ObserverNavBarEnabled extends ObserverItem<Boolean> {
    private static final int FRING_PRINT_TRIKEY = SystemProperties.getInt("ro.config.hw_front_fp_trikey", -1);
    private int mDefaultValue = 1;
    private boolean mEnableNavBar;

    public ObserverNavBarEnabled(Handler handler) {
        super(handler);
        initDefaultValue();
    }

    public Uri getUri() {
        return System.getUriFor("enable_navbar");
    }

    public void onChange() {
        boolean z = false;
        if (System.getIntForUser(this.mContext.getContentResolver(), "enable_navbar", this.mDefaultValue, UserSwitchUtils.getCurrentUser()) != 0) {
            z = true;
        }
        this.mEnableNavBar = z;
        HwLog.i(this.TAG, "mEnableNavBar=" + this.mEnableNavBar + " in user = " + UserSwitchUtils.getCurrentUser());
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mEnableNavBar);
    }

    public void initDefaultValue() {
        int i = 0;
        switch (FRING_PRINT_TRIKEY) {
            case 0:
                if (!SystemUiUtil.isChinaArea()) {
                    i = 1;
                }
                this.mDefaultValue = i;
                return;
            case 1:
                this.mDefaultValue = 0;
                return;
            default:
                this.mDefaultValue = 1;
                return;
        }
    }
}
