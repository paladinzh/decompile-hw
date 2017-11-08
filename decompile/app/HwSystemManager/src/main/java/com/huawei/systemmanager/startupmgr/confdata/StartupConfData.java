package com.huawei.systemmanager.startupmgr.confdata;

import android.content.Context;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.rainbow.db.bean.StartupConfigBean;
import com.huawei.systemmanager.util.HwLog;
import java.lang.ref.WeakReference;

public class StartupConfData {
    private static final String TAG = "StartupConfData";
    private static WeakReference<CompetitorAppsParser> mCompetitor = null;
    private static WeakReference<StartupCfgParser> mDftCfgParser = null;

    public static boolean isCompetitorPackage(Context ctx, String pkgName) {
        return !isCompetitorPackageCloud(ctx, pkgName) ? isCompetitorPackageLocal(ctx, pkgName) : true;
    }

    public static boolean getDftReceiverCfg(Context ctx, String pkgName) {
        return !getStartupAllowCfgCloud(ctx, pkgName, true) ? getStartupAllowReceiverLocal(ctx, pkgName) : true;
    }

    public static boolean getDftProviderServiceCfg(Context ctx, String pkgName) {
        return !getStartupAllowCfgCloud(ctx, pkgName, false) ? getStartupAllowProviderServiceLocal(ctx, pkgName) : true;
    }

    private static boolean isCompetitorPackageCloud(Context ctx, String pkgName) {
        return CloudDBAdapter.getInstance(ctx).isCompetitor(pkgName);
    }

    private static boolean isCompetitorPackageLocal(Context ctx, String pkgName) {
        CompetitorAppsParser parser = getCompetitorParser(ctx);
        if (parser != null) {
            HwLog.v(TAG, "isCompetitorPackageLocal in " + pkgName);
            return parser.isCompetitorApp(pkgName);
        }
        HwLog.w(TAG, "isCompetitorPackageLocal for " + pkgName + " getCompetitorParser null");
        return false;
    }

    private static boolean getStartupAllowCfgCloud(Context ctx, String pkgName, boolean isReceiver) {
        StartupConfigBean bean = CloudDBAdapter.getInstance(ctx).getSingleStartupConfig(pkgName);
        if (bean == null) {
            return false;
        }
        if (isReceiver) {
            return bean.isReceiverAllowStart();
        }
        return bean.isProviderServiceAllowStart();
    }

    private static boolean getStartupAllowReceiverLocal(Context ctx, String pkgName) {
        StartupCfgParser parser = getDfgCfgParser(ctx);
        if (parser != null) {
            HwLog.v(TAG, "getStartupAllowReceiverLocal in " + pkgName);
            return parser.getRDefaultValue(pkgName);
        }
        HwLog.w(TAG, "getStartupAllowReceiverLocal for " + pkgName + " getDfgCfgParser null");
        return false;
    }

    private static boolean getStartupAllowProviderServiceLocal(Context ctx, String pkgName) {
        StartupCfgParser parser = getDfgCfgParser(ctx);
        if (parser != null) {
            HwLog.v(TAG, "getStartupAllowProviderServiceLocal in " + pkgName);
            return parser.getSPDefaultValue(pkgName);
        }
        HwLog.w(TAG, "getStartupAllowProviderServiceLocal for " + pkgName + " getDfgCfgParser null");
        return false;
    }

    private static CompetitorAppsParser getCompetitorParser(Context ctx) {
        if (mCompetitor == null || mCompetitor.get() == null) {
            mCompetitor = new WeakReference(new CompetitorAppsParser(ctx));
        }
        return (CompetitorAppsParser) mCompetitor.get();
    }

    private static StartupCfgParser getDfgCfgParser(Context ctx) {
        if (mDftCfgParser == null || mDftCfgParser.get() == null) {
            mDftCfgParser = new WeakReference(new StartupCfgParser(ctx));
        }
        return (StartupCfgParser) mDftCfgParser.get();
    }
}
