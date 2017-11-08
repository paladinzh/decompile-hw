package com.android.rcs.ui;

import android.content.Context;
import com.android.rcs.RcsCommonConfig;

public class RcsRecipientListFragment {
    private boolean isRcsEnable = RcsCommonConfig.isRCSSwitchOn();
    private Context mContext;

    public RcsRecipientListFragment(Context context) {
        this.mContext = context;
    }
}
