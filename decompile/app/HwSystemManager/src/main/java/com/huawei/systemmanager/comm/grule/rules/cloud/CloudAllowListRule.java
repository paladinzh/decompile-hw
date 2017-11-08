package com.huawei.systemmanager.comm.grule.rules.cloud;

import android.content.Context;
import com.huawei.systemmanager.comm.grule.rules.IRule;

public class CloudAllowListRule implements IRule<String> {
    public boolean match(Context context, String pkgName) {
        return CloudControlRangeNamesHelper.getInstance(context).isPkgInWhiteList(pkgName);
    }
}
