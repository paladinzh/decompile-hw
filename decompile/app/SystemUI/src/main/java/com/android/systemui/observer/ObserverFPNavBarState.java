package com.android.systemui.observer;

import android.net.Uri;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.System;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;

public class ObserverFPNavBarState extends ObserverItem<Boolean> {
    private boolean mIsCurrentInThirdKey = true;
    private boolean mIsFPNavBarThirdKeyEnable = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);

    public ObserverFPNavBarState(Handler handler) {
        super(handler);
        initDefaultValue();
    }

    public Uri getUri() {
        return System.getUriFor("swap_key_position");
    }

    public void onChange() {
        boolean z = false;
        if (-1 != System.getIntForUser(this.mContext.getContentResolver(), "swap_key_position", 0, UserSwitchUtils.getCurrentUser())) {
            z = true;
        }
        this.mIsCurrentInThirdKey = z;
    }

    public Boolean getValue() {
        return Boolean.valueOf(this.mIsFPNavBarThirdKeyEnable ? this.mIsCurrentInThirdKey : false);
    }

    private void initDefaultValue() {
        if (this.mIsFPNavBarThirdKeyEnable) {
            boolean isThirdKeyType = 1 == SystemProperties.getInt("ro.config.hw_front_fp_trikey", 0);
            boolean isTrikeyExist = isTrikeyExist();
            if (!isThirdKeyType) {
                isTrikeyExist = false;
            }
            this.mIsFPNavBarThirdKeyEnable = isTrikeyExist;
        }
    }

    private boolean isTrikeyExist() {
        boolean ret = false;
        try {
            Class clazz = Class.forName("huawei.android.os.HwGeneralManager");
            ret = ((Boolean) clazz.getDeclaredMethod("isSupportTrikey", null).invoke(clazz.getDeclaredMethod("getInstance", null).invoke(clazz, (Object[]) null), (Object[]) null)).booleanValue();
        } catch (Exception e) {
            HwLog.e("ObserverFPNavBarState", "isTrikeyExist error! " + e);
        } catch (Exception ex) {
            HwLog.e("ObserverFPNavBarState", "isTrikeyExist error! " + ex);
        }
        HwLog.i("ObserverFPNavBarState", "isTrikeyExist = " + ret);
        return ret;
    }
}
