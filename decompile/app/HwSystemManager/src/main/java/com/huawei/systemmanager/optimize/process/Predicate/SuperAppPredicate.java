package com.huawei.systemmanager.optimize.process.Predicate;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.Set;

public class SuperAppPredicate implements Predicate<ProcessAppItem> {
    private static final String TAG = "SuperAppPredicate";
    private Set<String> mSuperApps = Sets.newHashSet();

    public SuperAppPredicate() {
        this.mSuperApps.add("com.tencent.mm");
        this.mSuperApps.add("com.tencent.mobileqqi");
        this.mSuperApps.add("com.tencent.mobileqq");
        this.mSuperApps.add("com.tencent.qqlite");
        this.mSuperApps.add("com.tencent.minihd.qq");
        this.mSuperApps.add("com.bjbyhd.voiceback");
        this.mSuperApps.add("com.dianming.phoneapp");
    }

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        String pkgName = input.getPackageName();
        if (!this.mSuperApps.contains(pkgName)) {
            return true;
        }
        HwLog.i(TAG, "Didnot kill super app, pkg:" + pkgName);
        return false;
    }
}
