package com.huawei.systemmanager.comm.grule.rules.fixresult;

import android.content.Context;
import com.huawei.systemmanager.comm.grule.rules.IRule;

public class AlwaysMismatchRule implements IRule<String> {
    public boolean match(Context context, String pkgName) {
        return false;
    }
}
