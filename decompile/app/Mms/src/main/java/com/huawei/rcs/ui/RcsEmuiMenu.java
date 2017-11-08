package com.huawei.rcs.ui;

import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;

public class RcsEmuiMenu {
    public int getExtendNotLandscapeDrawableId(int menuId) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            switch (menuId) {
                case 278927471:
                    return R.drawable.rcs_creat_group_chat_normal;
            }
        }
        return -1;
    }
}
