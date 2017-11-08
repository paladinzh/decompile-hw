package com.huawei.systemmanager.comm.grule.rules.appflag;

public class SystemFlagRule extends AppFlagRuleBase {
    boolean flagMatch(int appFlag) {
        return (appFlag & 1) != 0;
    }
}
