package com.huawei.systemmanager.comm.grule.rules.signature;

import android.content.Context;
import com.huawei.systemmanager.comm.grule.rules.IRule;
import com.huawei.systemmanager.filterrule.util.BaseSignatures;

public class HuaweiCertificateRule implements IRule<String> {
    public boolean match(Context context, String pkgName) {
        return BaseSignatures.getInstance().contains(pkgName);
    }
}
