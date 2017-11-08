package com.huawei.systemmanager.adblock.ui.view;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;

public interface IAdView {
    void initListView(ListAdapter listAdapter, OnItemClickListener onItemClickListener);

    void showProgressBar(boolean z);

    void updateAllOpSwitch(boolean z, boolean z2, OnCheckedChangeListener onCheckedChangeListener);

    void updateTipView(int i, int i2);
}
