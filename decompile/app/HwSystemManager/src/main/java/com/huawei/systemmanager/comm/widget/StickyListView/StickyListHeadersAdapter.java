package com.huawei.systemmanager.comm.widget.StickyListView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public interface StickyListHeadersAdapter extends ListAdapter {
    long getHeaderId(int i);

    View getHeaderView(int i, View view, ViewGroup viewGroup);
}
