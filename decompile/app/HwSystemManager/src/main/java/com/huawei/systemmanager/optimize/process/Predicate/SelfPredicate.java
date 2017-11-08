package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import com.google.common.base.Predicate;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.Objects;

public class SelfPredicate implements Predicate<ProcessAppItem> {
    private final String self;

    public SelfPredicate(Context ctx) {
        this.self = ctx.getPackageName();
    }

    public boolean apply(ProcessAppItem input) {
        boolean z = false;
        if (input == null) {
            return false;
        }
        boolean z2;
        String pkg = input.getPackageName();
        String str = "SelfPredicate";
        StringBuilder append = new StringBuilder().append("SelfPredicate return = ");
        if (Objects.equals(this.self, pkg)) {
            z2 = false;
        } else {
            z2 = true;
        }
        HwLog.i(str, append.append(z2).toString());
        if (!Objects.equals(this.self, pkg)) {
            z = true;
        }
        return z;
    }
}
