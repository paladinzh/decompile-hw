package com.android.systemui.power;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class HwCustPowerUI {
    Context mContext;

    public HwCustPowerUI(Context context) {
        this.mContext = context;
    }

    public void handleCustIntent(Intent intent) {
    }

    public void addMoreRegAction(IntentFilter filter) {
    }
}
