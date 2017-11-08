package com.huawei.systemmanager.comm.grule.rules.path;

public class PreDelApkRule extends APKPathRuleBase {
    public static final String PATH_CUST_DELAPP = "/data/cust/delapp";
    public static final String PATH_SYS_DELAPP = "/system/delapp";

    boolean pathMatch(String path) {
        return !path.equals(PATH_CUST_DELAPP) ? path.equals(PATH_SYS_DELAPP) : true;
    }
}
