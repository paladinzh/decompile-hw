package com.huawei.systemmanager.power.batteryoptimize;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class PowerOptimizeControl {
    public abstract ViewGroup getContentLayout();

    public abstract ViewGroup getHeadLayout();

    public abstract TextView getManualItemNumView();

    public abstract View newView();
}
