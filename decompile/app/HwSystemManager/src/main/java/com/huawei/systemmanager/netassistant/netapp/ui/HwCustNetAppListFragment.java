package com.huawei.systemmanager.netassistant.netapp.ui;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.ConstValues;
import java.util.ArrayList;
import java.util.List;

public class HwCustNetAppListFragment {
    private Context mContext;

    public HwCustNetAppListFragment(Context context) {
        this.mContext = context;
    }

    public List<Integer> getFilterUidList() {
        String filterStr = System.getString(this.mContext.getContentResolver(), "hw_invisible_apps_in_trafficmanager");
        List<Integer> uidList = null;
        if (!TextUtils.isEmpty(filterStr)) {
            uidList = new ArrayList();
            String[] invisibleUids = filterStr.split(ConstValues.SEPARATOR_KEYWORDS_EN);
            for (String applicationInfo : invisibleUids) {
                try {
                    uidList.add(Integer.valueOf(this.mContext.getPackageManager().getApplicationInfo(applicationInfo, 1).uid));
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return uidList;
    }
}
