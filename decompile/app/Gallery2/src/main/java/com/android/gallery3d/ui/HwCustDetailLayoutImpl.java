package com.android.gallery3d.ui;

import android.os.SystemProperties;

public class HwCustDetailLayoutImpl extends HwCustDetailLayout {
    public boolean deleteModleInfo() {
        return SystemProperties.getBoolean("ro.config.delete_msg", false);
    }
}
