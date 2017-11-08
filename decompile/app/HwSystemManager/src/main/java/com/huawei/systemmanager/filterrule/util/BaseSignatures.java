package com.huawei.systemmanager.filterrule.util;

import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustTrashConst;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.HashSet;
import java.util.Set;

public class BaseSignatures {
    private static BaseSignatures sInstance;
    private final String[] mBasePkgs = new String[]{"android", "com.huawei.systemmanager", "com.android.providers.applications", "com.android.providers.contacts", "com.android.providers.userdictionary", "com.android.providers.calendar", "com.android.providers.downloads", "com.android.providers.drm", "com.android.providers.media", "com.android.providers.downloads.ui", "com.android.calculator2", "com.android.htmlviewer", "com.android.browser", HwCustTrashConst.GALLERY_DEFAULT_PKG_NAME, "com.android.exchange", "com.android.email", "com.huawei.android.hwlockscreen", "com.android.smspush", "com.google.android.gsf", "com.google.android.gsf.login", "com.google.android.gms", "com.android.vending"};
    private Set<Integer> mSignSet = new HashSet();

    private BaseSignatures() {
        int i = 0;
        String[] strArr = this.mBasePkgs;
        int length = strArr.length;
        while (i < length) {
            addToSignatureMap(strArr[i]);
            i++;
        }
    }

    private void addToSignatureMap(String pkgName) {
        try {
            int[] codes = HsmPackageManager.getInstance().getPkgInfo(pkgName, 8192).mSignCodes;
            if (codes != null && codes.length != 0) {
                for (int hashCode : codes) {
                    this.mSignSet.add(Integer.valueOf(hashCode));
                }
            }
        } catch (NameNotFoundException e) {
        }
    }

    public static synchronized BaseSignatures getInstance() {
        BaseSignatures baseSignatures;
        synchronized (BaseSignatures.class) {
            if (sInstance == null) {
                sInstance = new BaseSignatures();
            }
            baseSignatures = sInstance;
        }
        return baseSignatures;
    }

    public boolean contains(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        try {
            return contains(HsmPackageManager.getInstance().getPkgInfo(pkgName, 8192).mSignCodes);
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public boolean contains(int[] codes) {
        if (codes == null || codes.length == 0) {
            return false;
        }
        for (int code : codes) {
            if (this.mSignSet.contains(Integer.valueOf(code))) {
                return true;
            }
        }
        return false;
    }
}
