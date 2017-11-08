package com.android.systemui.qs.tiles;

import android.content.Context;

public class HwCustDataSwitchTile {
    protected DataSwitchTile mParent;

    public HwCustDataSwitchTile(DataSwitchTile parent) {
        this.mParent = parent;
    }

    public void saveAfterChangeState(Context context, int newstate) {
    }
}
