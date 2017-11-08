package com.huawei.systemmanager.rainbow.client.connect.vercfg;

public class AppRightsConfig extends AbsVerConfigItem {
    public AppRightsConfig(String spfKey) {
        super(spfKey);
    }

    protected String defaultSubUrl() {
        return "getApkRight.do";
    }
}
