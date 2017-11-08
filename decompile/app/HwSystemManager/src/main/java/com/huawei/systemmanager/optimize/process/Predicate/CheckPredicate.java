package com.huawei.systemmanager.optimize.process.Predicate;

import com.google.common.base.Predicate;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;

public class CheckPredicate implements Predicate<ProcessAppItem> {
    public boolean apply(ProcessAppItem input) {
        boolean check = false;
        if (input == null) {
            return false;
        }
        if (!(input.isKeyProcess() || input.isProtect())) {
            check = true;
        }
        input.setChecked(check);
        return true;
    }
}
