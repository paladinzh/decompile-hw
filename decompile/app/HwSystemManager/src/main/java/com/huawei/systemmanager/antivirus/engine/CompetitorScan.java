package com.huawei.systemmanager.antivirus.engine;

import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.List;

public class CompetitorScan {
    public static List<HsmPkgInfo> getCompetitors(Context ctx) {
        List<String> competitors = CloudDBAdapter.getInstance(ctx).getAllCompetitors();
        List<HsmPkgInfo> results = Lists.newArrayList();
        for (String pkg : competitors) {
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfoInstalled(pkg);
            if (info != null) {
                results.add(info);
            }
        }
        return results;
    }
}
