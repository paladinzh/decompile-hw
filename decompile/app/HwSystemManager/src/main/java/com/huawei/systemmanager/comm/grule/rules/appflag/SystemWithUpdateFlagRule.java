package com.huawei.systemmanager.comm.grule.rules.appflag;

public class SystemWithUpdateFlagRule extends AppFlagRuleBase {
    boolean flagMatch(int appFlag) {
        return (appFlag & 128) != 0;
    }
}
