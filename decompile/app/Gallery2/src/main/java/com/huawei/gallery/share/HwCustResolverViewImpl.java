package com.huawei.gallery.share;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfo.DisplayNameComparator;
import android.provider.SettingsEx.Systemex;
import android.text.TextUtils;
import com.huawei.gallery.share.HwResolverView.DisplayResolveInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HwCustResolverViewImpl extends HwCustResolverView {
    private Context mContext;
    private Intent mIntent;
    private boolean mIsPluggerMmsTopEnabled;

    public HwCustResolverViewImpl(Context context, Intent intent) {
        boolean z = false;
        super(context, intent);
        this.mContext = context;
        this.mIntent = intent;
        if (Systemex.getInt(this.mContext.getContentResolver(), "claro_plugger_mms_ontop", 0) == 1) {
            z = "android.intent.action.SEND".equals(this.mIntent.getAction());
        }
        this.mIsPluggerMmsTopEnabled = z;
    }

    public void appendCustomItem(List<DisplayResolveInfo> adapter, ArrayList<DisplayResolveInfo> result) {
        if (this.mIsPluggerMmsTopEnabled) {
            adapter.addAll(2, result);
        } else {
            adapter.addAll(0, result);
        }
    }

    public boolean isPluggerMmsTopEnabled() {
        return this.mIsPluggerMmsTopEnabled;
    }

    public void sortCurrentResolveList(PackageManager pm, List<ResolveInfo> currentResolveList) {
        if (!this.mIsPluggerMmsTopEnabled) {
            Collections.sort(currentResolveList, new DisplayNameComparator(pm));
        }
    }

    public List<ResolveInfo> createCustomAdapter(Context context, List<ResolveInfo> resolveInfos) {
        List<ResolveInfo> workingList = new ArrayList(resolveInfos);
        String topAppConfig = Systemex.getString(context.getContentResolver(), "hw_gallery_top_app");
        if (!TextUtils.isEmpty(topAppConfig)) {
            String[] appNames = topAppConfig.split(";");
            List<ResolveInfo> tempList = new ArrayList();
            for (String name : appNames) {
                if (!TextUtils.isEmpty(name)) {
                    for (ResolveInfo info : resolveInfos) {
                        if (name.compareTo(info.activityInfo.name) == 0) {
                            tempList.add(info);
                            break;
                        }
                    }
                }
            }
            if (tempList.size() > 0) {
                workingList.removeAll(tempList);
                workingList.addAll(0, tempList);
            }
        }
        return workingList;
    }
}
