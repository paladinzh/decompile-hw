package com.huawei.systemmanager.rainbow.comm.crossutil.cvtmeta;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.lang.ref.SoftReference;

public class PermissionCvtProxy {
    private static final String TAG = PermissionCvtProxy.class.getSimpleName();
    private static SoftReference<CvtFileDOMParser> mParser;

    public static boolean subOfPermissionSet(Context ctx, String pkgName, int itemId) {
        try {
            PackageInfo packageInfo = PackageManagerWrapper.getPackageInfo(ctx.getPackageManager(), pkgName, 12288);
            if (packageInfo != null && packageInfo.requestedPermissions != null) {
                return getParser(ctx).subOfPermissionSet(itemId, Sets.newHashSet(packageInfo.requestedPermissions));
            }
            HwLog.w(TAG, "subOfPermissionSet empty packageInfo or requestedPermissions for: " + pkgName);
            return false;
        } catch (NameNotFoundException e) {
            HwLog.e(TAG, "subOfPermissionSet can't find package: " + pkgName);
        } catch (Exception e2) {
            HwLog.e(TAG, "subOfPermissionSet catch unknown exception!");
        }
    }

    public static int getPermissionConfigType(Context ctx, int itemId, int code, int cfg) {
        return getParser(ctx).getPermissionConfigType(itemId, code, cfg);
    }

    private static CvtFileDOMParser getParser(Context ctx) {
        if (mParser == null || mParser.get() == null) {
            mParser = new SoftReference(new CvtFileDOMParser());
            CvtFileDOMParser parser = (CvtFileDOMParser) mParser.get();
            if (parser != null) {
                parser.parseXml(ctx);
            }
        }
        return (CvtFileDOMParser) mParser.get();
    }
}
