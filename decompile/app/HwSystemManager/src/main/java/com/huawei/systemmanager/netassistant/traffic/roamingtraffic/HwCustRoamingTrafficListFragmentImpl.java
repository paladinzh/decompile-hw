package com.huawei.systemmanager.netassistant.traffic.roamingtraffic;

import android.os.SystemProperties;
import android.view.View;
import android.widget.CheckBox;
import com.huawei.systemmanager.netassistant.utils.ViewUtils;

public class HwCustRoamingTrafficListFragmentImpl extends HwCustRoamingTrafficListFragment {
    private boolean isSystemAppAllSelect = SystemProperties.getBoolean("ro.config.system_app_allSelect", false);

    public void systemAppAllSelect(CheckBox mRoamingHeadCheckBox, CheckBox mBackgroundHeadCheckBox) {
        if (this.isSystemAppAllSelect) {
            ViewUtils.setVisibility((View) mRoamingHeadCheckBox, 0);
            ViewUtils.setVisibility((View) mBackgroundHeadCheckBox, 0);
        }
    }
}
