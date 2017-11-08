package com.android.systemui.qs.tiles;

import android.content.Context;
import com.android.systemui.utils.ReportTool;

public class HwCustDataSwitchTileImpl extends HwCustDataSwitchTile {
    private static final int DATA_SWITCH_TYPE = 3;

    public HwCustDataSwitchTileImpl(DataSwitchTile parent) {
        super(parent);
    }

    public void saveAfterChangeState(Context context, int newstate) {
        ReportTool.getInstance(context).report(3, String.format("{Status:%d}", new Object[]{Integer.valueOf(newstate)}));
    }
}
