package com.huawei.systemmanager.startupmgr.service;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.startupmgr.comm.StartupBinderAccess;
import huawei.android.pfw.HwPFWStartupControlScope;
import java.util.List;
import java.util.regex.Pattern;

public class StartupPreSettingData {
    private Pattern mCtsPattern = Pattern.compile(".*android.*cts.*");
    private List<String> mSystemBlack = Lists.newArrayList();
    private List<String> mThirdWhite = Lists.newArrayList();

    public void loadPreSetting(Context ctx) {
        loadFwkControlScope();
    }

    public boolean defaultReceiveStartupValue(String pkgName) {
        return false;
    }

    public boolean isUnderControlledApp(ApplicationInfo applicationInfo) {
        if (applicationInfo.uid < 10000 || isCtsPackage(applicationInfo.packageName)) {
            return false;
        }
        if (isSystemUnRemovablePkg(applicationInfo)) {
            if (this.mSystemBlack.contains(applicationInfo.packageName)) {
                return true;
            }
        } else if (!this.mThirdWhite.contains(applicationInfo.packageName)) {
            return true;
        }
        return false;
    }

    private void loadFwkControlScope() {
        HwPFWStartupControlScope scope = StartupBinderAccess.startupXmlControlScope();
        if (scope != null) {
            scope.copyOutScope(this.mSystemBlack, this.mThirdWhite);
        }
    }

    private boolean isSystemUnRemovablePkg(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & 1) != 0 && applicationInfo.hwFlags == 0;
    }

    private boolean isCtsPackage(String pkgName) {
        if (this.mCtsPattern.matcher(pkgName).matches() || pkgName.equals("android.tests.devicesetup")) {
            return true;
        }
        return false;
    }
}
