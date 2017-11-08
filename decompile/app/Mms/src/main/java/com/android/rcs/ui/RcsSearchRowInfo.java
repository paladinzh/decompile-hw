package com.android.rcs.ui;

import com.android.rcs.RcsCommonConfig;

public class RcsSearchRowInfo {
    private static final boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private int mWitchTable = 10;

    public void setWitchTable(int witchTable) {
        if (mIsRcsOn) {
            this.mWitchTable = witchTable;
        }
    }

    public int getWitchTable() {
        if (mIsRcsOn) {
            return this.mWitchTable;
        }
        return 10;
    }
}
