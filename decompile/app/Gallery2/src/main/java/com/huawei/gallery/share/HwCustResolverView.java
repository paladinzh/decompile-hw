package com.huawei.gallery.share;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.huawei.gallery.proguard.Keep;
import com.huawei.gallery.share.HwResolverView.DisplayResolveInfo;
import java.util.ArrayList;
import java.util.List;

public class HwCustResolverView {
    @Keep
    public HwCustResolverView(Context context, Intent intent) {
    }

    public void appendCustomItem(List<DisplayResolveInfo> list, ArrayList<DisplayResolveInfo> arrayList) {
    }

    public void sortCurrentResolveList(PackageManager pm, List<ResolveInfo> list) {
    }

    public List<ResolveInfo> createCustomAdapter(Context context, List<ResolveInfo> list) {
        return null;
    }

    public boolean isPluggerMmsTopEnabled() {
        return false;
    }
}
