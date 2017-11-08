package com.huawei.systemmanager.antivirus.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.antivirus.engine.CompetitorScan;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.TipsUtil;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AntivirusTipUtil {
    private static final String TAG = "AntivirusTipUtil";

    public static boolean removeViewedCompetitor(Context ctx, List<String> pkgs) {
        if (ctx == null || HsmCollections.isEmpty(pkgs)) {
            return false;
        }
        SharedPreferences sp = TipsUtil.getSharedPrefer(ctx);
        Iterable viewdCompetitors = sp.getStringSet(TipsUtil.KEY_VIEWED_COMPETITORS, null);
        if (viewdCompetitors == null) {
            return false;
        }
        Set<String> viewdCompetitors2 = Sets.newHashSet(viewdCompetitors);
        if (!viewdCompetitors2.removeAll(pkgs)) {
            return false;
        }
        HwLog.i(TAG, "removeCompetitor pkgs:" + pkgs);
        sp.edit().putStringSet(TipsUtil.KEY_VIEWED_COMPETITORS, viewdCompetitors2).commit();
        return true;
    }

    public static boolean removeViewedCompetitor(Context ctx, String pkg) {
        if (ctx == null || TextUtils.isEmpty(pkg)) {
            return false;
        }
        return removeViewedCompetitor(ctx, Lists.newArrayList(pkg));
    }

    public static boolean putViewedCompetitor(Context ctx, List<String> pkgs) {
        if (ctx == null || HsmCollections.isEmpty(pkgs)) {
            return false;
        }
        SharedPreferences sp = TipsUtil.getSharedPrefer(ctx);
        Set<String> viewdCompetitors = sp.getStringSet(TipsUtil.KEY_VIEWED_COMPETITORS, null);
        Set<String> result = Sets.newHashSet();
        if (viewdCompetitors != null) {
            result.addAll(viewdCompetitors);
        }
        if (!result.addAll(pkgs)) {
            return false;
        }
        HwLog.i(TAG, "add viewed competitors pkgs:" + pkgs);
        sp.edit().putStringSet(TipsUtil.KEY_VIEWED_COMPETITORS, result).commit();
        return true;
    }

    private static Set<String> getViewedCompetitors(Context ctx) {
        Iterable viewdCompetitors = TipsUtil.getSharedPrefer(ctx).getStringSet(TipsUtil.KEY_VIEWED_COMPETITORS, null);
        if (viewdCompetitors == null) {
            return Sets.newHashSet();
        }
        return Sets.newHashSet(viewdCompetitors);
    }

    public static List<HsmPkgInfo> getCompetitorsNotviewd(Context ctx) {
        List<HsmPkgInfo> allCompetitors = CompetitorScan.getCompetitors(ctx);
        Set<String> viewedCompetitors = getViewedCompetitors(ctx);
        Iterator<HsmPkgInfo> it = allCompetitors.iterator();
        while (it.hasNext()) {
            if (viewedCompetitors.contains(((HsmPkgInfo) it.next()).getPackageName())) {
                it.remove();
            }
        }
        return allCompetitors;
    }
}
