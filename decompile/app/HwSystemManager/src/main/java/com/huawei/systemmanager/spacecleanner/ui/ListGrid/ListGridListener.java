package com.huawei.systemmanager.spacecleanner.ui.ListGrid;

import android.view.View;

public class ListGridListener {

    public interface OnClickListener {
        void onItemCheckBoxClick(View view);

        void onItemClick(View view);
    }

    public interface OnSizeChangeListener {
        void onSizeChanged(long j, long j2, boolean z, int i);
    }
}
