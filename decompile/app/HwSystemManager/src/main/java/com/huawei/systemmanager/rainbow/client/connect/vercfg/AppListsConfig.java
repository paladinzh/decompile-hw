package com.huawei.systemmanager.rainbow.client.connect.vercfg;

public class AppListsConfig extends AbsVerConfigItem {
    public AppListsConfig(String spfKey) {
        super(spfKey);
    }

    protected String defaultSubUrl() {
        return "v2/getBlackList.do";
    }
}
