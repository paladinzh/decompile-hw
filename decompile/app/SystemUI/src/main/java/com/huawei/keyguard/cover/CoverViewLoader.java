package com.huawei.keyguard.cover;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import com.huawei.keyguard.util.HwLog;

public class CoverViewLoader {
    public static View createView(Context context, String packageName, String layoutName) {
        if (context == null || packageName == null || layoutName == null) {
            HwLog.w("CoverViewLoader", "context = " + context + ", packageName = " + packageName + ", layoutName = " + layoutName);
            return null;
        }
        Context pkgContext = createContextWithPkg(context, packageName);
        if (pkgContext == null) {
            HwLog.w("CoverViewLoader", "createView, pkgContext is null");
            return null;
        }
        LayoutInflater inflater = LayoutInflater.from(pkgContext);
        try {
            inflater.setFactory(HwFrameworkFactory.getHwWidgetManager().createWidgetFactoryHuaWei(context, packageName));
            int layoutID = pkgContext.getResources().getIdentifier(packageName + ":layout/" + layoutName, null, null);
            if (layoutID != 0) {
                return inflater.inflate(layoutID, null, false);
            }
            HwLog.w("CoverViewLoader", "createView, layoutID is 0");
            return null;
        } catch (IllegalStateException e) {
            HwLog.w("CoverViewLoader", "createView, setFactory IllegalStateException");
            return null;
        } catch (NullPointerException e2) {
            HwLog.w("CoverViewLoader", "createView, setFactory NullPointerException");
            return null;
        }
    }

    private static Context createContextWithPkg(Context context, String packageName) {
        if (packageName == null) {
            return null;
        }
        try {
            return context.createPackageContext(packageName, 4);
        } catch (NameNotFoundException e) {
            HwLog.w("CoverViewLoader", "createContextWithPkg, NameNotFoundException");
            return null;
        }
    }
}
