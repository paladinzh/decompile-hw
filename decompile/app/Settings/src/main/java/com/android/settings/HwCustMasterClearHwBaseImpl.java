package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.view.View;
import android.widget.CheckBox;

public class HwCustMasterClearHwBaseImpl extends HwCustMasterClearHwBase {
    public HwCustMasterClearHwBaseImpl(Context context) {
        super(context);
    }

    public void handleCustFactoryResetOption(View aInternalStorageContainer, CheckBox aInternalStorage, View aLowlevelEraseInternalContainer, CheckBox aLowlevelEraseInternalCheckBox) {
        if (HwCustSettingsUtils.IS_SPRINT) {
            if (!(aInternalStorage == null || aInternalStorageContainer == null)) {
                aInternalStorage.setChecked(Boolean.TRUE.booleanValue());
                aInternalStorageContainer.setClickable(Boolean.FALSE.booleanValue());
            }
            if (isLowlevelEraseInternalSupported() && aLowlevelEraseInternalCheckBox != null && aLowlevelEraseInternalContainer != null) {
                aLowlevelEraseInternalCheckBox.setChecked(Boolean.TRUE.booleanValue());
                aLowlevelEraseInternalContainer.setClickable(Boolean.FALSE.booleanValue());
            }
        }
    }

    private boolean isLowlevelEraseInternalSupported() {
        return SystemProperties.getBoolean("ro.config.hw_lowlevel_erase", false);
    }
}
