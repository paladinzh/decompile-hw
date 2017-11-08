package com.huawei.permissionmanager.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;

public class AllAppAdapterBase extends BaseAdapter {
    protected List<AppInfoWrapper> mPermissionAppsList = null;

    public AllAppAdapterBase(List<AppInfoWrapper> permissionAppsList) {
        this.mPermissionAppsList = new ArrayList(permissionAppsList);
    }

    public int getCount() {
        return this.mPermissionAppsList.size();
    }

    public Object getItem(int position) {
        return this.mPermissionAppsList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public boolean isEnabled(int position) {
        if (((AppInfoWrapper) getItem(position)).isTag()) {
            return false;
        }
        return true;
    }
}
