package com.huawei.permissionmanager.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

/* compiled from: PermissionSettingFragment */
class PermissionSettingAdapter extends PermissionAdapterBase {
    private String LOG_TAG = "PermissionSettingAdapter";
    private PermissionListViewItemFactory listViewItemFactory = null;
    private OnCheckedChangeListener mCfgSwitchListener;
    private Context mContext;

    public PermissionSettingAdapter(Context context, ArrayList<AppInfoWrapperForSinglePermission> permissionAppsList, Permission perObject, OnCheckedChangeListener listener) {
        super(permissionAppsList);
        this.mContext = context;
        this.listViewItemFactory = new PermissionListViewItemFactory(this.mContext);
        this.mCfgSwitchListener = listener;
    }

    public View getView(int position, View convertview, ViewGroup arg2) {
        HwLog.v(this.LOG_TAG, "getView at " + position);
        return this.listViewItemFactory.getPermissionItemView((ListViewObject) getItem(position), convertview, this.mCfgSwitchListener);
    }
}
